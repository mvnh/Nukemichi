package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring

// Material 3's standard motion scheme, spelled out because material3 1.4 keeps MotionScheme internal:
// spatial for anything that moves or resizes, effects for colour and opacity.

private const val SPATIAL_DAMPING_RATIO = 0.9f
private const val SPATIAL_STIFFNESS = 700f
private const val EFFECTS_DAMPING_RATIO = 1f
private const val EFFECTS_STIFFNESS = 1600f

internal fun <T> dashboardSpatialSpec(): FiniteAnimationSpec<T> =
    spring(dampingRatio = SPATIAL_DAMPING_RATIO, stiffness = SPATIAL_STIFFNESS)

internal fun <T> dashboardEffectsSpec(): FiniteAnimationSpec<T> =
    spring(dampingRatio = EFFECTS_DAMPING_RATIO, stiffness = EFFECTS_STIFFNESS)
