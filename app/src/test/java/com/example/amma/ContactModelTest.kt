package com.example.amma

import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactModelTest {

    @Test
    fun effectivePronunciation_usesCustomIfAvailable() {
        val contactWithCustom = Contact(
            displayName = "Santhosh",
            relationship = "Son",
            phoneNumber = "9876543210",
            customPronunciation = "సంతోష్"
        )
        assertEquals("సంతోష్", contactWithCustom.effectivePronunciation)
    }

    @Test
    fun effectivePronunciation_fallsBackToDisplayName() {
        val contactWithoutCustom = Contact(
            displayName = "Santhosh",
            relationship = "Son",
            phoneNumber = "9876543210",
            customPronunciation = null
        )
        assertEquals("Santhosh", contactWithoutCustom.effectivePronunciation)
    }

    @Test
    fun contactDefaultTransports_validateEnumIntegrity() {
        assertEquals("సాధారణ ఫోన్", CallTransport.CELLULAR.teluguLabel)
        assertEquals("వాట్సాప్ వీడియో", CallTransport.WHATSAPP_VIDEO.teluguLabel)
        assertEquals("వాట్సాప్ ఆడియో", CallTransport.WHATSAPP_AUDIO.teluguLabel)
    }
}
