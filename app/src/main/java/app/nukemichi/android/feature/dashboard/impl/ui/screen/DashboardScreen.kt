package app.nukemichi.android.feature.dashboard.impl.ui.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.nukemichi.android.R
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardViewModel
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.ConnectFab
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.ConnectionHeader
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.DeployServerButton
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.ServerDetailsSheet
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.SubscriptionEditorSheet
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.connectFabClearance
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.rememberIsConnectionToggleScrolledAway
import app.nukemichi.android.feature.dashboard.impl.ui.screen.components.subscriptionItems
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens
import app.nukemichi.android.platform.ui.util.CollectAsEffect
import app.nukemichi.android.platform.ui.util.asString

private const val CONNECTION_ITEM_KEY = "connection"
private const val DEPLOY_ITEM_KEY = "deploy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(
    selectServerId: String?,
    onSettingsClick: () -> Unit,
    onNavigateToWizard: (subscriptionId: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel<DashboardViewModel, DashboardViewModel.Factory>(
        creationCallback = { factory -> factory.create(selectServerId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val isToggleScrolledAway by rememberIsConnectionToggleScrolledAway(listState)

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

            is DashboardContract.Effect.ShareText -> {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }

            is DashboardContract.Effect.NavigateToWizard -> onNavigateToWizard(effect.subscriptionId)
        }
    }

    Scaffold(
        modifier = modifier,
        // Unlike the default system bars, safeDrawing also keeps the list clear of a display cutout.
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = NukemichiIcons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            // The toggle at the top of the list and this FAB are the same control; exactly one is on screen.
            AnimatedVisibility(
                visible = isToggleScrolledAway && state.subscriptions.isNotEmpty(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                ConnectFab(
                    state = state,
                    onClick = { viewModel.processIntent(DashboardContract.Intent.ToggleConnection) },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        DashboardContent(
            state = state,
            listState = listState,
            innerPadding = innerPadding,
            onIntent = viewModel::processIntent,
        )
    }

    state.serverDetails?.let { details ->
        ServerDetailsSheet(
            details = details,
            isAdvancedMode = state.isAdvancedMode,
            onDismiss = { viewModel.processIntent(DashboardContract.Intent.ServerDetailsDismissed) },
            onShare = { viewModel.processIntent(DashboardContract.Intent.ShareServerRequested(details.id)) },
            onForget = { viewModel.processIntent(DashboardContract.Intent.ServerForgotten(details.id)) },
            onFingerprintChanged = { value ->
                viewModel.processIntent(DashboardContract.Intent.FingerprintChanged(details.id, value))
            },
            onMuxEnabledChanged = { enabled ->
                viewModel.processIntent(DashboardContract.Intent.MuxEnabledChanged(details.id, enabled))
            },
            onMuxConcurrencyChanged = { value ->
                viewModel.processIntent(DashboardContract.Intent.MuxConcurrencyChanged(details.id, value))
            },
        )
    }

    state.subscriptionEditor?.let { editor ->
        SubscriptionEditorSheet(
            editor = editor,
            onDismiss = { viewModel.processIntent(DashboardContract.Intent.SubscriptionEditorDismissed) },
            onRename = { name -> viewModel.processIntent(DashboardContract.Intent.SubscriptionRenamed(editor.id, name)) },
            onDelete = { viewModel.processIntent(DashboardContract.Intent.SubscriptionDeleted(editor.id)) },
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardContract.State,
    listState: LazyListState,
    innerPadding: PaddingValues,
    onIntent: (DashboardContract.Intent) -> Unit,
) {
    val dimens = MaterialTheme.dimens
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection) + dimens.l,
            end = innerPadding.calculateEndPadding(layoutDirection) + dimens.l,
            top = innerPadding.calculateTopPadding(),
            // Room for the FAB, so the last row can scroll clear of it.
            bottom = innerPadding.calculateBottomPadding() + connectFabClearance,
        ),
    ) {
        item(key = CONNECTION_ITEM_KEY) {
            ConnectionHeader(
                state = state,
                onToggleConnection = { onIntent(DashboardContract.Intent.ToggleConnection) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimens.xl),
            )
        }

        subscriptionItems(
            subscriptions = state.subscriptions,
            onToggleExpanded = { id -> onIntent(DashboardContract.Intent.SubscriptionExpandToggled(id)) },
            onAddServer = { id -> onIntent(DashboardContract.Intent.DeployServerRequested(id)) },
            onShare = { id -> onIntent(DashboardContract.Intent.ShareSubscriptionRequested(id)) },
            onEdit = { id -> onIntent(DashboardContract.Intent.SubscriptionEditorRequested(id)) },
            onSelectServer = { id -> onIntent(DashboardContract.Intent.ServerSelected(id)) },
            onServerDetails = { id -> onIntent(DashboardContract.Intent.ServerDetailsRequested(id)) },
        )

        if (state.isLibraryLoaded) {
            item(key = DEPLOY_ITEM_KEY) {
                DeployServerButton(
                    showEmptyHint = state.subscriptions.isEmpty(),
                    onClick = { onIntent(DashboardContract.Intent.DeployServerRequested(subscriptionId = null)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimens.l),
                )
            }
        }
    }
}
