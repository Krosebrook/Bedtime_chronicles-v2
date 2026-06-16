package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Hero Templates Model
data class Hero(
    val id: String,
    val name: String,
    val title: String,
    val power: String,
    val description: String,
    val icon: ImageVector,
    val colorHex: String,
    val gradientHexs: List<String>,
    val constellation: String
) {
    val color: Color
        get() = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.White
        }

    val gradientColors: List<Color>
        get() = gradientHexs.map { hex ->
            try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                Color.DarkGray
            }
        }
}

// Badge/Trophy Model
data class BedtimeBadge(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val condition: String,
    val unlocked: Boolean = false
)

// Content Theme Model
data class ContentTheme(
    val id: String,
    val label: String,
    val emoji: String
)

// Centralized Assets Registry
object BedtimeAssets {
    
    // Emojis for avatar creation
    val AVATAR_EMOJIS = listOf(
        "🦸", "🧙", "🦊", "🐱", "🦄", "🌟", "🚀", "🦋", "🐼", "🦁", "🐸", "🌈"
    )

    // The 8 Hero Templates with unique cosmic powers, constellations, backgrounds, and themes
    val HEROES = listOf(
        Hero(
            id = "hero-1",
            name = "Nova",
            title = "Guardian of Light",
            power = "Starlight Shield",
            description = "Nova protects sleeping children with her magical shield that glows like a thousand stars. She turns nightlights into tiny stars that keep the dark at bay.",
            icon = Icons.Default.Shield,
            colorHex = "#FFD54F",
            gradientHexs = listOf("#1B237E", "#283593"),
            constellation = "The Shield"
        ),
        Hero(
            id = "hero-2",
            name = "Coral",
            title = "Heart of the Ocean",
            power = "Kindness Wave",
            description = "Coral swims through moonlit oceans, spreading warmth and kindness wherever she goes. Her tail shimmers with sunset colors and her songs heal lonely hearts.",
            icon = Icons.Default.Waves,
            colorHex = "#4DD0E1",
            gradientHexs = listOf("#006064", "#00838F"),
            constellation = "The Wave"
        ),
        Hero(
            id = "hero-3",
            name = "Orion",
            title = "Star of Friendship",
            power = "Constellation Bond",
            description = "Once the loneliest star in the sky, Orion now connects friends across the universe. His constellation reminds everyone that true friends make you shine brighter.",
            icon = Icons.Default.Star,
            colorHex = "#B388FF",
            gradientHexs = listOf("#311B92", "#4527A0"),
            constellation = "The Bridge"
        ),
        Hero(
            id = "hero-4",
            name = "Luna",
            title = "The Dream Weaver",
            power = "Dream Loom",
            description = "Luna weaves beautiful dreams on her magical loom made of moonbeams and starlight. Each dream is unique and full of the things that make children happiest.",
            icon = Icons.Default.Bedtime,
            colorHex = "#CE93D8",
            gradientHexs = listOf("#4A148C", "#6A1B9A"),
            constellation = "The Loom"
        ),
        Hero(
            id = "hero-5",
            name = "Nimbus",
            title = "The Brave Cloud",
            power = "Storm Shield",
            description = "The smallest cloud with the biggest heart. Nimbus proves that you don't need to be big to be brave. He protects gardens and children from scary storms.",
            icon = Icons.Default.Cloud,
            colorHex = "#90CAF9",
            gradientHexs = listOf("#1565C0", "#1976D2"),
            constellation = "The Cloud"
        ),
        Hero(
            id = "hero-6",
            name = "Bloom",
            title = "Garden Keeper",
            power = "Dream Seeds",
            description = "Bloom tends the magical moonlit garden where dreams grow like flowers. Her silver wings scatter dream-seeds across the world, planting beautiful visions in sleeping minds.",
            icon = Icons.Default.LocalFlorist,
            colorHex = "#A5D6A7",
            gradientHexs = listOf("#1B5E20", "#2E7D32"),
            constellation = "The Garden"
        ),
        Hero(
            id = "hero-7",
            name = "Whistle",
            title = "Night Train Conductor",
            power = "Dream Express",
            description = "Every night at bedtime o'clock, Whistle drives the magical Night Train along the Milky Way, carrying dreaming children to wonderful destinations among the stars.",
            icon = Icons.Default.DirectionsTransit,
            colorHex = "#B0BEC5",
            gradientHexs = listOf("#37474F", "#455A64"),
            constellation = "The Track"
        ),
        Hero(
            id = "hero-8",
            name = "Shade",
            title = "Shadow Friend",
            power = "Shadow Play",
            description = "Made entirely of shadows, Shade is the gentlest hero of all. He makes funny shadow shapes to show children that the dark is nothing to fear, just a cozy blanket for sleeping.",
            icon = Icons.Default.Contrast,
            colorHex = "#78909C",
            gradientHexs = listOf("#212121", "#37474F"),
            constellation = "The Shadow"
        )
    )

    // The 12 Gamified Developmental badges/trophies
    val BADGE_DEFINITIONS = listOf(
        BedtimeBadge("first-adventure", "🌟", "First Adventure", "Completed your very first story!", "first_story"),
        BedtimeBadge("night-owl", "🦉", "Night Owl", "Listened to a bedtime story after 8:00 PM.", "night_story"),
        BedtimeBadge("early-bird", "🐦", "Early Bird", "Started an early story in the morning.", "morning_story"),
        BedtimeBadge("all-heroes", "🏆", "Hero Collector", "Played role-playing stories with 8 unique heroes!", "all_heroes"),
        BedtimeBadge("mad-libs-master", "🤪", "Silly Storyteller", "Completed 3 creative Mad Libs stories.", "madlibs_3"),
        BedtimeBadge("dream-weaver", "🌙", "Dream Weaver", "Completed 3 relaxing Sleep mode stories.", "sleep_3"),
        BedtimeBadge("classic-champion", "⚔️", "Classic Champion", "Completed 5 incredible Classic stories.", "classic_5"),
        BedtimeBadge("story-streak-3", "🔥", "On Fire!", "Maintained a consistent 3-day story reading streak.", "streak_3"),
        BedtimeBadge("story-streak-7", "💎", "Diamond Reader", "Maintained a beautiful 7-day story reading streak.", "streak_7"),
        BedtimeBadge("bookworm", "📚", "Bookworm", "Completed 10 total storybooks on the app.", "total_10"),
        BedtimeBadge("legend", "👑", "Story Legend", "Completed 25 total magical sleep narratives.", "total_25"),
        BedtimeBadge("vocabulary-star", "📖", "Word Wizard", "Learned 5 vocabulary words on your stories!", "vocab_5")
    )

    // The 6 Content Character focus theme tags
    val CONTENT_THEMES = listOf(
        ContentTheme("courage", "Courage", "🦁"),
        ContentTheme("kindness", "Kindness", "💗"),
        ContentTheme("friendship", "Friendship", "🤝"),
        ContentTheme("wonder", "Wonder", "✨"),
        ContentTheme("imagination", "Imagination", "🌈"),
        ContentTheme("comfort", "Comfort", "🧸")
    )
}
