package com.example.amma

import android.app.Application
import com.example.amma.data.ContactRepository
import com.example.amma.feedback.HapticsManager
import com.example.amma.feedback.SoundCueManager
import com.example.amma.status.SystemStatusEngine
import com.example.amma.telecom.CallOrchestrator
import com.example.amma.voice.VoiceGuidanceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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

    override fun onCreate() {
        super.onCreate()
        instance = this

        contactRepository = ContactRepository(this)
        voiceGuidanceEngine = VoiceGuidanceEngine(this)
        systemStatusEngine = SystemStatusEngine(this, applicationScope)
        callOrchestrator = CallOrchestrator(this)
        hapticsManager = HapticsManager(this)
        soundCueManager = SoundCueManager()

        systemStatusEngine.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        systemStatusEngine.stop()
        voiceGuidanceEngine.shutdown()
        soundCueManager.release()
    }

    companion object {
        lateinit var instance: AmmaApplication
            private set
    }
}
