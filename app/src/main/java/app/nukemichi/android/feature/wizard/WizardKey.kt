package app.nukemichi.android.feature.wizard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class WizardKey(val flow: WizardFlow = WizardFlow.DEPLOY_SERVER) : NavKey
