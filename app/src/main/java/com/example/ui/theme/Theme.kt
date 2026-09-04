package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ArtisticPrimaryDark,
    onPrimary = ArtisticOnPrimaryDark,
    primaryContainer = ArtisticPrimaryContainerDark,
    onPrimaryContainer = ArtisticOnPrimaryContainerDark,
    secondary = ArtisticSecondaryDark,
    onSecondary = ArtisticOnSecondaryDark,
    secondaryContainer = ArtisticSecondaryContainerDark,
    onSecondaryContainer = ArtisticOnSecondaryContainerDark,
    tertiary = ArtisticTertiaryDark,
    onTertiary = ArtisticOnTertiaryDark,
    tertiaryContainer = ArtisticTertiaryContainerDark,
    onTertiaryContainer = ArtisticOnTertiaryContainerDark,
    background = ArtisticBgDark,
    onBackground = ArtisticOnSurfaceDark,
    surface = ArtisticSurfaceDark,
    onSurface = ArtisticOnSurfaceDark,
    surfaceVariant = ArtisticSurfaceVariantDark,
    onSurfaceVariant = ArtisticOnSurfaceVariantDark,
    outline = ArtisticOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = ArtisticPrimaryLight,
    onPrimary = ArtisticOnPrimaryLight,
    primaryContainer = ArtisticPrimaryContainerLight,
    onPrimaryContainer = ArtisticOnPrimaryContainerLight,
    secondary = ArtisticSecondaryLight,
    onSecondary = ArtisticOnSecondaryLight,
    secondaryContainer = ArtisticSecondaryContainerLight,
    onSecondaryContainer = ArtisticOnSecondaryContainerLight,
    tertiary = ArtisticTertiaryLight,
    onTertiary = ArtisticOnTertiaryLight,
    tertiaryContainer = ArtisticTertiaryContainerLight,
    onTertiaryContainer = ArtisticOnTertiaryContainerLight,
    background = ArtisticBgLight,
    onBackground = ArtisticOnSurfaceLight,
    surface = ArtisticSurfaceLight,
    onSurface = ArtisticOnSurfaceLight,
    surfaceVariant = ArtisticSurfaceVariantLight,
    onSurfaceVariant = ArtisticOnSurfaceVariantLight,
    outline = ArtisticOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our branded design system by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
