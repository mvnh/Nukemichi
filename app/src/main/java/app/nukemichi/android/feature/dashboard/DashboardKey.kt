package app.nukemichi.android.feature.dashboard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** [selectServerId] becomes the selected server on arrival, e.g. the one the wizard just deployed. */
@Serializable
data class DashboardKey(val selectServerId: String? = null) : NavKey
