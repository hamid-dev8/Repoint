package com.repoint.basics.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.RepointTypography

@Composable
fun StyledBalanceText(balance: String, isHiddenBalance: Boolean) {
    val displayText = if (!isHiddenBalance) balance else "••••••••"

    val annotatedString = buildAnnotatedString {
        if (!isHiddenBalance && displayText.contains('.')) {
            val parts = displayText.split('.')
            append(parts[0] + ".") // whole part plus decimal point

            // Append cents with smaller size and different color
            withStyle(
                style = SpanStyle(
                    fontSize = 26.sp,  // smaller size for cents
                    color = Color.Gray  // different color for cents
                )
            ) {
                append(parts.getOrNull(1) ?: "")
            }
        } else {
            append(displayText) // show all text with default style if hidden or no decimal
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp),
        style = RepointTypography.bodyMedium,
        fontWeight = FontWeight.W700,
        fontSize = 42.sp
    )
}
