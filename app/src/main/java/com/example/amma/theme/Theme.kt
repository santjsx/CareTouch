package com.example.amma.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AmmaGreenBright,
    onPrimary = AmmaTextInverted,
    primaryContainer = AmmaGreenContainer,
    onPrimaryContainer = AmmaGreenBright,

    secondary = AmmaBlueInfo,
    onSecondary = AmmaTextPrimary,
    secondaryContainer = AmmaBlueContainer,
    onSecondaryContainer = AmmaBlueInfo,

    tertiary = AmmaAmberBright,
    onTertiary = AmmaTextInverted,
    tertiaryContainer = AmmaAmberContainer,
    onTertiaryContainer = AmmaAmberBright,

    error = AmmaRedBright,
    onError = AmmaTextPrimary,
    errorContainer = AmmaRedContainer,
    onErrorContainer = AmmaRedBright,

    background = AmmaBackground,
    onBackground = AmmaTextPrimary,

    surface = AmmaSurface,
    onSurface = AmmaTextPrimary,
    surfaceVariant = AmmaSurfaceVariant,
    onSurfaceVariant = AmmaTextSecondary,

    outline = AmmaBorder,
    outlineVariant = AmmaSurfaceElevated
)

@Composable
fun AmmaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
