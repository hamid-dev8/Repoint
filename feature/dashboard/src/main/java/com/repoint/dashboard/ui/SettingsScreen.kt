package com.repoint.dashboard.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.dashboard.ThemeViewModel
import com.repoint.dashboard.WalletConnectViewModel
import com.repoint.dashboard.activity.QrScannerActivity
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.models.sharedmodels.remote.PendingProposal
import com.repoint.models.sharedmodels.remote.ActiveSession


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ThemeViewModel = hiltViewModel(),
    walletConnectViewModel: WalletConnectViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val walletConnectUiState by walletConnectViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showWalletConnectSheet by remember { mutableStateOf(false) }
    val walletConnectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val approvalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeSession = walletConnectUiState.activeSession
    val hasConnectedWallet = activeSession != null || walletConnectUiState.status.startsWith("Connected to", ignoreCase = true)
    val connectedDisplayName = activeSession?.dAppName ?: walletConnectUiState.status.removePrefix("Connected to ").trim()

    LaunchedEffect(Unit) {
        walletConnectViewModel.refreshActiveSession()
    }

    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanned = result.data?.getStringExtra("SCANNED_ADDRESS")
            if (!scanned.isNullOrBlank()) {
                Log.d("WC_SCAN","the scanned value is : $scanned")
                walletConnectViewModel.pairWallet(scanned)
            } else {
                Log.d("WC_SCAN","the QR scanned but no walletConnect URI was found")
                walletConnectViewModel.updateStatus("QR scanned but no WalletConnect URI was found.")
            }
        } else {
            Log.d("WC_SCAN","Scanner canceled")
            walletConnectViewModel.updateStatus("Scanner canceled.")
        }
    }

    RepointAppBar("Settings", navController = navController, exp = { _, _, _ ->

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                SettingItem(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Wallets",
                    onClick = { navController.navigate("wallets") })
            }
            item {
                SettingToggleItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) })
            }
            item {
                SettingItem(
                    icon = Icons.Rounded.Link,
                    title = if (hasConnectedWallet) "WalletConnect connected" else "WalletConnect",
                    subtitle = if (hasConnectedWallet) connectedDisplayName.ifBlank { "Connected" } else "Connect a dApp",
                    isConnected = hasConnectedWallet,
                    onClick = {
                        showWalletConnectSheet = true
                    }
                )
            }
            item {
                SettingItem(icon = Icons.Rounded.Security, title = "Security", onClick = {
                    navController.navigate("security")
                })
            }
            item {
                SettingItem(icon = Icons.Rounded.Notifications, title = "Notifications", onClick = {})
            }
        }

        // ── WalletConnect QR / status sheet ──────────────────────────────────
        var showDisconnectDialog by remember { mutableStateOf(false) }

        if (showWalletConnectSheet) {
            ModalBottomSheet(
                onDismissRequest = { showWalletConnectSheet = false },
                sheetState = walletConnectSheetState
            ) {
                WalletConnectBottomSheetContent(
                    status = walletConnectUiState.status,
                    isPairing = walletConnectUiState.isPairing,
                    error = walletConnectUiState.error,
                    activeSession = walletConnectUiState.activeSession,
                    onDismiss = { showWalletConnectSheet = false },
                    onScanQr = {
                        qrScannerLauncher.launch(Intent(context, QrScannerActivity::class.java))
                    },
                    onDisconnect = { showDisconnectDialog = true }
                )
            }
        }

        if (showDisconnectDialog) {
            AlertDialog(
                onDismissRequest = { showDisconnectDialog = false },
                title = { Text("Disconnect Session?") },
                text = {
                    Text(
                        "This will end your WalletConnect session with ${walletConnectUiState.activeSession?.dAppName ?: "the dApp"}."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        walletConnectViewModel.disconnect()
                        showDisconnectDialog = false
                        showWalletConnectSheet = false
                    }) {
                        Text("Disconnect", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDisconnectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ── Session proposal approval sheet ──────────────────────────────────
        val pendingProposal = walletConnectUiState.pendingProposal
        if (pendingProposal != null) {
            ModalBottomSheet(
                onDismissRequest = { walletConnectViewModel.rejectPendingProposal() },
                sheetState = approvalSheetState
            ) {
                SessionProposalBottomSheet(
                    proposal = pendingProposal,
                    onApprove = {
                        walletConnectViewModel.approvePendingProposal()
                        // showWalletConnectSheet = false
                    },
                    onReject = { walletConnectViewModel.rejectPendingProposal() }
                )
            }
        }
    })
}

// ── Session proposal sheet ────────────────────────────────────────────────────

@Composable
fun SessionProposalBottomSheet(
    proposal: PendingProposal,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Connect to dApp", style = RepointTypography.titleMedium)

        HorizontalDivider()

        Text(text = proposal.name, style = RepointTypography.titleSmall)
        Text(text = proposal.url, style = RepointTypography.bodyMedium)

        if (proposal.chains.isNotEmpty()) {
            Text(
                text = "Chains: ${proposal.chains.joinToString()}",
                style = RepointTypography.bodyMedium
            )
        }

        Text(
            text = "This dApp is requesting access to your wallet. Only approve if you trust this source.",
            style = RepointTypography.bodyMedium
        )

        HorizontalDivider()

        RepointCommonButton(
            text = "Approve",
            onClick = onApprove,
            fullWidth = true,
            margin = 0.dp,
            buttonHeight = 52.dp
        )

        RepointCommonButton(
            text = "Reject",
            onClick = onReject,
            fullWidth = true,
            margin = 0.dp,
            buttonHeight = 52.dp
        )
    }
}

