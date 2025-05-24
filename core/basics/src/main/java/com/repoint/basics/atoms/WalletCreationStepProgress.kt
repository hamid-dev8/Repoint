package com.repoint.basics.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.RepointTypography

@Composable
fun WalletCreationStepProgress(
    currentStep: Int,
    userExists: Boolean,
    modifier: Modifier = Modifier
) {
    // Define step labels based on user existence
    val stepLabels = if (userExists) {
        listOf("Secret phrase", "Confirm\nSecret phrase")
    } else {
        listOf("Secret phrase", "Confirm\nSecret phrase", "Set password", "Confirm\npassword")
    }
    val totalSteps = stepLabels.size

    val circleSize = 32.dp
    val lineHeight = 2.dp
    val lineColorActive = Color(0xFFFF6600)
    val lineColorInactive = Color.Black
    val spacerWidth = 8.dp
    val labelBoxWidth = circleSize * 1.4f

    Column(modifier = modifier.fillMaxWidth()) {
        // Circles + lines row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step < currentStep
                val isActive = step == currentStep

                val circleColor = if (isCompleted || isActive) lineColorActive else Color.Transparent
                val borderColor = if (isCompleted || isActive) lineColorActive else Color.Black
                val textColor = if (isCompleted || isActive) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .border(0.5.dp, borderColor, CircleShape)
                        .background(circleColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.toString(),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (step < totalSteps) {
                    Spacer(modifier = Modifier.width(spacerWidth))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(lineHeight)
                            .background(
                                if (step < currentStep) lineColorActive else lineColorInactive
                            )
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(spacerWidth))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stepLabels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier.width(labelBoxWidth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (index + 1 == currentStep) lineColorActive else Color.Black,
                        textAlign = TextAlign.Center,
                        style = RepointTypography.labelSmall,
                        maxLines = 2
                    )
                }
            }
        }
    }
}






