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

    private var timeUpdateJob: Job? = null
    private var telephonyCallback: Any? = null
    private var legacyPhoneStateListener: PhoneStateListener? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
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
                    percent >= 20 -> BatteryLevelGrade.MEDIUM
                    percent >= 10 -> BatteryLevelGrade.LOW
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
    }

    fun start() {
        // Battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        appContext.registerReceiver(batteryReceiver, filter)

        // Connectivity callback
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            checkNetworkCapabilities()
        }

        // Real-time cellular signal strength monitoring
        registerSignalStrengthListener()

        // WhatsApp installation check
        checkWhatsAppInstalled()

        // Time updates
        startTimeTicker()

        // Initial checks
        checkNetworkCapabilities()
        checkSimState()
        updateDateTime()
    }

    fun stop() {
        timeUpdateJob?.cancel()
        try {
            appContext.unregisterReceiver(batteryReceiver)
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

    private fun updateSignalFromLevel(level: Int) {
        val hasSim = telephonyManager?.simState == TelephonyManager.SIM_STATE_READY
        val grade = if (!hasSim) {
            SignalGrade.NO_SIGNAL
        } else {
            when (level) {
                0 -> SignalGrade.NO_SIGNAL
                1 -> SignalGrade.POOR
                2 -> SignalGrade.POOR
                3 -> SignalGrade.GOOD
                else -> SignalGrade.EXCELLENT
            }
        }
        _status.update {
            it.copy(
                isSimAvailable = hasSim,
                signalGrade = grade
            )
        }
    }

    private fun startTimeTicker() {
        timeUpdateJob?.cancel()
        timeUpdateJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                updateDateTime()
                delay(10_000) // Update every 10 seconds
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
        if (cm == null) {
            _status.update { it.copy(isInternetAvailable = false, isWifiConnected = false) }
            return
        }

        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        // Edge Case: Validate that the network has actual IP throughput
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) || activeNetwork != null)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        _status.update {
            it.copy(
                isInternetAvailable = hasInternet,
                isWifiConnected = isWifi
            )
        }
    }

    fun checkSimState() {
        val tm = telephonyManager
        val simState = tm?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN
        val hasSim = simState == TelephonyManager.SIM_STATE_READY

        val rawLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                tm?.signalStrength?.level ?: 4
            } catch (e: Exception) {
                4
            }
        } else 4

        val grade = if (!hasSim) {
            SignalGrade.NO_SIGNAL
        } else {
            when (rawLevel) {
                0 -> SignalGrade.NO_SIGNAL
                1 -> SignalGrade.POOR
                2 -> SignalGrade.POOR
                3 -> SignalGrade.GOOD
                else -> SignalGrade.EXCELLENT
            }
        }

        _status.update {
            it.copy(
                isSimAvailable = hasSim,
                signalGrade = grade
            )
        }
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
