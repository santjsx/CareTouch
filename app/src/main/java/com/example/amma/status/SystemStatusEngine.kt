package com.example.amma.status

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import com.example.amma.model.BatteryLevelGrade
import com.example.amma.model.SignalGrade
import com.example.amma.model.SystemStatus
import com.example.amma.voice.TeluguPhraseResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SystemStatusEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val _status = MutableStateFlow(SystemStatus())
    val status: StateFlow<SystemStatus> = _status.asStateFlow()

    private var telemetryJob: Job? = null
    private var telephonyCallback: Any? = null
    private var legacyPhoneStateListener: PhoneStateListener? = null

    private val systemBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> handleBatteryIntent(intent)
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> checkSignalStrength()
                "android.net.conn.CONNECTIVITY_CHANGE" -> checkNetworkCapabilities()
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkNetworkCapabilities()
        }

        override fun onLost(network: Network) {
            checkNetworkCapabilities()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            checkNetworkCapabilities()
        }

        override fun onUnavailable() {
            checkNetworkCapabilities()
        }
    }

    fun start() {
        // Broadcast receiver for battery, airplane mode & connectivity
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
        }
        appContext.registerReceiver(systemBroadcastReceiver, filter)

        // Connectivity callback
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            checkNetworkCapabilities()
        }

        // Real-time cellular signal strength monitoring callback
        registerSignalStrengthListener()

        // WhatsApp installation check
        checkWhatsAppInstalled()

        // Start High-Frequency Telemetry Ticker (Time + Signal + Network Polling)
        startTelemetryTicker()

        // Initial checks
        checkNetworkCapabilities()
        checkSimState()
        checkSignalStrength()
        updateDateTime()
    }

    fun stop() {
        telemetryJob?.cancel()
        try {
            appContext.unregisterReceiver(systemBroadcastReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Callback not registered
        }
        unregisterSignalStrengthListener()
    }

    private fun handleBatteryIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val percent = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            _status.value.batteryPercent
        }

        val grade = when {
            isCharging -> BatteryLevelGrade.CHARGING
            percent >= 80 -> BatteryLevelGrade.EXCELLENT
            percent >= 50 -> BatteryLevelGrade.GOOD
            percent >= 25 -> BatteryLevelGrade.MEDIUM
            percent >= 15 -> BatteryLevelGrade.LOW
            else -> BatteryLevelGrade.CRITICAL
        }

        _status.update {
            it.copy(
                batteryPercent = percent,
                isCharging = isCharging,
                batteryGrade = grade
            )
        }
    }

    private fun registerSignalStrengthListener() {
        val tm = telephonyManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        updateSignalFromLevel(signalStrength.level)
                    }
                }
                telephonyCallback = callback
                tm.registerTelephonyCallback(appContext.mainExecutor, callback)
            } else {
                @Suppress("DEPRECATION")
                val listener = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onSignalStrengthsChanged(signalStrength: android.telephony.SignalStrength?) {
                        val level = signalStrength?.level ?: 4
                        updateSignalFromLevel(level)
                    }
                }
                legacyPhoneStateListener = listener
                @Suppress("DEPRECATION")
                tm.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not register telephony signal strength listener", e)
        }
    }

    private fun unregisterSignalStrengthListener() {
        val tm = telephonyManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (telephonyCallback as? TelephonyCallback)?.let {
                    tm.unregisterTelephonyCallback(it)
                }
            } else {
                @Suppress("DEPRECATION")
                legacyPhoneStateListener?.let {
                    tm.listen(it, PhoneStateListener.LISTEN_NONE)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering signal listener", e)
        }
    }

    fun updateSignalFromLevel(level: Int) {
        val hasSim = telephonyManager?.simState == TelephonyManager.SIM_STATE_READY
        val clampedLevel = level.coerceIn(0, 4)
        val grade = if (!hasSim) {
            SignalGrade.NO_SIGNAL
        } else {
            when (clampedLevel) {
                0 -> SignalGrade.NO_SIGNAL
                1 -> SignalGrade.POOR
                2 -> SignalGrade.MODERATE
                3 -> SignalGrade.GOOD
                else -> SignalGrade.EXCELLENT
            }
        }
        _status.update {
            it.copy(
                isSimAvailable = hasSim,
                signalBars = if (hasSim) clampedLevel else 0,
                signalGrade = grade
            )
        }
    }

    fun checkSignalStrength() {
        val tm = telephonyManager ?: return
        val hasSim = tm.simState == TelephonyManager.SIM_STATE_READY
        if (!hasSim) {
            _status.update { it.copy(isSimAvailable = false, signalBars = 0, signalGrade = SignalGrade.NO_SIGNAL) }
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val sig = tm.signalStrength
                if (sig != null) {
                    val cellLevels = sig.cellSignalStrengths
                    val resolvedLevel = if (cellLevels.isNotEmpty()) {
                        cellLevels.firstOrNull()?.level ?: sig.level
                    } else {
                        sig.level
                    }
                    updateSignalFromLevel(resolvedLevel)
                    return
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig = tm.signalStrength
                if (sig != null) {
                    updateSignalFromLevel(sig.level)
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error polling signalStrength", e)
        }
    }

    private fun startTelemetryTicker() {
        telemetryJob?.cancel()
        telemetryJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                updateDateTime()
                checkNetworkCapabilities()
                checkSignalStrength()
                delay(1500) // Poll telemetry every 1.5s for instant response to emulator/device changes
            }
        }
    }

    fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(calendar.time)

        val teluguTime = TeluguPhraseResolver.getTimePhrase(calendar)
        val teluguDate = TeluguPhraseResolver.getDatePhrase(calendar)

        _status.update {
            it.copy(
                formattedTime = formattedTime,
                formattedDate = teluguDate.replace("ఈరోజు ", ""),
                teluguTimePhrase = teluguTime,
                teluguDatePhrase = teluguDate
            )
        }
    }

    fun checkNetworkCapabilities() {
        val cm = connectivityManager
        val tm = telephonyManager

        if (cm == null) {
            _status.update { it.copy(isInternetAvailable = false, isWifiConnected = false, isDataDenied = true) }
            return
        }

        val activeNetwork = cm.activeNetwork
        val caps = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null

        val hasInternetCapability = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isValidated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val isNotSuspended = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED) == true
        } else true

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        val mobileDataState = tm?.dataState ?: TelephonyManager.DATA_UNKNOWN
        val isMobileDataConnected = mobileDataState == TelephonyManager.DATA_CONNECTED

        val hasInternet = (isWifi && hasInternetCapability && isValidated) ||
                (isCellular && hasInternetCapability && isValidated && isMobileDataConnected) ||
                (hasInternetCapability && isValidated && isNotSuspended && activeNetwork != null)

        val isDenied = !isWifi && (mobileDataState == TelephonyManager.DATA_DISCONNECTED || !hasInternet)

        _status.update {
            it.copy(
                isInternetAvailable = hasInternet,
                isWifiConnected = isWifi,
                isDataDenied = isDenied
            )
        }
    }

    fun checkSimState() {
        val tm = telephonyManager
        val simState = tm?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN
        val hasSim = simState == TelephonyManager.SIM_STATE_READY
        checkSignalStrength()
    }

    fun checkWhatsAppInstalled(): Boolean {
        val pm = appContext.packageManager
        val isInstalled = try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            try {
                pm.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES)
                true
            } catch (e2: Exception) {
                false
            }
        }
        _status.update { it.copy(isWhatsAppInstalled = isInstalled) }
        return isInstalled
    }

    companion object {
        private const val TAG = "SystemStatusEngine"
    }
}
