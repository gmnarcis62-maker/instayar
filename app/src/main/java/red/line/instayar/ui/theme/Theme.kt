package red.line.instayar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BrandPurple,
    onPrimary = DarkTextPrimary,
    primaryContainer = BrandViolet,
    onPrimaryContainer = DarkTextPrimary,
    secondary = BrandCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceCardElevated,
    onSecondaryContainer = BrandCyan,
    tertiary = VipGold,
    onTertiary = DarkBackground,
    tertiaryContainer = VipGoldDark,
    onTertiaryContainer = VipGoldLight,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = BrandViolet,
    onPrimary = LightSurface,
    primaryContainer = BrandPink,
    onPrimaryContainer = LightSurface,
    secondary = BrandCyan,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceCardElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = VipGold,
    onTertiary = LightSurface,
    tertiaryContainer = VipGoldLight,
    onTertiaryContainer = DarkBackground,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun GhalibinoTheme(
    darkTheme: Boolean = true, // Default to luxury dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    GhalibinoTheme(darkTheme = darkTheme, content = content)
}

