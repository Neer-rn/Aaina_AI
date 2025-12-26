package com.example.aainaai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * KYC App Color Scheme - Official, Trustworthy, Gov-Tech
 * Light theme only for consistency and professional appearance
 */
private val KYCColorScheme = lightColorScheme(
    // Primary - Deep Emerald (Main brand color)
    primary = DeepEmerald,
    onPrimary = Color.White,
    primaryContainer = DeepEmeraldLight,
    onPrimaryContainer = Color.White,

    // Secondary - Official Gold
    secondary = OfficialGold,
    onSecondary = TextPrimary,
    secondaryContainer = OfficialGoldLight,
    onSecondaryContainer = TextPrimary,

    // Tertiary - Success Green
    tertiary = SuccessGreen,
    onTertiary = Color.White,

    // Error - Soft Red
    error = SoftRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = SoftRed,

    // Background & Surface
    background = OffWhiteBackground,
    onBackground = TextPrimary,
    surface = CleanWhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = OffWhiteBackground,
    onSurfaceVariant = TextSecondary,

    // Outline & Borders
    outline = BorderGray,
    outlineVariant = Color(0xFFF0F0F0),
)

/**
 * Main app theme composable
 * Uses fixed color scheme for consistent branding (no dynamic colors)
 */
@Composable
fun AainaAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KYCColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}