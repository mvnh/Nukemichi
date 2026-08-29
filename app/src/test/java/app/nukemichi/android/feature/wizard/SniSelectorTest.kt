package app.nukemichi.android.feature.wizard

import app.nukemichi.android.feature.wizard.impl.domain.model.SniSelector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SniSelectorTest {

    @Test
    fun `rejects ru and by domains`() {
        assertFalse(SniSelector.isAllowed("max.ru"))
        assertFalse(SniSelector.isAllowed("mirror.corbina.net.ru"))
        assertFalse(SniSelector.isAllowed("some.domain.by"))
    }

    @Test
    fun `rejects explicitly blocked domains and subdomains`() {
        assertFalse(SniSelector.isAllowed("vk.com"))
        assertFalse(SniSelector.isAllowed("www.microsoft.com"))
        assertFalse(SniSelector.isAllowed("cloudflare.com"))
    }

    @Test
    fun `rejects dynamic DNS domains`() {
        assertFalse(SniSelector.isAllowed("simpp.duckdns.org"))
        assertFalse(SniSelector.isAllowed("home.no-ip.com"))
        assertFalse(SniSelector.isAllowed("box.ddns.net"))
    }

    @Test
    fun `rejects wildcard and malformed hostnames`() {
        assertFalse(SniSelector.isAllowed("*.userapi.com"))
        assertFalse(SniSelector.isAllowed("*.example.com"))
        assertFalse(SniSelector.isAllowed("-bad.example.com"))
        assertFalse(SniSelector.isAllowed("no-dot"))
        assertFalse(SniSelector.isAllowed(""))
    }

    @Test
    fun `allows ordinary domains`() {
        assertTrue(SniSelector.isAllowed("rocky-linux.tk"))
        assertTrue(SniSelector.isAllowed("mirror.i3d.net"))
    }

    @Test
    fun `is case- and trailing-dot-insensitive`() {
        assertFalse(SniSelector.isAllowed("MAX.RU"))
        assertFalse(SniSelector.isAllowed("vk.com."))
    }
}
