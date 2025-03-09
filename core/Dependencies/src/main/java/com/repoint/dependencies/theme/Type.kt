package com.repoint.dependencies.theme

import androidx.compose.material3.Text
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

    //w300 light , w500 medium , w600 semibold ,
)

// Set of Material typography styles to start with
val RepointTypography =
    Typography(
        displayLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W200, //black
            fontSize = 32.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W300,
            fontSize = 30.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W300,
            fontSize = 28.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 30.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 26.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 22.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W400, //bold
            fontSize = 15.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W300, //regular
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W300, //light
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.sp
        ),
        labelLarge = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        )

    )

