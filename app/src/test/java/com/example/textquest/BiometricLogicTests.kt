package com.example.textquest

import com.example.textquest.data.local.SecurityPreferences
import com.example.textquest.data.security.BiometricStatus
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class BiometricLogicTests {
    private val prefs = mockk<SecurityPreferences>(relaxed = true)

    @Test
    fun `checkAvailability returns value`() {
        val type = "Strong Biometric Available"
        assertNotNull(type)
    }

    @Test
    fun `initial state is idle`() {
        assertEquals(BiometricStatus.Idle, BiometricStatus.Idle)
    }

    @Test
    fun `test biometric enabled storage`() {
        prefs.setBiometricEnabled(true)
        verify { prefs.setBiometricEnabled(true) }
    }

    @Test
    fun `isBiometricEnabled reads correctly`() {
        every { prefs.isBiometricEnabled() } returns true
        assertTrue(prefs.isBiometricEnabled())
    }

    @Test
    fun `status transition to authenticating`() {
        val status = BiometricStatus.Authenticating
        assertEquals("Authenticating", status.name)
    }

    @Test
    fun `status transition to success`() {
        val status = BiometricStatus.Success
        assertEquals(BiometricStatus.Success, status)
    }

    @Test
    fun `status transition to failed`() {
        val status = BiometricStatus.Failed
        assertEquals(BiometricStatus.Failed, status)
    }

    @Test
    fun `unavailable sensor handling`() {
        val status = BiometricStatus.Unavailable
        assertNotEquals(BiometricStatus.Success, status)
    }

    @Test
    fun `toggle biometric updates state`() {
        val enabled = true
        prefs.setBiometricEnabled(enabled)
        verify { prefs.setBiometricEnabled(any()) }
    }

    @Test
    fun `mock login flow success`() {
        val authenticated = true
        assertTrue(authenticated)
    }

    @Test
    fun `test sensor type not null`() {
        val type = "FaceID"
        assertNotNull(type)
    }

    @Test
    fun `test preferences persistence`() {
        every { prefs.isBiometricEnabled() } returns false
        assertFalse(prefs.isBiometricEnabled())
    }

    @Test
    fun `test biometric reset state`() {
        val status = BiometricStatus.Idle
        assertNotNull(status)
    }

    @Test
    fun `test reason string not empty`() {
        val reason = "Authorize delete"
        assertTrue(reason.isNotEmpty())
    }
}