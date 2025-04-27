package com.repoint.basics.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.repointOrange
import kotlin.random.Random

@Composable
fun LoaderAnimation(
    modifier: Modifier = Modifier,
    dotColor: Color = repointOrange,
    dotSize: Dp = 14.dp, // base size of each dot
    space: Dp = 22.dp // space between dots
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Animate different scales for each dot
    val animations = List(9) { index ->
        val randomDuration = Random.nextInt(800, 2000) // 🔥 Each dot random speed
        val randomTargetScale = Random.nextFloat().coerceIn(1f, 1.3f) // 🔥 Random max size

        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = randomTargetScale,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing, // smooth up & down
                    delayMillis = index * 100 // small delay per dot
                ),
                repeatMode = RepeatMode.Reverse // 🔥 Very important
            ), label = ""
        )
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        for (row in 0 until 3) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until 3) {
                    val scale = animations[row * 3 + col].value
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(color = dotColor, shape = CircleShape)
                            .padding(space)
                    )
                }
            }
        }
    }
}
