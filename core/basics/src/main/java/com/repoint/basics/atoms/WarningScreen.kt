package com.repoint.basics.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.repointLightOrange
import com.repoint.dependencies.theme.repointOrange


@Composable
fun WarningBanner(message: String,modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth() // Ensure the banner respects parent width constraints
            .padding(horizontal = 16.dp) // Add padding to make it narrower
            .wrapContentWidth()
            .background(
                shape = RoundedCornerShape(8.dp),
                color = repointLightOrange
            )
            .padding(horizontal = 16.dp, vertical = 8.dp), // Inner padding for content
        verticalAlignment = Alignment.CenterVertically // Align items vertically // Light orange background ,
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Warning",
            tint = repointOrange, // Darker orange icon tint
            modifier = modifier.size(24.dp).align(Alignment.CenterVertically)
        )
        Spacer(modifier.width(1.dp)) // Space between icon and text
        Text(
            text = message,
            color = repointOrange, // Darker orange text color
            textAlign = TextAlign.Center,
            style = RepointTypography.labelSmall,
            modifier = modifier.align(Alignment.CenterVertically)
            //modifier = Modifier.weight(1f) // Text takes available space
        )
        //Optional > Icon
        /*Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "Go",
            tint = Color(0xFFE65100), // Darker orange icon tint
            modifier = Modifier.size(24.dp)
        )*/
    }
}