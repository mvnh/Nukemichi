package app.nukemichi.android.feature.wizard.impl.ui.screen

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.nukemichi.android.R
import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.feature.wizard.WizardFlow
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.core.ui.components.ConfirmDialog
import app.nukemichi.android.core.ui.components.LoadingDialog
import app.nukemichi.android.core.ui.components.MessageDialog
import app.nukemichi.android.core.ui.util.EffectHandler
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.WizardContainer
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.WizardTopBar
import app.nukemichi.android.feature.wizard.impl.ui.model.WizardStep
import app.nukemichi.android.feature.wizard.impl.ui.model.rememberWizardState
import app.nukemichi.android.feature.wizard.impl.ui.mvi.ConnectionCheckState
import app.nukemichi.android.feature.wizard.impl.ui.mvi.DeploymentPhase
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.Intent
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.Intent.SetupStrategyChanged
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardViewModel
import app.nukemichi.android.feature.wizard.impl.ui.mvi.isInProgress
import app.nukemichi.android.feature.wizard.impl.ui.mvi.isSshValid
import app.nukemichi.android.feature.wizard.impl.ui.screen.pages.ConfirmationPage
import app.nukemichi.android.feature.wizard.impl.ui.screen.pages.DeploymentProgressPage
import app.nukemichi.android.feature.wizard.impl.ui.screen.pages.ServerDataEntryPage
import app.nukemichi.android.feature.wizard.impl.ui.screen.pages.StrategyChoicePage
import kotlinx.collections.immutable.persistentListOf

private const val PAGE_STRATEGY = 0
private const val PAGE_SERVER_DATA = 1
private const val PAGE_CONFIRMATION = 2
private const val PAGE_DEPLOYMENT = 3
private const val PAGE_COUNT = 4

