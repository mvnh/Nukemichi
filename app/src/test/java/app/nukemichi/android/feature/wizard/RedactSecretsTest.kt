package app.nukemichi.android.feature.wizard

import app.nukemichi.android.feature.wizard.impl.domain.model.redactSecrets
import org.junit.Assert.assertEquals
import org.junit.Test

class RedactSecretsTest {

    @Test
    fun `redacts KEY=value lines while keeping the key name visible`() {
        assertEquals(
            "UUID=[REDACTED]",
            redactSecrets("UUID=123e4567-e89b-12d3-a456-426614174000"),
        )
        assertEquals(
            "PRIVATE_KEY=[REDACTED]",
            redactSecrets("PRIVATE_KEY=8JZ2avYyjEr7RqrRnMxDf3lQjuNQ5ZQ8x3z6Wp3nqA0"),
        )
        assertEquals(
            "SHORT_ID=[REDACTED]",
            redactSecrets("SHORT_ID=a1b2c3d4e5f6a7b8"),
        )
    }

    @Test
    fun `redacts a bare UUID appearing outside a KEY=value line`() {
        assertEquals(
            "Generated uuid [REDACTED] for this profile",
            redactSecrets("Generated uuid 123e4567-e89b-12d3-a456-426614174000 for this profile"),
        )
    }

    @Test
    fun `leaves ordinary log lines untouched`() {
        val line = "Downloading Xray-linux-arm64-v8a.zip from github.com/XTLS/Xray-core"
        assertEquals(line, redactSecrets(line))
    }
}
