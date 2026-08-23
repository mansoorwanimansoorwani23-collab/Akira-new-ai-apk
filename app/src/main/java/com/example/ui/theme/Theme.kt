package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AkiraDarkColorScheme = darkColorScheme(
    primary = AkiraCyanPrimary,
    onPrimary = CyberObsidian,
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = AkiraCyanAccent,
    secondary = AkiraVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0072),
    onSecondaryContainer = Color(0xFFFFD7FC),
    tertiary = AkiraIndigoTertiary,
    onTertiary = Color.White,
    background = CyberObsidian,
    onBackground = TextHighContrast,
    surface = CyberDarkSurface,
    onSurface = TextHighContrast,
    surfaceVariant = CyberCardGlass,
    onSurfaceVariant = TextMedium,
    outline = CyberCardBorder,
    error = AkiraErrorCoral,
    onError = Color.White
)

@Composable
fun AkiraAITheme(
    darkTheme: Boolean = true, // Akira is designed with a premium futuristic dark aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AkiraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
