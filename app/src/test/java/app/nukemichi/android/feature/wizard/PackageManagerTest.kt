package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager
import app.nukemichi.android.feature.wizard.impl.domain.ssh.DetectPackageManagerCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PackageManagerTest {

    @Test
    fun `install command uses the right syntax per manager`() {
        assertEquals(
            "export DEBIAN_FRONTEND=noninteractive; apt-get update -qq && apt-get install -y -qq curl unzip",
            PackageManager.APT.installCommand("curl", "unzip"),
        )
        assertEquals("dnf install -y -q curl unzip", PackageManager.DNF.installCommand("curl", "unzip"))
        assertEquals("yum install -y -q curl unzip", PackageManager.YUM.installCommand("curl", "unzip"))
        assertEquals("pacman -Sy --noconfirm curl unzip", PackageManager.PACMAN.installCommand("curl", "unzip"))
        assertEquals("apk add --no-cache curl unzip", PackageManager.APK.installCommand("curl", "unzip"))
    }

    @Test
    fun `fromToken round-trips all entries`() {
        PackageManager.entries.forEach { manager ->
            assertEquals(manager, PackageManager.fromToken(manager.token))
        }
    }

    @Test
    fun `fromToken returns null for an unknown token`() {
        assertNull(PackageManager.fromToken("zypper"))
    }

    @Test
    fun `detect command parses a known token`() {
        val result = CommandResult(stdout = "apt\n", stderr = "", exitCode = 0)
        assertEquals(PackageManager.APT, DetectPackageManagerCommand().parseOutput(result))
    }

    @Test
    fun `detect command rejects unknown output`() {
        val result = CommandResult(stdout = "none\n", stderr = "", exitCode = 0)
        assertThrows(IllegalArgumentException::class.java) {
            DetectPackageManagerCommand().parseOutput(result)
        }
    }
}
