package com.deivarayan.kids.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val KidsOrange = Color(0xFFFF7A18)
private val KidsPurple = Color(0xFF7C4DFF)
private val KidsGreen = Color(0xFF00C48C)
private val KidsYellow = Color(0xFFFFC107)
private val KidsBackground = Color(0xFFFFF8EE)

private val LightColors = lightColorScheme(
    primary = KidsOrange,
    secondary = KidsPurple,
    tertiary = KidsGreen,
    background = KidsBackground,
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = KidsOrange,
    secondary = KidsPurple,
    tertiary = KidsGreen,
    background = Color(0xFF1E1B2E),
    surface = Color(0xFF2A2640),
)

private val KidsTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
)

val KidsYellowAccent = KidsYellow

@Composable
fun KidsTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = KidsTypography,
        content = content,
    )
}
