package com.repoint.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repoint.dependencies.theme.RepointTypography

@Composable
fun SendErrorScreen(errorMessage: String?, navController: NavController) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("❌ Error Sending", style = RepointTypography.titleMedium, color = Color.Red)
        Spacer(Modifier.height(16.dp))
        Text(errorMessage ?: "Unknown error")
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            navController.navigate("home") {
                popUpTo(0)
            }
        }) {
            Text("Back to Home")
        }
    }
}
