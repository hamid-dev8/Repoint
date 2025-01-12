package com.repoint.basics.atoms

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleEditText(
    title: String,
    height: Int = 56,
    isItpasteNeed: Boolean,
    userInput: String,
    onInputChange: (String) -> Unit,
    onPaste: () -> Unit
) {
    val context = LocalContext.current



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, Modifier.fillMaxWidth(), color = Color.Gray)


        OutlinedTextField(
            value = userInput,
            onValueChange = onInputChange,
            label = { Text("Enter text", fontSize = 14.sp) },
            placeholder = {
                Text(
                    text = "Type something...",
                    fontSize = 16.sp
                )
            },

            trailingIcon = {
                if (isItpasteNeed) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        TextButton(onClick = {
                            // Paste text from clipboard
                           onPaste()
                        }) {
                            Text(
                                text = "Paste",
                            )
                        }
                    }
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    Toast.makeText(context, "Done editing!", Toast.LENGTH_SHORT).show()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
