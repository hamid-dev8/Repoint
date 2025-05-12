package com.repoint.basics.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.pureWhite
import com.repoint.dependencies.theme.richBlack

@Composable
@Preview
fun PreviewIconButton() {

/*    CircularButtonWithText(
        icon = Icons.Default.KeyboardArrowUp,
        text = "Send",
        onClick = {},
        modifier = Modifier.padding(18.dp)
    )*/

}


@Composable
fun CircularButtonWithText(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 64.dp,
    iconSize: Dp = 32.dp,
    textStyle: TextStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
    backgroundColor : Color = pureWhite
) {

    val padding = ((buttonSize - iconSize) / 2).coerceAtLeast(0.dp)


    Box() {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onClick,
                shape = CircleShape,
                modifier = Modifier.size(buttonSize),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                contentPadding = PaddingValues(padding) // ✅ dynamic padding
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize).padding(4.dp),
                    tint = richBlack
                )
            }
            Spacer(Modifier.padding(bottom = 8.dp))
        }
        //Text Below the Button
        Text(
            text = text,
            style = textStyle,
            modifier = Modifier.padding(top = 8.dp).align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun IconWithText(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    spacing: Dp = 8.dp // Space between icon and text
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(spacing))
        Text(
            text = text,
            style = RepointTypography.titleSmall
        )
    }
}
