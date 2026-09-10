package com.eon.futuresimulator.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightScheme = lightColorScheme(
    primary = AccentBluePrimary,
    onPrimary = Color.White,
    secondary = AccentBlueLight,
    background = NeutralLight50,
    surface = Color.White,
    surfaceVariant = NeutralLight100,
    onBackground = NeutralDark900,
    onSurface = NeutralDark900,
    outline = NeutralLight300,
    error = UnrealisticRed,
)

private val DarkScheme = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = NeutralDark900,
    secondary = AccentBluePrimary,
    background = NeutralDark900,
    surface = NeutralDark800,
    surfaceVariant = NeutralDark700,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = NeutralDark600,
    error = UnrealisticRed,
)

private val AmoledScheme = DarkScheme.copy(
    background = Color.Black,
    surface = Color(0xFF060608),
    surfaceVariant = Color(0xFF101014),
)

private val RedScheme = lightColorScheme(
    primary = AccentRedPrimary,
    onPrimary = Color.White,
    secondary = AccentRedLight,
    background = NeutralLight50,
    surface = Color.White,
    surfaceVariant = NeutralLight100,
    onBackground = NeutralDark900,
    onSurface = NeutralDark900,
    outline = NeutralLight300,
    error = AccentRedDark,
)

private val BlueScheme = lightColorScheme(
    primary = AccentRoyalPrimary,
    onPrimary = Color.White,
    secondary = AccentRoyalLight,
    background = NeutralLight50,
    surface = Color.White,
    surfaceVariant = NeutralLight100,
    onBackground = NeutralDark900,
    onSurface = NeutralDark900,
    outline = NeutralLight300,
    error = UnrealisticRed,
)

/**
 * Resolves the active ColorScheme for the requested [ThemeMode].
 * SYSTEM follows the OS light/dark setting and, from Android 12+, the user's
 * Material You wallpaper colors (dynamic color) — everything else is a fixed,
 * Windows-11-inspired Fluent palette so it looks identical on every device.
 */
@Composable
fun eonColorScheme(mode: ThemeMode, dynamicColorAllowed: Boolean) = when (mode) {
    ThemeMode.SYSTEM -> {
        val dark = isSystemInDarkTheme()
        when {
            dynamicColorAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark ->
                dynamicDarkColorScheme(androidx.compose.ui.platform.LocalContext.current)
            dynamicColorAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !dark ->
                dynamicLightColorScheme(androidx.compose.ui.platform.LocalContext.current)
            dark -> DarkScheme
            else -> LightScheme
        }
    }
    ThemeMode.LIGHT -> LightScheme
    ThemeMode.DARK -> DarkScheme
    ThemeMode.AMOLED -> AmoledScheme
    ThemeMode.RED -> RedScheme
    ThemeMode.BLUE -> BlueScheme
}

private fun ThemeMode.isDarkSurface(systemDark: Boolean) = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.DARK, ThemeMode.AMOLED -> true
    ThemeMode.LIGHT, ThemeMode.RED, ThemeMode.BLUE -> false
}

@Composable
fun EonTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColorAllowed: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = eonColorScheme(themeMode, dynamicColorAllowed)
    val isDark = themeMode.isDarkSurface(systemDark)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EonTypography,
        content = content,
    )
}
