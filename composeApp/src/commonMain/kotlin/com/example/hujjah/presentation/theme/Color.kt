package com.example.hujjah.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Hujjah Core Brand & Modern Editorial Colors
 */
val LuxuryGold = Color(0xFFD4AF37)        // Primary Muted Gold Accent
val NeonGold = Color(0xFFFBBF24)          // OLED Dark Gold Accent
val DarkIslamicGreen = Color(0xFF134E4A)  // Legacy Brand Identity Teal (PRESERVED AS GREEN FOR COMPATIBILITY)
val Charcoal = Color(0xFF25231F)          // Modern Editorial Primary Text & CTA
val WarmIvory = Color(0xFFFBF8F0)         // Modern Editorial Light Background
val WarmWhite = Color(0xFFFFFDF8)         // Modern Editorial Light Surface
val WarmBeige = Color(0xFFF3EDE2)         // Modern Editorial Soft Neutral Surface Variant
val WarmGray = Color(0xFF777168)          // Modern Editorial Secondary Muted Text
val SoftCreamDivider = Color(0xFFE6E1D5)  // Modern Editorial Soft Border Outline
val WarmBronze = Color(0xFF8A6439)        // Secondary Warm Bronze Accent

/**
 * Hujjah Semantic Light Tokens (Modern Islamic Editorial Wellness)
 */
val PrimaryGold = LuxuryGold
val BackgroundLight = WarmIvory
val SurfaceLight = WarmWhite
val SurfaceVariantLight = WarmBeige
val TextPrimaryLight = Charcoal
val TextSecondaryLight = WarmGray
val OutlineLight = SoftCreamDivider
val ErrorLight = Color(0xFFDC2626)           // Semantic Error Red

/**
 * Hujjah Semantic Dark Tokens (OLED Black)
 */
val PrimaryGoldDark = NeonGold
val DarkTealDark = Color(0xFF0F3D39)
val BackgroundDark = Color(0xFF000000)       // True OLED Black
val SurfaceDark = Color(0xFF121212)          // Elevated OLED Dark Surface
val SurfaceVariantDark = Color(0xFF1C1A17)   // Elevated Warm Dark Surface Variant
val TextPrimaryDark = Color(0xFFFBF8F0)      // Warm Ivory Light Text
val TextSecondaryDark = Color(0xFFA39E93)    // Muted Warm Light Text
val OutlineDark = Color(0xFF2C2824)          // Subtle Dark Border Outline
val ErrorDark = Color(0xFFEF4444)            // Semantic Dark Error Red

/**
 * Backward Compatibility Aliases
 */
val DarkTeal = DarkIslamicGreen
val PureWhite = SurfaceLight
val TrueBlack = BackgroundDark
val LightSurface = SurfaceVariantLight
val DarkSurface = SurfaceVariantDark
