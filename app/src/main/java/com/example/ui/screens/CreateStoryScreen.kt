package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.Slate300
import com.example.viewmodel.GenerationState
import com.example.viewmodel.StoryGenerationViewModel
import com.airbnb.lottie.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    onBack: () -> Unit,
    onStoryGenerated: (String) -> Unit, // Ideally navigate to reader
    initialHeroName: String? = null
) {
    val viewModel: StoryGenerationViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isCachedResult by viewModel.isCachedResult.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { com.example.data.AppPreferences.getInstance(context) }
    
    var heroName by remember { mutableStateOf(initialHeroName ?: "") }
    var selectedGenre by remember { mutableStateOf("Adventure") }
    var setting by remember { mutableStateOf("A glowing forest on a distant planet") }
    var keywords by remember { mutableStateOf("") }
    var genreDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPersona by remember { mutableStateOf("Gentle Grandma") }
    var selectedHeroId by remember { mutableStateOf<String?>(null) }
    
    val allGenres = listOf("Space Exploration", "Mythical Creatures", "Gentle Bedtime", "Adventure", "Fantasy", "Magic", "Cosmic", "Sleepy")

    LaunchedEffect(state) {
        if (state is GenerationState.Success) {
            val storyId = (state as GenerationState.Success).story.id
            if (heroName.isNotEmpty()) {
                prefs.setHeroUsed(heroName)
            }
            prefs.incrementModeStoryCount(selectedGenre)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Craft a New Tale", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state is GenerationState.Error) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                    Text((state as GenerationState.Error).message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (state is GenerationState.Success) {
                val story = (state as GenerationState.Success).story
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth().testTag("story_generated_card")) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isCachedResult) {
                            Text("Offline Bedtime Adventure Loaded!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center, modifier = Modifier.testTag("offline_story_title_label"))
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(bottom = 8.dp).testTag("offline_badge")) {
                                Text("📶 Offline / Cozy Mode Active", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        } else {
                            Text("Story Created!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.testTag("story_created_label"))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(story.title, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isCachedResult) {
                            Text("Your device is offline or network is limited. We've instantly retrieved an adventure matching your theme from your library to keep bedtimes restful!", textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        } else {
                            Text("Your cover image and story have been generated and saved to your library.", textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.testTag("create_another_button")) {
                                Text("Create Another")
                            }
                            Button(onClick = { 
                                onStoryGenerated(story.id)
                                viewModel.reset() 
                            }, modifier = Modifier.testTag("read_story_button")) {
                                Text("Read Story")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state !is GenerationState.Success && state !is GenerationState.Generating) {
                val db = remember { com.example.data.DatabaseProvider.getDatabase(context) }
                val customHeroesList by db.customHeroDao().getAllCustomHeroes().collectAsStateWithLifecycle(initialValue = emptyList())

                if (customHeroesList.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(
                            text = "Your Created Bedtime Heroes",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).testTag("select_custom_hero_label")
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            items(customHeroesList) { customHero ->
                                val isSelected = heroName == customHero.name
                                val parsedColor = Color(android.graphics.Color.parseColor(customHero.outfitColorHex))
                                Card(
                                    onClick = {
                                        selectedHeroId = null
                                        heroName = customHero.name
                                        setting = "A stellar, dreamy atmosphere where ${customHero.name} embarks on an adventure."
                                        keywords = "${customHero.auraGlowStyle}, ${customHero.companionType}, starry skies, tranquil clouds"
                                        selectedGenre = "Cosmic"
                                    },
                                    modifier = Modifier
                                        .width(162.dp)
                                        .height(150.dp)
                                        .testTag("custom_hero_choice_${customHero.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            parsedColor.copy(alpha = 0.22f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        }
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) parsedColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(parsedColor.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(customHero.avatarEmoji, fontSize = 20.sp)
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = parsedColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        
                                        Column {
                                            Text(
                                                text = customHero.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = customHero.backstoryArchetype,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate300,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                        
                                        Text(
                                            text = "Glow: ${customHero.auraGlowStyle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = parsedColor,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Hero Templates Selector Carousel
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(
                        text = "Choose a Bedtime Hero Template",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).testTag("select_hero_label")
                    )
                    Text(
                        text = "Tap a cosmic hero to instantly configure your bedtime story parameters!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        items(com.example.data.BedtimeAssets.HEROES) { hero ->
                            val isSelected = selectedHeroId == hero.id
                            Card(
                                onClick = {
                                    selectedHeroId = hero.id
                                    heroName = hero.name
                                    setting = "A quiet, peaceful planetary haven located inside ${hero.constellation}."
                                    keywords = "${hero.power}, stardust, beautiful nightlights, soft whispers"
                                    selectedGenre = when (hero.name) {
                                        "Nova", "Whistle" -> "Space Exploration"
                                        "Coral" -> "Adventure"
                                        "Orion" -> "Space Exploration"
                                        "Luna", "Shade" -> "Gentle Bedtime"
                                        "Nimbus" -> "Mythical Creatures"
                                        "Bloom" -> "Mythical Creatures"
                                        else -> "Adventure"
                                    }
                                },
                                modifier = Modifier
                                    .width(162.dp)
                                    .height(172.dp)
                                    .testTag("hero_choice_${hero.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        hero.color.copy(alpha = 0.22f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    }
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) hero.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Brush.linearGradient(hero.gradientColors)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = hero.icon,
                                                contentDescription = hero.name,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = hero.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    Column {
                                        Text(
                                            text = hero.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = hero.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate300,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    Text(
                                        text = "Power: ${hero.power}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = hero.color,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = heroName,
                    onValueChange = { 
                        heroName = it
                        selectedHeroId = null // clear preset when custom name typed
                    },
                    label = { Text("Hero's Name") },
                    modifier = Modifier.fillMaxWidth().testTag("hero_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Keywords (e.g. planet, robot, dragon)") },
                    placeholder = { Text("Enter magical bedtime ingredients...") },
                    modifier = Modifier.fillMaxWidth().testTag("keywords_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Story Theme & Focus",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("select_theme_label"),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    items(com.example.data.BedtimeAssets.CONTENT_THEMES) { themeItem ->
                        val isSelected = selectedGenre.equals(themeItem.label, ignoreCase = true)
                        Card(
                            onClick = { selectedGenre = themeItem.label },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            ),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            },
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("theme_chip_${themeItem.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(themeItem.emoji, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(themeItem.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = genreDropdownExpanded,
                    onExpandedChange = { genreDropdownExpanded = !genreDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedGenre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Genre") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = genreDropdownExpanded,
                        onDismissRequest = { genreDropdownExpanded = false }
                    ) {
                        allGenres.forEach { genre ->
                            DropdownMenuItem(
                                text = { Text(genre) },
                                onClick = {
                                    selectedGenre = genre
                                    genreDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Narrator Persona",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                val personaList = listOf(
                    Triple("Gentle Grandma", "Warm, slow-paced, & loving cozy voice", Icons.Default.Face),
                    Triple("Exciting Explorer", "Adventurous, curious, & starry-eyed voice", Icons.Default.Explore),
                    Triple("Classic Bard", "Poetic, rhythmic, & lyrical storytelling", Icons.Default.MenuBook),
                    Triple("Cosmic Wizard", "Serene, mysterious, & stardust atmosphere", Icons.Default.AutoAwesome)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    personaList.forEach { (name, desc, icon) ->
                        val isSelected = selectedPersona == name
                        Card(
                            onClick = { selectedPersona = name },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            ),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("persona_card_$name")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = setting,
                    onValueChange = { setting = it },
                    label = { Text("Setting") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.generateStory(heroName.ifEmpty { "Finn" }, selectedGenre, setting, keywords, selectedPersona) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Generate Story", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else if (state is GenerationState.Generating) {
                // Visual Skeleton Loading Screen
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Weaving your magical tale. Please wait...", color = Slate300)
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Cover Image Skeleton with Cosmic Spinner & Lottie animation
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CosmicLottieAnimation(
                                modifier = Modifier
                                    .size(120.dp)
                                    .testTag("generating_story_lottie")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Weaving star dust and stories...", fontSize = 12.sp, color = Slate300)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Title Skeleton
                    Box(modifier = Modifier.width(200.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).background(GlassWhite))
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.width(100.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(GlassWhite))
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Paragraph Skeletons
                    repeat(3) {
                        Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).background(GlassWhite))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).background(GlassWhite))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(GlassWhite))
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicLoadingSpinner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_spinner")
    
    // Slow outer orbit rotation
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "outer_rotation"
    )
    
    // Quick inner orbit rotation (reversed)
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ),
        label = "inner_rotation"
    )
    
    // Core expansion pulse
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )
    
    // Starlight twinkle offset/alpha
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle_alpha"
    )

    Canvas(modifier = modifier.size(120.dp)) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        
        // 1. Core Glowing Nebula
        val nebulaBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFC084FC).copy(alpha = 0.8f * twinkleAlpha), // Glowing purple core
                Color(0xFF6366F1).copy(alpha = 0.4f),               // Deep indigo transition
                Color.Transparent
            ),
            center = center,
            radius = width * 0.22f * corePulse
        )
        drawCircle(
            brush = nebulaBrush,
            radius = width * 0.35f,
            center = center
        )
        
        // Solid core center star
        drawCircle(
            color = Color(0xFFFDE047).copy(alpha = 0.9f), // Glowing gold celestial star
            radius = width * 0.06f * corePulse,
            center = center
        )

        // 2. Beautiful Outer Cosmic Orbit Ring (dashed, glowing)
        rotate(rotationOuter, pivot = center) {
            drawCircle(
                color = Color(0xFF818CF8).copy(alpha = 0.5f), // Indigo cosmic ring
                radius = width * 0.42f,
                center = center,
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(25f, 15f),
                        phase = 0f
                    )
                )
            )
            
            // "Planet" orbiting on outer ring
            drawCircle(
                color = Color(0xFFA78BFA), // Pastel Violet planet
                radius = width * 0.05f,
                center = Offset(center.x + width * 0.42f, center.y)
            )
            
            // Tiny satellite
            drawCircle(
                color = Color(0xFF38BDF8), // Cyan moon
                radius = width * 0.02f,
                center = Offset(center.x + width * 0.42f - 15f, center.y - 15f)
            )
        }

        // 3. Inner Orbit Ring (reversed)
        rotate(rotationInner, pivot = center) {
            drawCircle(
                color = Color(0xFFF472B6).copy(alpha = 0.4f), // Soft pink ring
                radius = width * 0.28f,
                center = center,
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(15f, 10f),
                        phase = 0f
                    )
                )
            )
            
            // Sparkling star on the inner orbit
            drawCircle(
                color = Color(0xFFFDE047), // Gold star
                radius = width * 0.035f,
                center = Offset(center.x + width * 0.28f, center.y)
            )
        }
        
        // 4. Ambient Twinkling Cosmic Orbs in background
        drawCircle(
            color = Color(0xFF22D3EE).copy(alpha = twinkleAlpha * 0.8f), // Glowing cyan speck
            radius = 3f,
            center = Offset(width * 0.2f, height * 0.3f)
        )
        drawCircle(
            color = Color(0xFFC084FC).copy(alpha = (1.0f - twinkleAlpha) * 0.8f), // Twinkling purple speck
            radius = 4f,
            center = Offset(width * 0.8f, height * 0.25f)
        )
        drawCircle(
            color = Color(0xFFFDE047).copy(alpha = twinkleAlpha * 0.7f), // Twinkling gold speck
            radius = 2.5f,
            center = Offset(width * 0.35f, height * 0.85f)
        )
    }
}

@Composable
fun CosmicLottieAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.example.R.raw.cosmic_loader)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}
