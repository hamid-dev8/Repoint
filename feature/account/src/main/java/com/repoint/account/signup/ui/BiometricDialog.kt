package com.repoint.account.signup.ui

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoint.account.BioViewModel
import com.repoint.dependencies.theme.PurpleGrey40
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.repointOrange


@Composable
fun BiometricDialog(
    activity: FragmentActivity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Authenticate", style = MaterialTheme.typography.titleLarge)

                // Include BiometricScreen here

            }

        }


    }
}


@Composable
fun BiometricalDialog(
    activity: FragmentActivity,
    onDenyClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit, // New parameter for dismissing the dialog
    fingerprintIcon: Painter,
    viewModel: BioViewModel = hiltViewModel(),
// Replace this with your fingerprint image resource
) {

    LaunchedEffect(Unit) {
        viewModel.checkBiometricAvailability()
    }

    val isBiometricAvailable by viewModel.isBiometricAvailable.collectAsState()

    Dialog(onDismissRequest = { onDismissRequest() } , properties = DialogProperties(true,true,false)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .background(PurpleGrey40.copy(alpha = 0.2f))
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentWidth()
                    .background(Color.White, RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = fingerprintIcon,
                    contentDescription = "Fingerprint Icon",
                    modifier = Modifier
                        .size(256.dp)
                        .padding(top = 24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Biometric Login",
                    style = RepointTypography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan your fingerprint for secure\nand convenient access to your account.",
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = RepointTypography.labelSmall

                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (isBiometricAvailable) {
                        Button(
                            onClick = { onDenyClick() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(50.dp),
                        ) {
                            Text(
                                text = "Deny",
                                color = Color.Black,
                                style = RepointTypography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                activity.let {
                                    viewModel.authenticate(it, onConfirmClick, onDenyClick)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = repointOrange
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(50.dp),
                        ) {
                            Text(
                                text = "Confirm",
                                color = Color.White,
                                style = RepointTypography.bodySmall
                            )
                        }
                    } else {
                        Button(
                            onClick = { onDenyClick() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(50.dp),
                        ) {
                            Text(
                                text = "FingerPrint not available!",
                                color = Color.Black,
                                style = RepointTypography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
@Preview
fun PreviewDialog() {
    /*  val activity = FragmentActivity()
      BiometricalDialog(
          activity,
          onDenyClick = {},
          onConfirmClick = {},
          fingerprintIcon = painterResource(com.repoint.dependencies.R.drawable.biometric_ic),
      )*/
}
