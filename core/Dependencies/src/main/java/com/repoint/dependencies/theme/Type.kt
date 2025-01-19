package com.repoint.dependencies.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.R


val CustomFontFamily = FontFamily(
    Font(resId = R.font.fonts)
)

// Set of Material typography styles to start with
val RepointTypography =
    Typography(
        displayLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W900, //black
            fontSize = 32.sp,
            lineHeight = 34.sp,
            letterSpacing = 1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W700, //bold
            fontSize = 24.sp,
            lineHeight = 28.sp,
            letterSpacing = 1.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W400, //regular
            fontSize = 22.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.sp
        ),
        bodySmall = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W300, //light
            fontSize = 18.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp
        )

        /* Other default text styles to override
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
        */
    )

