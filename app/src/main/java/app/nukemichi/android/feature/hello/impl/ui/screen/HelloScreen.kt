package app.nukemichi.android.feature.hello.impl.ui.screen

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import app.nukemichi.android.feature.hello.impl.ui.screen.components.HelloContent

private const val SOURCE_CODE_URL = "https://github.com/mvnh/Nukemichi"

@Composable
internal fun HelloScreen(
    onSetUpServerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    HelloContent(
        modifier = modifier,
        onSetUpServerClick = onSetUpServerClick,
        onConnectOrImportClick = {},
        onLearnFirstClick = {},
        onViewSourceCodeClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, SOURCE_CODE_URL.toUri()))
        },
    )
}
