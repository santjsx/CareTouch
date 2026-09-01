package com.example.amma

import android.app.Application
import com.example.amma.cloud.auth.AuthRepository
import com.example.amma.cloud.auth.AuthState
import com.example.amma.cloud.drive.GoogleDriveStorageManager
import com.example.amma.cloud.firestore.FirestoreSyncEngine
import com.example.amma.cloud.r2.R2StorageManager
import com.example.amma.data.ContactRepository
import com.example.amma.feedback.HapticsManager
import com.example.amma.feedback.SoundCueManager
import com.example.amma.status.SystemStatusEngine
import com.example.amma.telecom.CallOrchestrator
import com.example.amma.voice.VoiceGuidanceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AmmaApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var contactRepository: ContactRepository
        private set

    lateinit var voiceGuidanceEngine: VoiceGuidanceEngine
        private set

    lateinit var systemStatusEngine: SystemStatusEngine
        private set

    lateinit var callOrchestrator: CallOrchestrator
        private set

    lateinit var hapticsManager: HapticsManager
        private set

    lateinit var soundCueManager: SoundCueManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var firestoreSyncEngine: FirestoreSyncEngine
        private set

    lateinit var r2StorageManager: R2StorageManager
        private set

    lateinit var googleDriveStorageManager: GoogleDriveStorageManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        contactRepository = ContactRepository(this)
        voiceGuidanceEngine = VoiceGuidanceEngine(this)
        systemStatusEngine = SystemStatusEngine(this, applicationScope)
        callOrchestrator = CallOrchestrator(this)
        hapticsManager = HapticsManager(this)
        soundCueManager = SoundCueManager()

        // Cloud Integrations: Auth, Firestore, Google Drive & Cloudflare R2
        authRepository = AuthRepository(this)
        firestoreSyncEngine = FirestoreSyncEngine(this, contactRepository, applicationScope)
        r2StorageManager = R2StorageManager(this)
        googleDriveStorageManager = GoogleDriveStorageManager(this)

        // Automatically start Firestore sync when authenticated
        applicationScope.launch {
            authRepository.authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    firestoreSyncEngine.startSync(state.uid)
                } else {
                    firestoreSyncEngine.stopSync()
                }
            }
        }

        systemStatusEngine.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        firestoreSyncEngine.stopSync()
        systemStatusEngine.stop()
        voiceGuidanceEngine.shutdown()
        soundCueManager.release()
    }

    companion object {
        lateinit var instance: AmmaApplication
            private set
    }
}
