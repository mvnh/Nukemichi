package app.nukemichi.android.feature.wizard.impl.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import app.nukemichi.android.core.ui.util.UiText

@Stable
data class WizardStep(
    val title: UiText = UiText.Empty,
    val content: @Composable WizardScope.() -> Unit
)
