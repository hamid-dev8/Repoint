package com.repoint.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.dependencies.theme.PurpleGrey80
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.richBlack


@Composable
fun SendTokenScreen(navController: NavController) {

    var walletAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val isButtonEnabled = walletAddress.isNotBlank() && amount.isNotBlank()

    RepointAppBar("send", navController = navController, exp = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(text = "Send Pol", style = RepointTypography.titleMedium)

                OutlinedTextField(
                    value = walletAddress,
                    onValueChange = { walletAddress = it },
                    label = { Text("Wallet Address") },
                    trailingIcon = {
                        Row {
                            TextButton(onClick = {

                            }) { Text("Paste") }
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Scan QR"
                                )
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    trailingIcon = {
                        TextButton(onClick = {}) {
                            Text("Max")
                        }
                    }, modifier = Modifier.fillMaxWidth()
                )

                Text(text = "$ 0.00", style = RepointTypography.bodySmall, color = PurpleGrey80)


            }
            RepointCommonButton(
                text = "Confirm",
                onClick = {

                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    })

}
