package app.nukemichi.android.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SecretTest {

    @Test
    fun `toString masks the value`() {
        assertEquals("Secret(***)", Secret("hunter2").toString())
    }

    @Test
    fun `the value itself is preserved`() {
        assertEquals("hunter2", Secret("hunter2").value)
    }

    @Test
    fun `string interpolation goes through the masking override`() {
        assertFalse("${Secret("hunter2")}".contains("hunter2"))
    }
}
