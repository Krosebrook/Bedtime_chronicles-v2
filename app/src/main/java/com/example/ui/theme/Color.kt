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
    primary = Color(0xFF38BDF8), // Celestial Cyan
    secondary = Color(0xFFFBBF24), // Milkyway Amber
    tertiary = Color(0xFFA5B4FC), // Lavender starlight
    background = Color(0xFF000000), // Pure Midnight Void black
    surface = Color(0xFF08080C), // Dark matte obsidian
    surfaceVariant = Color(0xFF111116), // Dark obsidian contrast
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF8FAFC), // Crisp ultra high contrast white
    onSurface = Color(0xFFF8FAFC), // Crisp ultra high contrast white
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF334155) // Slate gray border
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
