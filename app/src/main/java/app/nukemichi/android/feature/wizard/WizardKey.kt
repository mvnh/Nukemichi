package app.nukemichi.android.feature.wizard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class WizardKey(val flow: WizardFlow = WizardFlow.DEPLOY_SERVER) : NavKey

/** Which purpose the wizard was opened for — determines its top-bar title. Only [DEPLOY_SERVER]
 *  has real steps today; the other two are entry points reserved for the Hello screen's
 *  currently-disabled "Connect or import" action. Lives alongside [WizardKey] rather than in its
 *  own file: it's part of the key's public constructor signature, so it has to be at the
 *  feature's root too. */
@Serializable
enum class WizardFlow {
    DEPLOY_SERVER,
    ADD_TO_SUBSCRIPTION,
    IMPORT_URI,
}
