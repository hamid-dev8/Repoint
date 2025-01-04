package com.repoint.basics.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.RepointTheme


@Composable
@Preview
fun ButtonPreview() {
    RepointCommonButton("Confirm", onClick = {})
    RepointCommonButton("Confirm", onClick = {})
}

@Composable
fun RepointCommonButton(
    text: String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    margin : Dp = 8.dp
) {
    RepointTheme {

        Button(
            onClick = {
                onClick()
            },
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = margin)
                .height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}