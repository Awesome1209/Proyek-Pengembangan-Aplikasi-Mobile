package com.example.hujjah.presentation.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Hujjah Design System Shape Tokens
 */
data class HujjahShapes(
    val small: CornerBasedShape = RoundedCornerShape(12.dp),
    val medium: CornerBasedShape = RoundedCornerShape(16.dp),
    val large: CornerBasedShape = RoundedCornerShape(24.dp),
    val pill: CornerBasedShape = RoundedCornerShape(32.dp)
)

val LocalHujjahShapes = staticCompositionLocalOf { HujjahShapes() }

val HujjahShapeTokens = HujjahShapes()

/**
 * Material Design 3 Shapes Mapping
 */
val MaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = HujjahShapeTokens.small,
    medium = HujjahShapeTokens.medium,
    large = HujjahShapeTokens.large,
    extraLarge = HujjahShapeTokens.pill
)
