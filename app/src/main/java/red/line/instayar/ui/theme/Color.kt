package red.line.instayar.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Gradient & Brand Accents
val BrandViolet = Color(0xFF7C3AED)
val BrandMagenta = Color(0xFFE11D48)
val BrandPurple = Color(0xFF8B5CF6)
val BrandPink = Color(0xFFEC4899)
val BrandCyan = Color(0xFF06B6D4)
val BrandTeal = Color(0xFF14B8A6)

// Gold / VIP
val VipGold = Color(0xFFF59E0B)
val VipGoldLight = Color(0xFFFDE68A)
val VipGoldDark = Color(0xFFD97706)

// Dark Theme Luxury Backgrounds
val DarkBackground = Color(0xFF0C0A17)
val DarkSurface = Color(0xFF151226)
val DarkSurfaceCard = Color(0xFF1E1935)
val DarkSurfaceCardElevated = Color(0xFF292247)
val DarkBorder = Color(0x338B5CF6)
val DarkBorderSubtle = Color(0x1FFFFFFF)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFF1F5F9)
val LightSurfaceCardElevated = Color(0xFFE2E8F0)
val LightBorder = Color(0x227C3AED)
val LightBorderSubtle = Color(0x0F000000)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Gradients
val PrimaryGradientBrush = Brush.linearGradient(
    colors = listOf(BrandPurple, BrandPink)
)

val VipGradientBrush = Brush.linearGradient(
    colors = listOf(VipGold, VipGoldDark)
)

val CardGlowBrush = Brush.radialGradient(
    colors = listOf(Color(0x338B5CF6), Color.Transparent),
    radius = 500f
)
