package app.nukemichi.android.feature.wizard

import app.nukemichi.android.feature.wizard.impl.domain.model.generateServerNickname
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerNicknameTest {

    @Test
    fun `is deterministic for the same seed`() {
        assertEquals(generateServerNickname("203.0.113.42"), generateServerNickname("203.0.113.42"))
    }

    @Test
    fun `is two words`() {
        val nickname = generateServerNickname("198.51.100.7")
        assertEquals(2, nickname.split(" ").size)
    }

    @Test
    fun `different seeds can produce different names`() {
        val names = setOf(
            generateServerNickname("10.0.0.1"),
            generateServerNickname("10.0.0.2"),
            generateServerNickname("10.0.0.3"),
            generateServerNickname("10.0.0.4"),
        )
        assertTrue("expected some variety across different seeds", names.size > 1)
    }
}
