package app.nukemichi.android.feature.hello.impl.ui.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString

@Composable
internal fun HelloHeader(subtitle: UiText) {
    val highlight = stringResource(id = R.string.hello_header_title_highlight)
    val fullTitle = stringResource(id = R.string.hello_header_title, highlight)
    val highlightStart = fullTitle.lastIndexOf(highlight).coerceAtLeast(0)

    val title = buildAnnotatedString {
        append(fullTitle.substring(0, highlightStart))
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(fullTitle.substring(highlightStart))
        }
    }

    Column {
        Icon(
            painter = painterResource(id = R.drawable.michi_cat),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(MaterialTheme.dimens.xxl)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.m))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.s))

        Text(
            text = subtitle.asString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
