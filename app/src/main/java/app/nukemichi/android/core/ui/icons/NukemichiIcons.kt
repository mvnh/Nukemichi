package app.nukemichi.android.core.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import app.nukemichi.android.core.ui.icons.internal._arrow_back
import app.nukemichi.android.core.ui.icons.internal._arrow_drop_up
import app.nukemichi.android.core.ui.icons.internal._auto_stories
import app.nukemichi.android.core.ui.icons.internal._cable
import app.nukemichi.android.core.ui.icons.internal._check
import app.nukemichi.android.core.ui.icons.internal._close
import app.nukemichi.android.core.ui.icons.internal._dns
import app.nukemichi.android.core.ui.icons.internal._lock
import app.nukemichi.android.core.ui.icons.internal._more_vert
import app.nukemichi.android.core.ui.icons.internal._rocket_launch
import app.nukemichi.android.core.ui.icons.internal._schedule
import app.nukemichi.android.core.ui.icons.internal._share
import app.nukemichi.android.core.ui.icons.internal._shield
import app.nukemichi.android.core.ui.icons.internal._visibility
import app.nukemichi.android.core.ui.icons.internal._visibility_off
import app.nukemichi.android.core.ui.icons.internal._vpn_key

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
        val ArrowDropDown: ImageVector get() = _arrow_drop_up
        val Close: ImageVector get() = _close
        val MoreVert: ImageVector get() = _more_vert
        val Share: ImageVector get() = _share
    }
}
