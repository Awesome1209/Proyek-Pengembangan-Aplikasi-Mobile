package com.example.hujjah.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hujjah Design System Spacing Scale (4dp/8dp modular grid)
 */
data class HujjahSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 24.dp,
    val huge: Dp = 32.dp
)

val LocalHujjahSpacing = staticCompositionLocalOf { HujjahSpacing() }

val HujjahSpacingTokens = HujjahSpacing()