// ── QR / status sheet ─────────────────────────────────────────────────────────

@Composable
fun WalletConnectBottomSheetContent(
    status: String,
    isPairing: Boolean,
    error: String?,
    activeSession: ActiveSession?,
    onDismiss: () -> Unit,
    onScanQr: () -> Unit,
    onDisconnect: () -> Unit = {},
) {
    if (activeSession != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header row ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "WalletConnect", style = RepointTypography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                // Green "Connected" badge
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1DB954).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● Connected",
                        style = RepointTypography.labelSmall,
                        color = Color(0xFF1DB954)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── dApp info ────────────────────────────────────────────────
            Text(text = "Connected dApp", style = RepointTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = activeSession.dAppName, style = RepointTypography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = activeSession.dAppUrl, style = RepointTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Wallet address", style = RepointTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${activeSession.connectedAddress.take(6)}...${activeSession.connectedAddress.takeLast(4)}",
                style = RepointTypography.titleSmall
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()

            // ── Manage session row ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: navigate to session detail / manage */ }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = "Manage session",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Manage session", style = RepointTypography.titleSmall,
                    modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))

            // ── Disconnect button ────────────────────────────────────────
            RepointCommonButton(
                text = "Disconnect",
                onClick = onDisconnect,
                fullWidth = true,
                margin = 0.dp,
                buttonHeight = 52.dp
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Connect Wallet", style = RepointTypography.titleMedium)

            Text(
                text = "Scan a WalletConnect QR code from a dApp to connect your wallet.",
                style = RepointTypography.bodyMedium
            )

            if (status.isNotBlank()) {
                Text(text = status, style = RepointTypography.bodyMedium)
            }

            if (!error.isNullOrBlank()) {
                Text(text = "⚠ $error", style = RepointTypography.bodyMedium)
            }

            RepointCommonButton(
                text = if (isPairing) "Pairing..." else "Scan QR Code",
                onClick = onScanQr,
                fullWidth = true,
                margin = 0.dp,
                buttonHeight = 52.dp,
                enabled = !isPairing
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel")
            }
        }
    }
}

// ── helpers ───────────────────────────────────────────────────────────────────

private tailrec fun Context.findActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> {
        Log.d("WC_SCAN", "findActivity hit FragmentActivity: ${this::class.java.simpleName}")
        this
    }
    is ContextWrapper -> {
        Log.d("WC_SCAN", "findActivity unwrap: ${this::class.java.simpleName}")
        baseContext.findActivity()
    }
    else -> {
        Log.d("WC_SCAN", "findActivity failed on context: ${this::class.java.simpleName}")
        null
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isConnected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isConnected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = RepointTypography.titleSmall,
                color = if (isConnected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = RepointTypography.bodyMedium,
                    color = if (isConnected) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = RepointTypography.titleSmall)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
