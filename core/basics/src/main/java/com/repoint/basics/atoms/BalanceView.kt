package com.repoint.basics.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.transparentColor
import com.repoint.models.sharedmodels.local.RepointWallet

@Composable
@Preview
fun PreviewBalanceScreen() {

    val sampleList = listOf("wallet1", "wallet 2 ", " wallet 3")

    //  BalanceScreen(sampleList, "0.00")

}

@Composable
fun BalanceScreen(items: List<RepointWallet>, balance: String) {
    var isHiddenBalance by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (items.isNotEmpty()) {
            DropDownList(items, items[0].name, onItemSelected = {

            }, Modifier.align(Alignment.CenterHorizontally))

        }
        Spacer(Modifier.padding(top = 8.dp))

        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = if (!isHiddenBalance) balance else balance.replace(Regex("[0-9]"), "*") ,
                Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
                style = RepointTypography.displaySmall
            )
            Icon(
                imageVector = Icons.Rounded.RemoveRedEye,
                contentDescription = "hide_balance",
                Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterVertically).clickable {
                        isHiddenBalance = !isHiddenBalance
                    },
                tint = Color.Gray,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownList(
    items: List<RepointWallet>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) } // Controls the dropdown visibility
    var selectedText by remember { mutableStateOf(selectedItem ?: "") } // Stores the selected item

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.width(200.dp)
    ) {
        // TextField for dropdown
        TextField(
            value = selectedText,
            onValueChange = { /* No direct editing */ },
            modifier = Modifier
                .menuAnchor()
                .padding(8.dp),
            label = {
                Text(
                    "switch wallet",
                    textAlign = TextAlign.Center,
                    style = RepointTypography.bodySmall
                )
            },
            readOnly = true, // Ensures the field cannot be edited manually
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = transparentColor,
                unfocusedContainerColor = transparentColor,
                unfocusedIndicatorColor = transparentColor,
            )
        )
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier.wrapContentWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.name.toString(), Modifier) },
                    onClick = {
                        selectedText = item.name.toString()
                        onItemSelected(item.name.toString())
                        expanded = false
                    }
                )
            }
        }
    }

}