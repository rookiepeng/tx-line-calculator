package com.rookiedev.microwavetools.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.rookiedev.microwavetools.R

/** Semantic colors: green for what Synthesize solves, purple for what Analyze solves. */
@Immutable
data class Accents(
    val synthesize: Color,
    val onSynthesize: Color,
    val synthesizeContainer: Color,
    val analyze: Color,
    val onAnalyze: Color,
    val analyzeContainer: Color,
)

private val LightAccents = Accents(
    synthesize = Color(0xFF2E7D32), onSynthesize = Color.White, synthesizeContainer = Color(0xFFC8E6C9),
    analyze = Color(0xFF8E24AA), onAnalyze = Color.White, analyzeContainer = Color(0xFFF3D9F7),
)

private val DarkAccents = Accents(
    synthesize = Color(0xFF81C784), onSynthesize = Color(0xFF0B3A10), synthesizeContainer = Color(0xFF1E4620),
    analyze = Color(0xFFCE93D8), onAnalyze = Color(0xFF3B0A45), analyzeContainer = Color(0xFF4A1856),
)

val LocalAccents = staticCompositionLocalOf { LightAccents }

/** The app palette from res/values(-night)/colors.xml, used when dynamic color is unavailable. */
@Composable
private fun brandScheme(dark: Boolean): ColorScheme {
    val primary = colorResource(R.color.md_theme_primary)
    val onPrimary = colorResource(R.color.md_theme_onPrimary)
    val primaryContainer = colorResource(R.color.md_theme_primaryContainer)
    val onPrimaryContainer = colorResource(R.color.md_theme_onPrimaryContainer)
    val secondary = colorResource(R.color.md_theme_secondary)
    val onSecondary = colorResource(R.color.md_theme_onSecondary)
    val secondaryContainer = colorResource(R.color.md_theme_secondaryContainer)
    val onSecondaryContainer = colorResource(R.color.md_theme_onSecondaryContainer)
    val background = colorResource(R.color.md_theme_background)
    val onBackground = colorResource(R.color.md_theme_onBackground)
    val surface = colorResource(R.color.md_theme_surface)
    val onSurface = colorResource(R.color.md_theme_onSurface)
    val surfaceVariant = colorResource(R.color.md_theme_surfaceVariant)
    val onSurfaceVariant = colorResource(R.color.md_theme_onSurfaceVariant)
    val outline = colorResource(R.color.md_theme_outline)
    val outlineVariant = colorResource(R.color.md_theme_outlineVariant)
    val error = colorResource(R.color.md_theme_error)
    val containerLow = colorResource(R.color.md_theme_surfaceContainerLow)
    val container = colorResource(R.color.md_theme_surfaceContainer)
    val containerHigh = colorResource(R.color.md_theme_surfaceContainerHigh)
    return if (dark) darkColorScheme(
        primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer, secondary = secondary, onSecondary = onSecondary,
        secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
        background = background, onBackground = onBackground, surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant, outline = outline,
        outlineVariant = outlineVariant, error = error, surfaceContainerLow = containerLow,
        surfaceContainer = container, surfaceContainerHigh = containerHigh,
    ) else lightColorScheme(
        primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer, secondary = secondary, onSecondary = onSecondary,
        secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
        background = background, onBackground = onBackground, surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant, outline = outline,
        outlineVariant = outlineVariant, error = error, surfaceContainerLow = containerLow,
        surfaceContainer = container, surfaceContainerHigh = containerHigh,
    )
}

@Composable
fun TxLineTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> brandScheme(dark)
    }
    CompositionLocalProvider(LocalAccents provides if (dark) DarkAccents else LightAccents) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
