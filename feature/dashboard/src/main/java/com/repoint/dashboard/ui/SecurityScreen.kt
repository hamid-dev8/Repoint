package com.repoint.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.SecuritySettingItem
import com.repoint.basics.atoms.TextSettingItem
import com.repoint.dashboard.AutoLockOption
import com.repoint.dashboard.LockMethod
import com.repoint.dashboard.SecurityViewModel

@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: SecurityViewModel = hiltViewModel<SecurityViewModel>()
) {

    val usePasscode by viewModel.isPasscodeEnabled.collectAsState()
    val currentLockMethod by viewModel.lockMethod.collectAsState()
    val autoLockMinutes by viewModel.autoLockMinutes.collectAsState()
    val isTransactionSigningEnabled by viewModel.isTransactionSigningEnabled.collectAsState()
    val isScannerEnabled by viewModel.isScannerEnabled.collectAsState()
    var showLockMethodDialog by remember { mutableStateOf(false) }
    var showAutoLockDialog by remember { mutableStateOf(false) }

    val lockMethodText = if (currentLockMethod == LockMethod.PASSCODE.name) {
        "Passcode"
    } else {
        "Biometric"
    }
    val autoLockText = AutoLockOption.fromMinutes(autoLockMinutes).label

    RepointAppBar(title = "Security", navController = navController, exp = {_,_,_ ->

        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

            SecuritySettingItem(
                title = "Security Scanner",
                description = "Show Warnings for high-risk transactions.",
                checked = isScannerEnabled,
                onToggle = { viewModel.toggleScanner(it) }
            )

            SecuritySettingItem(
                title = "Passcode",
                checked = usePasscode,
                onToggle = { enabled ->
                    viewModel.togglePasscode(enabled)
                }
            )

            TextSettingItem(
                title = "Auto-lock",
                subtitle = autoLockText,
                onClick = { showAutoLockDialog = true }
            )

            TextSettingItem(
                title = "Lock method",
                subtitle = lockMethodText,
                onClick = { showLockMethodDialog = true }
            )

            SecuritySettingItem(
                title = "Transaction sign-in",
                description = "Ask for approval ahead of transactions.",
                checked = isTransactionSigningEnabled,
                onToggle = { viewModel.toggleTransactionSigning(it) }
            )

        }

        if (showAutoLockDialog) {
            AlertDialog(
                onDismissRequest = { showAutoLockDialog = false },
                title = { Text("Choose auto-lock") },
                text = {
                    Column {
                        AutoLockOption.values().forEach { option ->
                            TextButton(
                                onClick = {
                                    viewModel.setAutoLockMinutes(option.minutes)
                                    showAutoLockDialog = false
                                }
                            ) {
                                Text(option.label)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAutoLockDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showLockMethodDialog) {
            AlertDialog(
                onDismissRequest = { showLockMethodDialog = false },
                title = { Text("Choose lock method") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                viewModel.setLockMethod(LockMethod.PASSCODE)
                                showLockMethodDialog = false
                            }
                        ) { Text("Passcode") }
                        TextButton(
                            onClick = {
                                viewModel.setLockMethod(LockMethod.BIOMETRIC)
                                showLockMethodDialog = false
                            }
                        ) { Text("Biometric") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLockMethodDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    })

}