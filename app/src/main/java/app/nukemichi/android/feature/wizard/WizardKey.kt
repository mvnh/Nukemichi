package app.nukemichi.android.feature.wizard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** [subscriptionId] names the subscription the deployed server joins; null puts it in a new one. */
@Serializable
data class WizardKey(val subscriptionId: String? = null) : NavKey
