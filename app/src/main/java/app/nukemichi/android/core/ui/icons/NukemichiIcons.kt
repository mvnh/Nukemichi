package app.nukemichi.android.core.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import app.nukemichi.android.core.ui.icons.sources._arrow_back
import app.nukemichi.android.core.ui.icons.sources._arrow_drop_down
import app.nukemichi.android.core.ui.icons.sources._arrow_drop_up
import app.nukemichi.android.core.ui.icons.sources._auto_stories
import app.nukemichi.android.core.ui.icons.sources._cable
import app.nukemichi.android.core.ui.icons.sources._check
import app.nukemichi.android.core.ui.icons.sources._close
import app.nukemichi.android.core.ui.icons.sources._dns
import app.nukemichi.android.core.ui.icons.sources._lock
import app.nukemichi.android.core.ui.icons.sources._more_vert
import app.nukemichi.android.core.ui.icons.sources._rocket_launch
import app.nukemichi.android.core.ui.icons.sources._schedule
import app.nukemichi.android.core.ui.icons.sources._share
import app.nukemichi.android.core.ui.icons.sources._shield
import app.nukemichi.android.core.ui.icons.sources._visibility
import app.nukemichi.android.core.ui.icons.sources._visibility_off
import app.nukemichi.android.core.ui.icons.sources._vpn_key

object NukemichiIcons {

    object Common {
        val RocketLaunch: ImageVector get() = _rocket_launch
        val Shield: ImageVector get() = _shield
        val Schedule: ImageVector get() = _schedule
        val Check: ImageVector get() = _check
        val Cable: ImageVector get() = _cable
        val Dns: ImageVector get() = _dns
        val AutoStories: ImageVector get() = _auto_stories
        val Lock: ImageVector get() = _lock
        val VpnKey: ImageVector get() = _vpn_key
        val Visibility: ImageVector get() = _visibility
        val VisibilityOff: ImageVector get() = _visibility_off
    }

    object Navigation {
        val ArrowBack: ImageVector get() = _arrow_back
        val ArrowDropUp: ImageVector get() = _arrow_drop_up
        val ArrowDropDown: ImageVector get() = _arrow_drop_down
        val Close: ImageVector get() = _close
        val MoreVert: ImageVector get() = _more_vert
        val Share: ImageVector get() = _share
    }
}
