package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val MidnightBlue = Color(0xFF05051E)
val DeepIndigo = Color(0xFF0F0FBD)
val MagicPurple = Color(0xFF6366F1)
val PrimaryBlue = Color(0xFF6467F2)
val BackgroundDark = Color(0xFF101122)
val BackgroundLight = Color(0xFFF6F6F8)
val GlassWhite = Color(0x0DFFFFFF) // 5% white
val GlassBorder = Color(0x19FFFFFF) // 10% white
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = PrimaryBlue,
    secondary = MagicPurple,
    tertiary = DeepIndigo,
    background = MidnightBlue,
    surface = BackgroundDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

val MidnightColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFF97316), // Warm Amber
    secondary = Color(0xFFEF4444), // Warm Red
    tertiary = Color(0xFFFBBF24), // Soft Amber
    background = Color(0xFF140C05), // Deep warm brown (low-light)
    surface = Color(0xFF26160A), // Matte warm obsidian
    surfaceVariant = Color(0xFF331E0D), // Warm contrast
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFEDD5), // Warm peach for eye strain reduction
    onSurface = Color(0xFFFFEDD5),
    onSurfaceVariant = Color(0xFFFDBA74), // Orange tint text
    outline = Color(0xFF78350F) // Warm slate border
)

val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = PrimaryBlue,
    secondary = MagicPurple,
    tertiary = DeepIndigo,
    background = BackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = MidnightBlue,
    onSurface = MidnightBlue,
)
