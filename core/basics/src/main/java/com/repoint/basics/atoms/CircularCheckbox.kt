package com.repoint.basics.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun CheckboxPreview() {
    var isChecked by remember { mutableStateOf(true) }

    RepointCheckbox(
        isChecked,
        onCheckedChange = { isChecked = it },
        size = 32.dp,
        borderColor = Color.Black,
        checkedColor = Color.Green,
        uncheckedColor = Color.LightGray
    )

}


@Composable
fun RepointCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    borderColor: Color,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = Color.Transparent
) {

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isChecked) checkedColor else uncheckedColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .clickable { onCheckedChange(!isChecked) },
        contentAlignment = Alignment.Center
    ){
        if (isChecked){
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "checked",
                tint = Color.White,
                modifier = Modifier.size(size/2)
            )
        }
    }

}