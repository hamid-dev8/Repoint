package com.repoint.basics.atoms

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.repoint.basics.R
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.pureWhite
import com.repoint.dependencies.theme.transparentColor
import com.repoint.models.sharedmodels.local.MasterWallet

@Composable
@Preview
fun PreviewBalanceScreen() {

    val sampleList = listOf("wallet1", "wallet 2 ", " wallet 3")

    //  BalanceScreen(sampleList, "0.00")

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    items: List<MasterWallet>,
    selectedWalletName: String,
    balance: String,
    onAddWallet: () -> Unit,
    onWalletSelected: (MasterWallet) -> Unit
) {
    var isHiddenBalance by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (items.isNotEmpty()) {
            DropDownList(
                items = items,
                selectedItem = selectedWalletName,
                onItemSelected = { selectedWallet ->
                    //todo change active wallet
                    onWalletSelected(selectedWallet)
                },
                onAddWalletClick = { onAddWallet() },
                Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth()
            )

        }
        Spacer(Modifier.padding(top = 4.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(start = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!isHiddenBalance) balance else balance.replace(Regex("[0-9]"), "*"),
                Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
                style = RepointTypography.displayLarge
            )
            Box(
                modifier = Modifier
                    .size(32.dp) // ✅ fixed size prevents jumping
                    .align(Alignment.CenterVertically)
                    .clickable { isHiddenBalance = !isHiddenBalance },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = isHiddenBalance, label = "eye") { show ->
                    Icon(
                        painter = painterResource(id = if (show) com.repoint.dependencies.R.drawable.eye else com.repoint.dependencies.R.drawable.eye_close),
                        contentDescription = "hide_balance",
                        Modifier
                            .padding(4.dp)
                            .clickable {
                                isHiddenBalance = !isHiddenBalance
                            }
                            .fillMaxSize(),
                        tint = Color.Gray,
                    )
                }
            }
        }
    }
}

// ✅ Updated DropDownList with styled circle icon per wallet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownList(
    items: List<MasterWallet>,
    selectedItem: String?,
    onItemSelected: (MasterWallet) -> Unit,
    onAddWalletClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val selectedWallet = items.find { it.masterWalletId == selectedItem }
    val selectedText = selectedWallet?.let {
        if (it.name.startsWith("wallet", ignoreCase = true))
            it.name + it.walletIndex
        else it.name
    } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                expanded = false
            }
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .padding(8.dp)
                    .fillMaxWidth(),
                label = {
                    Text(
                        "Switch Wallet",
                        textAlign = TextAlign.Center,
                        style = RepointTypography.bodySmall
                    )
                },
                trailingIcon = {
                    WalletIconGradient(
                        name = selectedWallet?.name ?: "",
                        index = selectedWallet?.walletIndex ?: 0
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = transparentColor,
                    unfocusedContainerColor = transparentColor
                )
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(pureWhite)
            ) {
                items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier
                                .background(grayHound)
                                .clip(RoundedCornerShape(4.dp)),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    item.walletIndex?.let {
                                        WalletIconGradient(
                                            name = item.name,
                                            index = it
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (item.name.startsWith(
                                                "wallet",
                                                ignoreCase = true
                                            )
                                        )
                                            item.name + item.walletIndex
                                        else item.name
                                    )
                                }
                            },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            },
                            trailingIcon = if (item.masterWalletId == selectedItem) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        modifier = modifier
                            .background(grayHound)
                            .clip(RoundedCornerShape(4.dp)),
                        text = { Text("➕ Add New Wallet", style = RepointTypography.labelSmall) },
                        onClick = {
                            expanded = false
                            onAddWalletClick()
                        }
                    )
                }
            }
        }
    }
}
