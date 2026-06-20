package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.DatabaseProvider
import com.example.data.CustomHero
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.viewmodel.CustomHeroViewModel
import com.example.viewmodel.CustomHeroViewModelFactory
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreatorScreen(
    onBack: () -> Unit,
    onLaunchStoryCreation: (String) -> Unit // Route to create story with hero name loaded
) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val viewModel: CustomHeroViewModel = viewModel(
        factory = CustomHeroViewModelFactory(db.customHeroDao())
    )

    val heroesList by viewModel.customHeroes.collectAsStateWithLifecycle()

    // Creator State
    var heroName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🦸") }
    var selectedColorHex by remember { mutableStateOf("#818CF8") } // Default Indigo
    var selectedAuraStyle by remember { mutableStateOf("Stardust Nebulae") }
    var selectedCompanion by remember { mutableStateOf("None") }
    var selectedArchetype by remember { mutableStateOf("The Lost Explorer") }
    var backstorySummary by remember { mutableStateOf("A brave deep-space traveler who charts cozy constellation paths to help sleepy planets find their light.") }
    var editingHeroId by remember { mutableStateOf<Int?>(null) }

    val emojis = listOf("🦸", "🧙", "🦊", "🐱", "🦄", "🌟", "🚀", "🐼", "🤖", "🐆", "🦋", "🦖")
    val colorsList = listOf(
        "#818CF8" to "Indigo",
        "#D946EF" to "Magenta",
        "#34D399" to "Emerald",
        "#F59E0B" to "Solar",
        "#38BDF8" to "Cyan",
        "#EC4899" to "Pink"
    )

    val auraStyles = listOf("Stardust Nebulae", "Supernova Ring", "Aurora Borealis", "Glacial Spark")
    
    val companions = listOf(
        "None" to "🌌 None",
        "Mechanical Owl" to "🦉 Owl",
        "Stardust Kitten" to "🐱 Kitten",
        "Baby Dragon" to "🐉 Dragon",
        "Cosmic Turtle" to "🐢 Turtle"
    )

    val backstoryArchetypes = listOf(
        Triple(
            "The Lost Explorer",
            "🚀 Explorer",
            "A brave deep-space traveler who charts cozy constellation paths to help sleepy planets find their light."
        ),
        Triple(
            "Constellation Crafter",
            "✨ Weaver",
            "A magical weaver who gathers loose stardust to sew beautiful shining night-lights across the dark sky."
        ),
        Triple(
            "The Dream Guardian",
            "🌙 Guardian",
            "A gentle keeper of comfortable dreams, protecting children from dark thoughts with soft soothing whispers."
        ),
        Triple(
            "The Cosmic Inventor",
            "🤖 Inventor",
            "A clever robot-builder who designs magical music-box space-capsules to play soothing lullabies to the stars."
        ),
        Triple(
            "Custom",
            "✍️ Custom Lores",
            ""
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Celestial Character Creator", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("char_creator_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // App Greeting
            Text(
                text = "Dream up your ultimate Bedtime Superhero! Customize how they look, give them an cosmic companion, and select their bedtime mission backstory.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate300,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, bottom = 16.dp)
            )

            // Visual live preview section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C091F)),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw Cosmic Aura & Particles
                    AnimatedAuraAndParticles(
                        colorHex = selectedColorHex,
                        auraStyle = selectedAuraStyle,
                        companion = selectedCompanion
                    )

                    // Large central character avatar emoji
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(android.graphics.Color.parseColor(selectedColorHex)).copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedEmoji,
                            fontSize = 54.sp,
                            modifier = Modifier.testTag("preview_character_emoji")
                        )
                    }

                    // Cosmic Character Floating Badge tags
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassWhite)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (heroName.isNotBlank()) heroName else "No Name Hero",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Input Fields Configuration
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = heroName,
                    onValueChange = { heroName = it },
                    label = { Text("Hero's Celestial Name") },
                    placeholder = { Text("e.g. Captain Nebula, Stardust Whiskers...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("char_name_field"),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Create, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )

                // Select Emoji Label
                Text(
                    text = "1. Choose Avatar Element",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else GlassBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedEmoji = emoji }
                                .testTag("emoji_select_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                    }
                }

                // Select Suit/Outfit color
                Text(
                    text = "2. Select Suit & Power Custom Color",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorsList) { (hex, name) ->
                        val isSelected = selectedColorHex == hex
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedColorHex = hex }
                                .testTag("color_select_$name")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = name, fontSize = 11.sp, color = if (isSelected) parsedColor else Slate400, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // Aura style
                Text(
                    text = "3. Select Celestial Aura Style",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    auraStyles.forEach { style ->
                        val isSelected = selectedAuraStyle == style
                        OutlinedCard(
                            onClick = { selectedAuraStyle = style },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else GlassBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("aura_tab_$style")
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = style.substringBefore(" "),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.大胆,
                                    color = if (isSelected) Color.White else Slate300
                                )
                            }
                        }
                    }
                }

                // Celestial Orbit Companion selection
                Text(
                    text = "4. Celestial Orbit Companion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(companions) { (key, label) ->
                        val isSelected = selectedCompanion == key
                        Card(
                            onClick = { selectedCompanion = key },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else GlassWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else GlassBorder),
                            modifier = Modifier
                                .height(44.dp)
                                .padding(vertical = 2.dp)
                                .testTag("companion_chip_$key")
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Backstory configuration section
                Text(
                    text = "5. Craft Bedtime Backstory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(backstoryArchetypes) { (archetype, label, templateText) ->
                        val isSelected = selectedArchetype == archetype
                        Card(
                            onClick = {
                                selectedArchetype = archetype
                                if (archetype != "Custom") {
                                    backstorySummary = templateText
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else GlassWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else GlassBorder),
                            modifier = Modifier
                                .height(44.dp)
                                .padding(vertical = 2.dp)
                                .testTag("archetype_chip_${archetype.replace(" ", "_")}")
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Story Narrative Custom area
                OutlinedTextField(
                    value = backstorySummary,
                    onValueChange = { backstorySummary = it },
                    label = { Text("Hero's Cosmic Backstory Summary") },
                    placeholder = { Text("Write about your hero's gentle cosmic mission...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("char_backstory_field"),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4,
                    enabled = true
                )

                // Save or Update Button
                Button(
                    onClick = {
                        if (heroName.isBlank()) {
                            heroName = "Starlight Hero"
                        }
                        viewModel.saveHero(
                            id = editingHeroId ?: 0,
                            name = heroName,
                            avatarEmoji = selectedEmoji,
                            outfitColorHex = selectedColorHex,
                            auraGlowStyle = selectedAuraStyle,
                            companionType = selectedCompanion,
                            backstoryArchetype = selectedArchetype,
                            backstorySummary = backstorySummary
                        )
                        // Reset form state comfortably
                        heroName = ""
                        selectedEmoji = "🦸"
                        selectedColorHex = "#818CF8"
                        selectedAuraStyle = "Stardust Nebulae"
                        selectedCompanion = "None"
                        selectedArchetype = "The Lost Explorer"
                        backstorySummary = "A brave deep-space traveler who charts cozy constellation paths to help sleepy planets find their light."
                        editingHeroId = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_hero_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = if (editingHeroId == null) "Ignite & Save Character!" else "Update Character Base", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Subtitle lists current heroes
            Text(
                text = "Your Galactic Bedtime Heroes Squad",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            // Database items count checklist/fallback states
            if (heroesList.isEmpty()) {
                Surface(
                    color = GlassWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("heroes_empty_state")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No custom characters created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Design your bedtime hero above, choose their powers, and save them. They will appear right here, waiting to launch their own custom sleep adventures!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    heroesList.forEach { hero ->
                        val parsedColor = Color(android.graphics.Color.parseColor(hero.outfitColorHex))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hero_card_${hero.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF100B2B)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, parsedColor.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Avatar representation
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(parsedColor.copy(alpha = 0.2f))
                                                .border(2.dp, parsedColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = hero.avatarEmoji, fontSize = 28.sp)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = hero.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Aura: ${hero.auraGlowStyle} | Companion: ${hero.companionType}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = parsedColor,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    // Action buttons (Launch, edit, delete)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = {
                                                // Load into editor fields
                                                heroName = hero.name
                                                selectedEmoji = hero.avatarEmoji
                                                selectedColorHex = hero.outfitColorHex
                                                selectedAuraStyle = hero.auraGlowStyle
                                                selectedCompanion = hero.companionType
                                                selectedArchetype = hero.backstoryArchetype
                                                backstorySummary = hero.backstorySummary
                                                editingHeroId = hero.id
                                            },
                                            modifier = Modifier.testTag("edit_hero_${hero.id}")
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Slate300)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteHero(hero.id) },
                                            modifier = Modifier.testTag("delete_hero_${hero.id}")
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = hero.backstorySummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onLaunchStoryCreation(hero.name) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("launch_story_hero_${hero.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = parsedColor)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Weave Tale with ${hero.name}", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedAuraAndParticles(
    colorHex: String,
    auraStyle: String,
    companion: String,
    modifier: Modifier = Modifier
) {
    val auraColor = Color(android.graphics.Color.parseColor(colorHex))
    val infiniteTransition = rememberInfiniteTransition(label = "aura_effects")

    // Rotation multiplier
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "orbit_angle"
    )

    // Pulse multiplier
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("animated_creative_canvas")
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw different backgrounds depending on auraStyle
        when (auraStyle) {
            "Aurora Borealis" -> {
                val brush = Brush.linearGradient(
                    colors = listOf(auraColor.copy(alpha = 0.25f), Color.Cyan.copy(alpha = 0.1f), Color.Transparent),
                    start = Offset(0f, height * 0.2f),
                    end = Offset(width, height * 0.8f)
                )
                drawRect(brush = brush)
            }
            "Glacial Spark" -> {
                val brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), auraColor.copy(alpha = 0.15f), Color.Transparent),
                    center = center,
                    radius = width * 0.45f
                )
                drawCircle(brush = brush, center = center, radius = width * 0.45f)
            }
            "Supernova Ring" -> {
                drawCircle(
                    color = auraColor.copy(alpha = 0.4f * pulseScale),
                    radius = width * 0.26f,
                    center = center,
                    style = Stroke(width = 8f)
                )
            }
            else -> { // "Stardust Nebulae"
                val brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = 0.35f * pulseScale), Color.Transparent),
                    center = center,
                    radius = width * 0.32f
                )
                drawCircle(brush = brush, center = center, radius = width * 0.35f)
            }
        }

        // Companion Orbits
        if (companion != "None") {
            val radiusOrbit = width * 0.35f
            val rads = Math.toRadians(orbitAngle.toDouble())
            val compX = center.x + radiusOrbit * cos(rads).toFloat()
            val compY = center.y + radiusOrbit * sin(rads).toFloat()
            val compOffset = Offset(compX, compY)

            // Orbit path line
            drawCircle(
                color = GlassBorder.copy(alpha = 0.3f),
                radius = radiusOrbit,
                center = center,
                style = Stroke(width = 2f)
            )

            // Draw specific companion symbol
            val emojiRepresentation = when (companion) {
                "Mechanical Owl" -> "🦉"
                "Stardust Kitten" -> "🐱"
                "Baby Dragon" -> "🐉"
                "Cosmic Turtle" -> "🐢"
                else -> "⭐"
            }

            // Draw companion glow circle
            drawCircle(
                color = auraColor.copy(alpha = 0.6f),
                radius = 16.dp.toPx(),
                center = compOffset
            )
        }

        // Twinkling stars
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 4f, center = Offset(width * 0.15f, height * 0.25f))
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 3f, center = Offset(width * 0.82f, height * 0.3f))
        drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 5f, center = Offset(width * 0.25f, height * 0.82f))
        drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 4f, center = Offset(width * 0.78f, height * 0.75f))
    }
}

// Inline language modifier helper
private val FontWeight.Companion.大胆: FontWeight get() = FontWeight.Bold
