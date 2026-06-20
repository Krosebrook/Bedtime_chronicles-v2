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

    // The 9 Hero Templates with unique cosmic powers, constellations, backgrounds, and themes
    val HEROES = listOf(
        Hero(
            id = "hero-1",
            name = "Nova Engineer",
            title = "The Blue Spark",
            power = "Techno-Flight & Robot Owl Scout",
            description = "Wearing deep blue armor coursing with purple energy lines, Nova Engineer explores the far reaches of the universe alongside his trusty robotic owl, fixing broken stars and building bridges across the cosmos.",
            icon = Icons.Default.Hardware,
            colorHex = "#3B82F6",
            gradientHexs = listOf("#1E3A8A", "#312E81"),
            constellation = "The Spark"
        ),
        Hero(
            id = "hero-2",
            name = "Star Knight",
            title = "Defender of the Galaxy",
            power = "Cosmic Shield",
            description = "Donning an iconic pink and purple star-crested helmet, Star Knight summons a swirling galaxy shield to block nightmares and defend the dreams of children everywhere. He is brave, bright, and always ready.",
            icon = Icons.Default.Security,
            colorHex = "#D946EF",
            gradientHexs = listOf("#701A75", "#831843"),
            constellation = "The Shield"
        ),
        Hero(
            id = "hero-3",
            name = "Void Keeper",
            title = "Explorer of the Deep",
            power = "Lantern of the Black Hole",
            description = "Dressed in a glowing orange-trimmed space suit, Void Keeper carries a lantern containing a miniature, harmless black hole. He wanders the dark corners of space, catching bad dreams and safely locking them away.",
            icon = Icons.Default.Public,
            colorHex = "#F97316",
            gradientHexs = listOf("#7C2D12", "#431407"),
            constellation = "The Lantern"
        ),
        Hero(
            id = "hero-4",
            name = "Gearheart",
            title = "The Steampunk Mechanic",
            power = "Clockwork Engineering",
            description = "With her signature copper goggles and a bright red braid, Gearheart uses her mechanical genius to invent wondrous clockwork toys that come alive to tell stories and keep children company at night.",
            icon = Icons.Default.Settings,
            colorHex = "#FBBF24",
            gradientHexs = listOf("#78350F", "#064E3B"),
            constellation = "The Gear"
        ),
        Hero(
            id = "hero-5",
            name = "Celestial",
            title = "The Halo Sentinel",
            power = "Orbiting Rings of Light",
            description = "A mysterious heroine with glowing white hair and orbiting planetary rings. She floats silently through the cosmos, bringing a sense of ultimate peace and tranquility to those entering slumber.",
            icon = Icons.Default.Brightness7,
            colorHex = "#F8FAFC",
            gradientHexs = listOf("#0F172A", "#1E293B"),
            constellation = "The Ring"
        ),
        Hero(
            id = "hero-6",
            name = "Prism",
            title = "Master of Refraction",
            power = "Crystal Light Beams",
            description = "Sporting vivid purple hair and geometric crystal armor, Prism holds the ultimate jewel of light. She can split starlight into breathtaking rainbows that dance across bedroom walls.",
            icon = Icons.Default.Diamond,
            colorHex = "#C084FC",
            gradientHexs = listOf("#4C1D95", "#3B0764"),
            constellation = "The Prism"
        ),
        Hero(
            id = "hero-7",
            name = "Starweaver",
            title = "The Constellation Crafter",
            power = "Galaxy Weaving",
            description = "Wrapping herself in a flowing cape made of real constellations, Starweaver shapes cosmic dust into glowing magical orbs. Her deep blue starry hair holds the map to all galaxies.",
            icon = Icons.Default.AutoFixHigh,
            colorHex = "#818CF8",
            gradientHexs = listOf("#1E1B4B", "#312E81"),
            constellation = "The Loom"
        ),
        Hero(
            id = "hero-8",
            name = "Botanist",
            title = "The Cosmic Gardener",
            power = "Neon Flora Growth",
            description = "Wearing flower-petal goggles and neon green circuit armor, Botanist plants seeds that grow into towering, glowing trees on barren asteroids, bringing life to the darkest corners of space.",
            icon = Icons.Default.LocalFlorist,
            colorHex = "#4ADE80",
            gradientHexs = listOf("#064E3B", "#065F46"),
            constellation = "The Sprout"
        ),
        Hero(
            id = "hero-9",
            name = "Crystal Knight",
            title = "The Ice Guardian",
            power = "Swirling Frost Shield",
            description = "With bright eyes and glowing crystal-plated armor, the Crystal Knight wields a swirling vortex shield of pure star-frost. He leads the charge in freezing bad dreams before they even begin.",
            icon = Icons.Default.AcUnit,
            colorHex = "#38BDF8",
            gradientHexs = listOf("#082F49", "#0C4A6E"),
            constellation = "The Crystal"
        )
    )

    // The 12 Gamified Developmental badges/trophies
    val BADGE_DEFINITIONS = listOf(
        BedtimeBadge("first-adventure", "🌟", "First Adventure", "Completed your very first story!", "first_story"),
        BedtimeBadge("night-owl", "🦉", "Night Owl", "Listened to a bedtime story after 8:00 PM.", "night_story"),
        BedtimeBadge("early-bird", "🐦", "Early Bird", "Started an early story in the morning.", "morning_story"),
        BedtimeBadge("all-heroes", "🏆", "Hero Collector", "Played role-playing stories with 9 unique heroes!", "all_heroes"),
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
