package com.repoint.dependencies.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = repointOrange,
    secondary = RoseWood,
    tertiary = PowderBlue,
    tertiaryContainer = aliceBlue,
    onTertiaryContainer = lapisLazul,
    background = ghostWhite,
    onBackground = richBlack,
    surface = richBlack,
    onSurface = ghostWhite,
    onPrimaryContainer = repointOrange,
    secondaryContainer = RoseWood
)

private val LightColorScheme = lightColorScheme(
    primary = repointOrange,
    secondary = RoseWood,
    tertiary = PowderBlue,
    tertiaryContainer = aliceBlue,
    onTertiaryContainer = lapisLazul,
    background = ghostWhite,
    onBackground = richBlack,
    surface = ghostWhite,
    onSurface = richBlack,
    onPrimaryContainer = repointOrange,
    secondaryContainer = RoseWood


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun RepointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
      /*  dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }*/

        darkTheme -> DarkColorScheme
        else -> LightColorScheme

    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepointTypography,
        content = content
    )
}