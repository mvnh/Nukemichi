package app.nukemichi.android.feature.dashboard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DashboardKey(val selectServerId: String? = null) : NavKey