@Composable
internal fun WizardScreen(
    flow: WizardFlow,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: WizardViewModel = hiltViewModel()
) {
    val title = flow.title()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.processIntent(
            if (result.resultCode == Activity.RESULT_OK) Intent.VpnPermissionGranted
            else Intent.VpnPermissionDenied
        )
    }

    val wizardState = rememberWizardState(pageCount = PAGE_COUNT)
    val navigator = LocalAppNavigator.current
    var isAdvancedSheetOpen by remember { mutableStateOf(false) }

    EffectHandler(viewModel.effect) { effect ->
        when (effect) {
            is WizardContract.Effect.GoToNextPage -> wizardState.next()
            is WizardContract.Effect.NavigateBack -> onNavigateBack()
            is WizardContract.Effect.NavigateToDashboard -> onNavigateToDashboard()
            is WizardContract.Effect.RequestVpnPermission -> {
                VpnService.prepare(context)?.let(vpnPermissionLauncher::launch)
                    ?: viewModel.processIntent(Intent.VpnPermissionGranted)
            }
        }
    }

    val wizardSteps = persistentListOf(
        WizardStep(title = UiText.Resource(R.string.wizard_strategy_choice)) {
            StrategyChoicePage(
                onStrategySelected = { strategy ->
                    viewModel.processIntent(SetupStrategyChanged(strategy))
                },
                selectedStrategy = uiState.setupStrategy,
                isAdvancedMode = uiState.appMode == AppMode.ADVANCED,
            )
        },
        WizardStep(title = UiText.Resource(R.string.wizard_server_data_entry)) {
            ServerDataEntryPage(
                serverAddress = uiState.serverAddress,
                onServerAddressChange = {
                    viewModel.processIntent(Intent.ServerAddressChanged(it))
                },
                authMethod = uiState.serverAuthMethod,
                onAuthMethodChange = {
                    viewModel.processIntent(Intent.ServerAuthMethodChanged(it))
                },
                password = uiState.password.value,
                onPasswordChange = {
                    viewModel.processIntent(Intent.PasswordChanged(it))
                },
                sshKey = uiState.sshKey.value,
                onSshKeyChange = {
                    viewModel.processIntent(Intent.SshKeyChanged(it))
                },
                port = uiState.sshPort,
                onPortChange = {
                    viewModel.processIntent(Intent.SshPortChanged(it))
                },
                username = uiState.username,
                onUsernameChange = {
                    viewModel.processIntent(Intent.UsernameChanged(it))
                },
                sshFingerprint = uiState.sshFingerprint,
                onSshFingerprintChange = { viewModel.processIntent(Intent.SshFingerprintChanged(it)) },
                isAdvancedSheetOpen = isAdvancedSheetOpen,
                onAdvancedSheetDismiss = { isAdvancedSheetOpen = false },
            )
        },
        WizardStep(title = UiText.Resource(R.string.wizard_step_confirmation)) {
            ConfirmationPage(
                serverAddress = uiState.serverAddress,
                sshPort = uiState.sshPort,
                username = uiState.username,
                authMethod = uiState.serverAuthMethod,
                setupStrategy = uiState.setupStrategy,
                hasAcknowledgedRisks = uiState.hasAcknowledgedRisks,
                onAcknowledgeChange = { viewModel.processIntent(Intent.AcknowledgeRisksToggled(it)) },
            )
        },
        WizardStep(title = UiText.Resource(R.string.wizard_step_deployment)) {
            DeploymentProgressPage(
                deployment = uiState.deployment,
                onToggleTerminal = { viewModel.processIntent(Intent.ToggleTerminalVisibility) },
                onRetry = { viewModel.processIntent(Intent.RetryDeployment) },
                onBack = {
                    viewModel.processIntent(Intent.CancelDeployment)
                    previous()
                },
                onFinish = { viewModel.processIntent(Intent.FinishAndStartVpn) },
            )
        }
    )

    val isDeploymentPage = wizardState.currentPage == PAGE_DEPLOYMENT

    BackHandler(enabled = isDeploymentPage) {
        (context as? Activity)?.finish()
    }

    WizardContainer(
        state = wizardState,
        title = title,
        steps = wizardSteps,
        isNextEnabled = when (wizardState.currentPage) {
            PAGE_SERVER_DATA -> uiState.isSshValid
            PAGE_CONFIRMATION -> uiState.hasAcknowledgedRisks
            else -> true
        },
        isLoading = uiState.isLoading || uiState.deployment.phase == DeploymentPhase.InProgress,
        isTopBarVisible = !isDeploymentPage,
        isBottomBarVisible = !isDeploymentPage,
        onNavIconClick = { viewModel.processIntent(Intent.OnCloseWizardClicked) },
        onNextClick = { viewModel.processIntent(Intent.OnNextClicked(wizardState.currentPage)) },
        topBar = {
            WizardTopBar(
                title = title,
                state = wizardState,
                onNavIconClick = { viewModel.processIntent(Intent.OnCloseWizardClicked) },
                onOverflowClick = if (wizardState.currentPage == PAGE_SERVER_DATA) {
                    { isAdvancedSheetOpen = true }
                } else {
                    null
                },
            )
        },
    )

    if (uiState.connectionCheck.isInProgress) {
        LoadingDialog(
            message = UiText.Resource(
                if (uiState.connectionCheck is ConnectionCheckState.StillChecking) {
                    R.string.wizard_connection_check_still_checking
                } else {
                    R.string.wizard_connection_check_checking
                }
            ),
            onCancel = if (uiState.connectionCheck is ConnectionCheckState.StillChecking) {
                { viewModel.processIntent(Intent.CancelConnectionCheck) }
            } else {
                null
            },
        )
    }

    val connectionError = uiState.connectionCheck as? ConnectionCheckState.Failed
    if (connectionError != null) {
        val detail = connectionError.reason.asString()
        val body = stringResource(R.string.wizard_connection_check_failed_message) +
            (detail.takeIf(String::isNotBlank)?.let { "\n\n${stringResource(R.string.wizard_connection_check_failed_detail_prefix)}$it" } ?: "")

        MessageDialog(
            title = UiText.Resource(R.string.wizard_connection_check_failed_title),
            body = UiText.Raw(body),
            onConfirm = { viewModel.processIntent(Intent.DismissConnectionErrorDialog) },
        )
    }

    val untrustedHost = uiState.connectionCheck as? ConnectionCheckState.UntrustedHost
    if (untrustedHost != null) {
        ConfirmDialog(
            title = UiText.Resource(R.string.wizard_untrusted_host_title),
            body = UiText.Resource(R.string.wizard_untrusted_host_body, untrustedHost.fingerprint),
            confirmText = UiText.Resource(R.string.wizard_untrusted_host_trust),
            onConfirm = { viewModel.processIntent(Intent.TrustHostAndRetry(untrustedHost.fingerprint)) },
            onDismiss = { viewModel.processIntent(Intent.DismissConnectionErrorDialog) },
        )
    }
}

private fun WizardFlow.title(): UiText = when (this) {
    WizardFlow.DEPLOY_SERVER -> UiText.Resource(R.string.wizard_title_deploy_server)
    WizardFlow.ADD_TO_SUBSCRIPTION -> UiText.Resource(R.string.wizard_title_add_to_subscription)
    WizardFlow.IMPORT_URI -> UiText.Resource(R.string.wizard_title_import_uri)
}
