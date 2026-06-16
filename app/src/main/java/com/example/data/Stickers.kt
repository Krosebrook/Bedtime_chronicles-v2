package com.example.data

data class Sticker(
    val id: String,
    val name: String,
    val emoji: String,
    val category: String, // "Stars", "Characters", "Objects", "Locations"
    val unlockHint: String,
    val unlockKeywords: List<String>
)

val availableStickers = listOf(
    // Stars
    Sticker("star", "Aura Star", "⭐", "Stars", "Unlocked by default!", emptyList()),
    Sticker("comet", "Gliding Comet", "☄️", "Stars", "Unlocked by default!", emptyList()),
    Sticker("shooting_star", "Dreamy Shooting Star", "🌠", "Stars", "Generate or read a story about stars or dreams.", listOf("star", "dream", "stardust")),
    Sticker("planet", "Ringed Saturn", "🪐", "Stars", "Unlocked by default!", emptyList()),

    // Characters
    Sticker("whale", "Starry Whale", "🐋", "Characters", "Read 'The Starry Whales' or a tale about whales.", listOf("whale", "whales")),
    Sticker("kitty", "Cloud Kitty", "🐱", "Characters", "Read 'The Candy Cloud Kingdom' or a tale about kittens.", listOf("kitty", "kittens", "cat", "cats")),
    Sticker("bear", "Barnaby Bear", "🐻", "Characters", "Read 'Bear's Long Sleep' or a story about bears.", listOf("bear", "barnaby", "bears")),
    Sticker("owl", "Symphony Owl", "🦉", "Characters", "Read 'The Firefly Symphony' or a story with an owl.", listOf("owl", "owls")),
    Sticker("dragon", "Celestial Dragon", "🐉", "Characters", "Generate or read a story about celestial dragons.", listOf("dragon", "dragons")),

    // Objects
    Sticker("rocket", "Moonlit Rocket", "🚀", "Objects", "Read 'The Moonlit Rocket' or a story about a rocket or spaceship.", listOf("rocket", "spaceship", "space")),
    Sticker("firefly", "Golden Firefly", "🐝", "Objects", "Read 'The Firefly Symphony' or a story about fireflies.", listOf("firefly", "fireflies")),
    Sticker("ufo", "Friendly UFO", "🛸", "Objects", "Generate or read a story about friendly aliens or UFOs.", listOf("ufo", "alien", " aliens", "outer space")),
    Sticker("magic_wand", "Wonder Wand", "🪄", "Objects", "Generate or read a story about magic or wonders.", listOf("magic", "wand", "wizard", "spell")),

    // Locations
    Sticker("moon", "Quiet Moon", "🌙", "Locations", "Read 'Journey to the Quiet Moon' or a story about the moon.", listOf("moon", "quiet moon", "moonbeams")),
    Sticker("cloud", "Candy Cloud", "☁️", "Locations", "Read 'The Candy Cloud Kingdom' or a story with sweet clouds.", listOf("cloud", "clouds")),
    Sticker("castle", "Stardust Castle", "🏰", "Locations", "Generate or read a story about a castle or kingdom.", listOf("castle", "kingdom", "palace")),
    Sticker("tree", "Whispering Tree", "🌳", "Locations", "Read 'Whispering Woods' or a story about forests or trees.", listOf("woods", "forest", "tree", "trees"))
)
