package com.jalay.manageexpenses.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.jalay.manageexpenses.data.preferences.ThemeMode

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
val LocalThemeSetter = staticCompositionLocalOf<(ThemeMode) -> Unit> { {} }

private val DarkColorScheme = darkColorScheme(
    // Background colors
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,

    // Foreground/text colors
    onBackground = DarkForeground,
    onSurface = DarkForeground,
    onSurfaceVariant = DarkMutedForeground,

    // Primary - neutral for shadcn style
    primary = DarkForeground,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkForeground,

    // Secondary - muted
    secondary = DarkMuted,
    onSecondary = DarkForeground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkForeground,

    // Tertiary
    tertiary = DarkMuted,
    onTertiary = DarkForeground,

    // Borders
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f),

    // Error/semantic
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRedDark,
    onErrorContainer = Color.White,

    // Inverse
    inverseSurface = LightSurface,
    inverseOnSurface = LightForeground,
    inversePrimary = LightForeground,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    // Background colors
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,

    // Foreground/text colors
    onBackground = LightForeground,
    onSurface = LightForeground,
    onSurfaceVariant = LightMutedForeground,

    // Primary - neutral for shadcn style
    primary = LightForeground,
    onPrimary = LightBackground,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightForeground,

    // Secondary - muted
    secondary = LightMuted,
    onSecondary = LightForeground,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightForeground,

    // Tertiary
    tertiary = LightMuted,
    onTertiary = LightForeground,

    // Borders
    outline = LightBorder,
    outlineVariant = LightBorder.copy(alpha = 0.5f),

    // Error/semantic
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRedLight,
    onErrorContainer = ExpenseRed,

    // Inverse
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkForeground,
    inversePrimary = DarkForeground,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.3f)
)

@Composable
fun ManageExpensesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Disabled for consistent shadcn styling
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Transparent status bar for edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            // Set status bar icons based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
