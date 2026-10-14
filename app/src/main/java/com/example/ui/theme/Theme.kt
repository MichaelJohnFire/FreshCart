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
import com.example.data.local.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = MintGreenPrimaryContainer,
    onPrimaryContainer = MintGreenOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = TangerineTertiary,
    onTertiary = TangerineOnTertiary,
    tertiaryContainer = TangerineTertiaryContainer,
    onTertiaryContainer = TangerineOnTertiaryContainer,
    background = FreshBackgroundLight,
    onBackground = FreshOnBackgroundLight,
    surface = FreshSurfaceLight,
    onSurface = FreshOnSurfaceLight,
    surfaceVariant = FreshSurfaceVariantLight,
    onSurfaceVariant = FreshOnSurfaceVariantLight,
    outline = FreshOutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimaryDark,
    onPrimary = ForestGreenOnPrimaryDark,
    primaryContainer = MintGreenPrimaryContainerDark,
    onPrimaryContainer = MintGreenOnPrimaryContainerDark,
    secondary = SageSecondaryDark,
    onSecondary = SageOnSecondaryDark,
    secondaryContainer = SageSecondaryContainerDark,
    onSecondaryContainer = SageOnSecondaryContainerDark,
    tertiary = TangerineTertiaryDark,
    onTertiary = TangerineOnTertiaryDark,
    tertiaryContainer = TangerineTertiaryContainerDark,
    onTertiaryContainer = TangerineOnTertiaryContainerDark,
    background = FreshBackgroundDark,
    onBackground = FreshOnBackgroundDark,
    surface = FreshSurfaceDark,
    onSurface = FreshOnSurfaceDark,
    surfaceVariant = FreshSurfaceVariantDark,
    onSurfaceVariant = FreshOnSurfaceVariantDark,
    outline = FreshOutlineDark
)

@Composable
fun FreshCartTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    dynamicColor: Boolean = false, // Set false to default to our signature Fresh Market Green branding
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

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = FreshCartTheme(
    themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
    dynamicColor = dynamicColor,
    content = content
)
