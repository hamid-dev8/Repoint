package com.repoint.basics.atoms

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.RepointTheme
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.repointBlue
import com.repoint.dependencies.theme.repointOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
@Preview
fun ButtonPreview() {
    RepointCommonButton("Confirm", onClick = {})
    RepointCommonButton("Confirm", onClick = {})
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun RepointCommonButton(
    text: String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    margin : Dp = 0.dp,
    shape: Shape = RoundedCornerShape(50.dp),
    fullWidth: Boolean = true,
    buttonHeight: Dp = 52.dp
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )
    Button(
        onClick = {
           scope.launch {
               delay(150)
               onClick()
            }
        },
        enabled = enabled,
        shape = shape,
        interactionSource = interactionSource,
        modifier = modifier
            .graphicsLayer {
               scaleX = scale
               scaleY = scale
            }
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .clip(shape)
            .padding(horizontal = 14.dp, vertical = margin)
            .height(buttonHeight),
    ) {
        Text(text = text, style = RepointTypography.bodyLarge)
    }

}


@Composable
fun WalletButton(
    text: String,
    subText: String,
    backgroundColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit)
{

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "wallet_button_scale"
    )



    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(65.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundColor, shape = RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // or null if you want no ripple
                onClick = { onClick() }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.White,
                style = RepointTypography.bodySmall
            )
            Text(
                text = subText,
                fontSize = 12.sp,
                color = Color.White,
                style = RepointTypography.bodyMedium
            )
        }

        // Arrow Icon
        Icon(
            imageVector = Icons.Filled.ArrowForwardIos,
            contentDescription = "Arrow Icon",
            tint = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWalletButtons() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WalletButton(
            text = "Create new wallet",
            subText = "Secret phrase or Swift wallet",
            backgroundColor = repointOrange, // Orange
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Icon",
                    tint = Color(0xFFFFA726)
                )
            },
            onClick = { /* Handle click */ }
        )

        WalletButton(
            text = "Add existing wallet",
            subText = "Import, restore or view-only",
            backgroundColor = repointBlue, // Blue
            icon = {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download Icon",
                    tint = Color(0xFF1565C0)
                )
            },
            onClick = { /* Handle click */ }
        )
    }
}


