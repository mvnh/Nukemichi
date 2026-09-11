package app.nukemichi.android.core.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XrayNicknameTest {

    @Test
    fun `is deterministic for the same seed`() {
        assertEquals(generateNickname("203.0.113.42"), generateNickname("203.0.113.42"))
    }

    @Test
    fun `is two words`() {
        val nickname = generateNickname("198.51.100.7")
        assertEquals(2, nickname.split(" ").size)
    }

    @Test
    fun `different seeds can produce different names`() {
        val names = setOf(
            generateNickname("10.0.0.1"),
            generateNickname("10.0.0.2"),
            generateNickname("10.0.0.3"),
            generateNickname("10.0.0.4"),
        )
        assertTrue("expected some variety across different seeds", names.size > 1)
    }
}
