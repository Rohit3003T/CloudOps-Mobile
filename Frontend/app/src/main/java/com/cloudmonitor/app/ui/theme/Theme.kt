package com.cloudmonitor.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── AWS-inspired dark palette ──────────────────────────────────────────────────
val AwsOrange         = Color(0xFFFF9900)
val AwsOrangeDim      = Color(0x1FFF9900)
val AwsDark           = Color(0xFF0F1923)
val AwsDarkSurface    = Color(0xFF111820)
val AwsDarkContainer  = Color(0xFF1A2433)
val AwsDarkVariant    = Color(0xFF232F3E)
val TextPrimary       = Color(0xFFE2EAF2)
val TextSecondary     = Color(0xFF8FA3B8)
val TextTertiary      = Color(0xFF576F87)
val Green             = Color(0xFF22D87A)
val Red               = Color(0xFFFF4757)
val Blue              = Color(0xFF38BEFF)
val Purple            = Color(0xFFA78BFA)
val Yellow            = Color(0xFFFFD166)
val BorderColor       = Color(0xFF1E2D3D)

private val DarkColorScheme = darkColorScheme(
    primary          = AwsOrange,
    onPrimary        = Color(0xFF1A1A1A),
    primaryContainer = AwsDarkVariant,
    onPrimaryContainer = AwsOrange,
    secondary        = Blue,
    onSecondary      = AwsDark,
    secondaryContainer = Color(0xFF1A2D3D),
    onSecondaryContainer = Blue,
    background       = AwsDark,
    onBackground     = TextPrimary,
    surface          = AwsDarkSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = AwsDarkContainer,
    onSurfaceVariant = TextSecondary,
    error            = Red,
    onError          = Color.White,
    outline          = BorderColor,
    outlineVariant   = Color(0xFF263545)
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.06.sp,
        color = TextTertiary
    )
)

@Composable
fun CloudMonitorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
