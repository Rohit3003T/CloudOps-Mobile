package com.cloudops.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AWS-inspired dark theme palette
val AwsOrange = Color(0xFFFF9900)
val AwsOrangeDark = Color(0xFFCC7A00)
val AwsDark = Color(0xFF0F1923)
val AwsDarkCard = Color(0xFF1A2733)
val AwsDarkSurface = Color(0xFF16212E)
val AwsBlue = Color(0xFF1E88E5)
val AwsGreen = Color(0xFF43A047)
val AwsRed = Color(0xFFE53935)
val AwsYellow = Color(0xFFFFB300)
val AwsGray = Color(0xFF8C9BAB)
val AwsLightText = Color(0xFFE8EDF2)

private val DarkColorScheme = darkColorScheme(
    primary = AwsOrange,
    onPrimary = Color.Black,
    primaryContainer = AwsOrangeDark,
    secondary = AwsBlue,
    onSecondary = Color.White,
    background = AwsDark,
    onBackground = AwsLightText,
    surface = AwsDarkCard,
    onSurface = AwsLightText,
    surfaceVariant = AwsDarkSurface,
    onSurfaceVariant = AwsGray,
    error = AwsRed,
    onError = Color.White,
    outline = Color(0xFF2C3E50),
)

@Composable
fun CloudOpsMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
