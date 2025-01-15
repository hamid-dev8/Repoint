package com.repoint.basics.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RepointTypography
import org.bouncycastle.math.raw.Mod

@Composable
@Preview
fun PreviewBalanceScreen() {

    val sampleList = listOf("wallet1" , "wallet 2 " , " wallet 3")

    BalanceScreen(sampleList,"0.00")

}

@Composable
fun BalanceScreen(items : List<String>,balance: String) {

    Box(Modifier.padding(26.dp), contentAlignment = Alignment.BottomCenter) {

        DropDownList(items,"Wallet", onItemSelected = {

        },Modifier.align(Alignment.TopCenter))


        Spacer(Modifier.padding(top = 12.dp))

        Row {
            Text(
                balance,
                Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterVertically),
                style = RepointTypography.displayLarge
            )
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "hide_balance",
                Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownList(items : List<String>,selectedItem : String?,onItemSelected : (String) -> Unit,modifier : Modifier = Modifier){

    var expanded by remember { mutableStateOf(false) } // Controls the dropdown visibility
    var selectedText by remember { mutableStateOf(selectedItem ?: "") } // Stores the selected item

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        // TextField for dropdown
        OutlinedTextField(
            value = selectedText,
            onValueChange = { /* No direct editing */ },
            modifier = Modifier
                .menuAnchor().padding(56.dp),
            label = { Text("Select wallet") },
            readOnly = true, // Ensures the field cannot be edited manually
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier.width(12.dp)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item,Modifier) },
                    onClick = {
                        selectedText = item
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }

}