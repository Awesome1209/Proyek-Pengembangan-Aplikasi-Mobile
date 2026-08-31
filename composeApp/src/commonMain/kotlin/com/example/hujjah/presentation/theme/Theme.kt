package com.example.hujjah.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGold,
    onPrimary = Charcoal,
    primaryContainer = WarmBeige,
    onPrimaryContainer = Charcoal,
    secondary = Charcoal,
    onSecondary = WarmIvory,
    secondaryContainer = WarmBeige,
    onSecondaryContainer = Charcoal,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = ErrorLight,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGoldDark,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = WarmIvory,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = ErrorDark,
    onError = BackgroundDark
)

data class HujjahColors(
    val isDarkTheme: Boolean,
    val goldHighlight: Color,
    val islamicGreen: Color,
    val goldAccent: Color = if (isDarkTheme) NeonGold else LuxuryGold,
    val darkTeal: Color = DarkIslamicGreen,
    val charcoal: Color = Charcoal,
    val warmIvory: Color = WarmIvory
)

val LocalHujjahColors = staticCompositionLocalOf {
    HujjahColors(
        isDarkTheme = false,
        goldHighlight = LuxuryGold,
        islamicGreen = DarkIslamicGreen,
        charcoal = Charcoal,
        warmIvory = WarmIvory
    )
}

@Composable
fun HujjahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val hujjahColors = HujjahColors(
        isDarkTheme = darkTheme,
        goldHighlight = if (darkTheme) NeonGold else LuxuryGold,
        islamicGreen = DarkIslamicGreen,
        goldAccent = if (darkTheme) NeonGold else LuxuryGold,
        darkTeal = DarkIslamicGreen,
        charcoal = Charcoal,
        warmIvory = WarmIvory
    )

    CompositionLocalProvider(
        LocalHujjahColors provides hujjahColors,
        LocalHujjahSpacing provides HujjahSpacingTokens,
        LocalHujjahShapes provides HujjahShapeTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = MaterialShapes,
            typography = HujjahTypography,
            content = content
        )
    }
}

/**
 * Convenience Extension Accessors for MaterialTheme
 */
val MaterialTheme.spacing: HujjahSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalHujjahSpacing.current

val MaterialTheme.customShapes: HujjahShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalHujjahShapes.current
