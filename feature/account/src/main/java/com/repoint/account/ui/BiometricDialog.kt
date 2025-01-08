package com.repoint.account.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity


@Composable
fun BiometricDialog(
    activity : FragmentActivity,
    onDismiss: () -> Unit,
    onConfirm : () -> Unit
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
                BiometricScreen(
                    activity,
                    onSuccess = {
                        Log.d("BiometricDialog", "Authentication succeeded")
                        onConfirm()
                    },
                    onFailure = {
                        Log.d("BiometricDialog", "Authentication failed")
                        // Handle failure if needed
                        onDismiss()
                    }
                )
            }

        }


    }
}