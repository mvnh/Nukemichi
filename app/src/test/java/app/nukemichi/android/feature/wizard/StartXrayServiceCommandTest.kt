package app.nukemichi.android.feature.wizard

import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager
import app.nukemichi.android.feature.wizard.impl.domain.ssh.StartXrayServiceCommand
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartXrayServiceCommandTest {

    @Test
    fun `Alpine's package manager selects an OpenRC script, not systemd`() {
        val script = StartXrayServiceCommand(PackageManager.APK).script

        assertTrue(script.contains("rc-service"))
        assertFalse(script.contains("systemctl"))
    }

    @Test
    fun `every other package manager selects the systemd script`() {
        val nonAlpine = PackageManager.entries - PackageManager.APK

        nonAlpine.forEach { packageManager ->
            val script = StartXrayServiceCommand(packageManager).script
            assertTrue("$packageManager should get systemctl", script.contains("systemctl"))
            assertFalse("$packageManager should not get rc-service", script.contains("rc-service"))
        }
    }
}
