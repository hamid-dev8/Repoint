package com.repoint.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repoint.dependencies.theme.RepointTypography

@Composable
fun SendSuccessScreen(txHash: String, navController: NavController) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✅ Transaction Sent!", style = RepointTypography.titleMedium)
        Spacer(Modifier.height(16.dp))
        SelectionContainer {
            Text("TxHash:\n$txHash", textAlign = TextAlign.Center, style = RepointTypography.titleSmall)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            navController.navigate("home") {
                popUpTo(0) // Clears full backstack
            }
        }) {
            Text("Back to Home")
        }
    }
}
