package com.repoint.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.dashboard.ThemeViewModel
import com.repoint.dependencies.theme.RepointTypography


@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ThemeViewModel = hiltViewModel()
) {

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    RepointAppBar("Settings", navController, exp = {

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            item {
                SettingItem(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Wallets",
                    onClick = {
                        navController.navigate("wallets")
                    })
            }

            item {
                SettingToggleItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) })
            }

            item {
                SettingItem(icon = Icons.Rounded.Link, title = "Wallet Connect", onClick = {
                    //todo walletConnect
                })
            }

            item {
                SettingItem(
                    icon = Icons.Rounded.Security, title = "Security", onClick = {
                       navController.navigate("security")
                    })
            }

            item {
                SettingItem(icon = Icons.Rounded.Notifications, title = "Notifications", onClick = {
                    //todo add notifications
                })
            }


        }


    })

}

@Composable
fun SettingItem(icon: ImageVector, title: String, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = RepointTypography.titleSmall)
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
        Spacer(modifier = Modifier.weight(1f)) // Pushes the Switch to the right
        Switch(checked = checked, onCheckedChange = onCheckedChange)

    }


}