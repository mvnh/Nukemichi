package app.nukemichi.android.feature.dashboard.impl.ui.screen

import android.content.Intent
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.components.StatusBadge
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.CollectAsEffect
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardViewModel
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isConnected
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.ConnectionToggle
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.ServerInfoCard
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.StatsRow
import kotlinx.coroutines.delay

private const val SLOW_TRANSITION_HINT_DELAY_MS = 5_000L

@Composable
internal fun DashboardScreen(
    onNavigateToLogs: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.processIntent(DashboardContract.Intent.ErrorDismissed)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val intent = if (result.resultCode == android.app.Activity.RESULT_OK) {
            DashboardContract.Intent.VpnPermissionGranted
        } else {
            DashboardContract.Intent.VpnPermissionDenied
        }
        viewModel.processIntent(intent)
    }

    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is DashboardContract.Effect.RequestVpnPermission ->
                permissionLauncher.launch(effect.permissionIntent)

            is DashboardContract.Effect.ShareVlessLink -> {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.uri)
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        DashboardContent(
            state = state,
            onToggleConnection = { viewModel.processIntent(DashboardContract.Intent.ToggleConnection) },
            onExportVlessLinkClick = { viewModel.processIntent(DashboardContract.Intent.ExportVlessLinkRequested) },
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(MaterialTheme.dimens.m),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.s),
        ) {
            TextButton(onClick = onNavigateToLogs) {
                Text(text = stringResource(R.string.dashboard_logs))
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun DashboardContent(
    state: DashboardContract.State,
    onToggleConnection: () -> Unit,
    onExportVlessLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimens.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.m),
        ) {
            Text(
                text = state.profileName ?: stringResource(R.string.dashboard_no_server_configured),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.profileName != null) {
                TextButton(onClick = onExportVlessLinkClick) {
                    Icon(
                        imageVector = NukemichiIcons.Navigation.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimens.l),
                    )
                    Spacer(modifier = Modifier.width(dimens.s))
                    Text(
                        text = stringResource(R.string.dashboard_export_link_label),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        Box(modifier = Modifier.padding(top = dimens.xl, bottom = dimens.xl)) {
            ConnectionToggle(
                state = state,
                onClick = onToggleConnection,
            )
        }

        StatusBadge(text = state.engineState.label())

        if (state.engineState == XrayEngineState.STARTING || state.engineState == XrayEngineState.STOPPING) {
            var showsSlowHint by remember(state.engineState) { mutableStateOf(false) }
            LaunchedEffect(state.engineState) {
                delay(SLOW_TRANSITION_HINT_DELAY_MS)
                showsSlowHint = true
            }
            AnimatedVisibility(visible = showsSlowHint) {
                Text(
                    text = stringResource(R.string.dashboard_state_taking_a_while),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimens.s),
                )
            }
        }

        state.connectedSinceRealtime?.let { since ->
            var nowRealtime by remember(since) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
            LaunchedEffect(since) {
                while (true) {
                    nowRealtime = SystemClock.elapsedRealtime()
                    delay(1_000)
                }
            }
            Text(
                text = stringResource(
                    R.string.dashboard_connected_for,
                    formatDuration(nowRealtime - since),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimens.s),
            )
        }

        if (state.isConnected) {
            StatsRow(
                stats = state.stats,
                modifier = Modifier
                    .padding(top = dimens.xl)
                    .fillMaxWidth(),
            )
        }

        if (state.serverAddress != null) {
            ServerInfoCard(
                state = state,
                modifier = Modifier
                    .padding(top = dimens.xl)
                    .fillMaxWidth(),
            )
        }
    }
}

private fun XrayEngineState.label(): UiText = when (this) {
    XrayEngineState.IDLE -> UiText.Resource(R.string.dashboard_state_not_connected)
    XrayEngineState.STARTING -> UiText.Resource(R.string.dashboard_state_connecting)
    XrayEngineState.RUNNING -> UiText.Resource(R.string.dashboard_state_connected)
    XrayEngineState.STOPPING -> UiText.Resource(R.string.dashboard_state_disconnecting)
    XrayEngineState.STOPPED -> UiText.Resource(R.string.dashboard_state_not_connected)
    XrayEngineState.ERROR -> UiText.Resource(R.string.dashboard_state_error)
}

@Composable
private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) stringResource(R.string.duration_hours_minutes, hours, minutes)
    else stringResource(R.string.duration_minutes_seconds, minutes, seconds)
}
