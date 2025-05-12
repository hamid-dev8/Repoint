package com.repoint.basics.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RepointTypography
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun WalletIconGradient(name: String, index: Int) {
    val gradient = remember(index) {
        when (index % 5) {
            0 -> Brush.linearGradient(listOf(Color(0xFF42A5F5), Color(0xFF478DE0)))
            1 -> Brush.linearGradient(listOf(Color(0xFFFFA726), Color(0xFFFF7043)))
            2 -> Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF43A047)))
            3 -> Brush.linearGradient(listOf(Color(0xFFAB47BC), Color(0xFF7E57C2)))
            else -> Brush.linearGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFDD835)))
        }
    }

    val firstLetter = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(32.dp)
            .clip(CircleShape)
            .background(gradient)
    ) {
        Text(
            text = firstLetter,
            color = Color.White,
            style = RepointTypography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
