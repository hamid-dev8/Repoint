package com.repoint.basics.atoms

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.RepointTypography

@Composable
fun SimpleEditText(
    title: String,
    height: Int = 56,
    isItpasteNeed: Boolean,
    userInput: String,
    onInputChange: (String) -> Unit,
    onPaste: () -> Unit,
    suggestions: List<String> = emptyList(),
    onSuggestionClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var textFieldValue by remember(userInput) {
        mutableStateOf(TextFieldValue(text = userInput, selection = TextRange(userInput.length)))
    }

    LaunchedEffect(userInput) {
        if (textFieldValue.text != userInput) {
            textFieldValue = TextFieldValue(text = userInput, selection = TextRange(userInput.length))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            Modifier.fillMaxWidth(),
            color = Color.Gray,
            style = RepointTypography.labelSmall
        )

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onInputChange(newValue.text)
            },
            label = { Text("Enter text", fontSize = 14.sp) },
            placeholder = {
                Text(
                    text = "Type something...",
                    style = RepointTypography.labelMedium
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
                .height(height.dp), shape = RoundedCornerShape(12.dp)
        )

        if (suggestions.isNotEmpty()) {
            val rows = suggestions.chunked(2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { suggestion ->
                            RepointCommonButton(
                                text = suggestion,
                                onClick = {
                                    val nextWord = userInput.trimEnd().split(Regex("\\s+"))
                                        .dropLast(1)
                                        .plus(suggestion)
                                        .joinToString(" ") + " "
                                    onSuggestionClick(nextWord)
                                    onInputChange(nextWord)
                                    textFieldValue = TextFieldValue(text = nextWord, selection = TextRange(nextWord.length))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(18.dp),
                                fullWidth = false,
                                buttonHeight = 36.dp,
                                margin = 0.dp,
                            )
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
