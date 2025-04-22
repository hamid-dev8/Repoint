package com.repoint.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.SecuritySettingItem
import com.repoint.basics.atoms.TextSettingItem
import com.repoint.dashboard.SecurityViewModel

@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: SecurityViewModel = hiltViewModel<SecurityViewModel>()
) {

    RepointAppBar(title = "Security", navController, exp = {

        Column(Modifier.padding(horizontal = 16.dp)) {

            SecuritySettingItem(
                title = "Security Scanner",
                description = "Show Warnings for high-risk transactions.",
                checked = viewModel.isScannerEnabled.value,
                onToggle = { viewModel.toggleScanner(it) }
            )

            SecuritySettingItem(
                title = "passcode",
                checked = viewModel.isPasscodeEnabled.value,
                onToggle = { viewModel.togglePasscode(it) }
            )

            TextSettingItem(
                title = "Auto-lock",
                subtitle = "immediate",
                onClick = {/*show bottom sheet : Immediate / 1min / 5 min*/ }
            )

            TextSettingItem(
                title = "Lock method",
                subtitle = "Passcode",
                onClick = { /*show Dialog : FingerPrint / Passcode */ }
            )

            SecuritySettingItem(
                title = "Transaction sign-in",
                description = "Ask for approval ahead of transactions.",
                checked = viewModel.isTransactionSigningEnabled.value,
                onToggle = { viewModel.toggleTransactionSigning(it) }
            )

        }

    })

}