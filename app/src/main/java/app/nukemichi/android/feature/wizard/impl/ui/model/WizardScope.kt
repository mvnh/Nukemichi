package app.nukemichi.android.feature.wizard.impl.ui.model

import androidx.compose.runtime.Stable

@Stable
interface WizardScope {
    val wizardState: WizardState
    fun navigateTo(page: Int)
    fun next()
    fun previous()
}

internal class WizardScopeImpl(
    override val wizardState: WizardState
) : WizardScope {
    override fun navigateTo(page: Int) = wizardState.navigateTo(page)
    override fun next() = wizardState.next()
    override fun previous() = wizardState.previous()
}
