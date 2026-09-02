package app.nukemichi.android.core.ui.theme.size

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppDimensions(
    // Spacing scale. Every step doubles, so a layout can only land on the scale or off it.
    val xs: Dp = 2.dp,
    val s: Dp = 4.dp,
    val m: Dp = 8.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 64.dp,

    // Sizes that are not spacing. Naming them by role keeps them off the scale above, where they
    // would otherwise need half-steps that mean nothing on their own.
    val cornerRadius: Dp = 12.dp,
    val icon: Dp = 24.dp,
    val control: Dp = 40.dp,
    val scrollIndicatorHeight: Dp = 6.dp,
    val successBadge: Dp = 80.dp,
)

val LocalDimensions = staticCompositionLocalOf { AppDimensions() }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.dimens: AppDimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current
