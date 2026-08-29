package app.nukemichi.android.feature.wizard

import kotlinx.serialization.Serializable

/** Which purpose the wizard was opened for — determines its top-bar title. Only [DEPLOY_SERVER]
 *  has real steps today; the other two are entry points reserved for the Hello screen's
 *  currently-disabled "Connect or import" action. */
@Serializable
enum class WizardFlow {
    DEPLOY_SERVER,
    ADD_TO_SUBSCRIPTION,
    IMPORT_URI,
}
