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
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val s: Dp = 4.dp,
    val slimM: Dp = 6.dp,
    val m: Dp = 8.dp,
    val slimL: Dp = 12.dp,
    val l: Dp = 16.dp,
    val slimXl: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val oversizeXl: Dp = 40.dp,
    val slimXxl: Dp = 48.dp,
    val xxl: Dp = 64.dp,
)

val LocalDimensions = staticCompositionLocalOf { AppDimensions() }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.dimens: AppDimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current

internal fun getDimensions(): AppDimensions = AppDimensions()