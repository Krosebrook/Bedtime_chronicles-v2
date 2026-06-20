package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AppPreferences
import com.example.ui.theme.*
import com.example.util.TextToSpeechHelper
import com.example.viewmodel.AdventureState
import com.example.viewmodel.AdventureViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AdventureViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val allSaves by viewModel.allSaves.collectAsStateWithLifecycle()
    val isLoadingSave by viewModel.isLoadingSave.collectAsStateWithLifecycle()
    
    // TTS Instantiation
    var ttsHelper by remember { mutableStateOf<TextToSpeechHelper?>(null) }
    var isTtsPlaying by remember { mutableStateOf(false) }
    val prefs = remember { AppPreferences.getInstance(context) }
    val currentNarrator by prefs.ttsNarrator.collectAsStateWithLifecycle()
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Ambient Soundscape integration
    val ambientHelper = remember { com.example.util.AmbientSoundHelper() }
    var ambientSoundscape by remember { mutableStateOf("Off") }
    var ambientVolume by remember { mutableStateOf(0.40f) }

    LaunchedEffect(context) {
        ttsHelper = TextToSpeechHelper(context) { }
    }

    // Control Ambient Audio synthesis cycles
    LaunchedEffect(ambientSoundscape) {
        ambientHelper.start(ambientSoundscape)
    }

    LaunchedEffect(ambientVolume) {
        ambientHelper.setVolume(ambientVolume)
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper?.shutdown()
            ambientHelper.stop()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cosmic Adventure Maker", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                ),
                actions = {
                    if (state is AdventureState.ActivePlay) {
                        IconButton(
                            onClick = { showVoiceDialog = true },
                            modifier = Modifier.testTag("adventure_voice_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = "Choose Character Voice",
                                tint = if (currentNarrator != "default") MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                isTtsPlaying = !isTtsPlaying
                                if (isTtsPlaying) {
                                    val currentPrompt = (state as AdventureState.ActivePlay).currentPrompt
                                    ttsHelper?.speak(currentPrompt)
                                } else {
                                    ttsHelper?.stop()
                                }
                            },
                            modifier = Modifier.testTag("adventure_tts_button")
                        ) {
                            Icon(
                                imageVector = if (isTtsPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Narrate story segment",
                                tint = if (isTtsPlaying) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                ttsHelper?.stop()
                                isTtsPlaying = false
                                viewModel.startNewGame()
                            },
                            modifier = Modifier.testTag("adventure_restart_top_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stars Dynamic background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0C0721),
                                Color(0xFF040209)
                            )
                        )
                    )
            )

            // Dynamic Content
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "StateTransition"
            ) { targetState ->
                when (targetState) {
                    is AdventureState.CharacterCreation -> {
                        CharacterCreatorView(
                            viewModel = viewModel,
                            savesList = allSaves,
                            isLoading = isLoadingSave,
                            onStartAdventure = { name, hair, clothes, backstory ->
                                viewModel.initializeAdventure(name, hair, clothes, backstory)
                            },
                            onLoadSave = { slot ->
                                viewModel.loadGame(slot)
                            },
                            onDeleteSave = { slot ->
                                viewModel.deleteSave(slot)
                            }
                        )
                    }
                    is AdventureState.Loading -> {
                        GameLoadingView()
                    }
                    is AdventureState.ActivePlay -> {
                        GamePlayView(
                            activeData = targetState,
                            viewModel = viewModel,
                            allSaves = allSaves,
                            ambientSoundscape = ambientSoundscape,
                            onSoundscapeChanged = { ambientSoundscape = it },
                            ambientVolume = ambientVolume,
                            onVolumeChanged = { ambientVolume = it },
                            onChoiceSelected = { choice ->
                                if (isTtsPlaying) {
                                    ttsHelper?.stop()
                                    isTtsPlaying = false
                                }
                                viewModel.makeChoice(choice)
                            },
                            onSaveRequested = { slot ->
                                viewModel.saveGame(slot)
                            }
                        )
                    }
                    is AdventureState.Error -> {
                        GameErrorView(
                            errorMessage = targetState.message,
                            onRetry = { viewModel.startNewGame() }
                        )
                    }
                }
            }
        }
    }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Hearing,
                        contentDescription = "Character Voices",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Choose Story Teller Voice",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        "Select a soothing bedtime narrator voice to guide your child peaceably into dreamland.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val narrators = listOf(
                        Triple("default", "Standard Storyteller", "🎙️ Warm & natural bedtime voice"),
                        Triple("cosmic_sage", "Cosmic Sage", "🧙‍♂️ Deep, wise & mystical"),
                        Triple("starlight_sprite", "Starlight Sprite", "🧚‍♀️ Light, bright & bubbly"),
                        Triple("gentle_dreamer", "Gentle Dreamer", "🌙 Soft, slow & soothing")
                    )

                    narrators.forEach { (id, name, desc) ->
                        val isSelected = currentNarrator == id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    prefs.setTtsNarrator(id)
                                    // if isTtsPlaying was active, restart current index with the new voice
                                    if (isTtsPlaying && state is AdventureState.ActivePlay) {
                                        ttsHelper?.stop()
                                        ttsHelper?.speak((state as AdventureState.ActivePlay).currentPrompt)
                                    }
                                }
                                .testTag("adventure_narrator_voice_$id"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        desc,
                                        color = Slate300,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        "Active",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Done", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun CharacterCreatorView(
    viewModel: AdventureViewModel,
    savesList: List<com.example.data.AdventureSaveState>,
    isLoading: Boolean,
    onStartAdventure: (String, String, String, String) -> Unit,
    onLoadSave: (String) -> Unit,
    onDeleteSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hairColor by remember { mutableStateOf("Silver") }
    var clothingStyle by remember { mutableStateOf("Midnight Blue cloak") }
    
    val origins = listOf(
        "Starlight Foundry",
        "Dreamweaver Canopy",
        "Whispering New Nebula",
        "Indigo Abyssal Coral"
    )
    val motivations = listOf(
        "protect sleep-energy from cold night-mists",
        "guide lost baby constellations back to home orbits",
        "restore soft glowing colors to fading nurseries",
        "weave comforting dream-melodies that soothe active minds"
    )
    val pastEvents = listOf(
        "guided a sleepy space whale back to the deep indigo ocean",
        "repaired the clockwork gear of the Moon Clock during the eclipse",
        "lighted up a pitch-dark planet of sad dreams with a single starry lantern",
        "planted shimmering neon-glow seedlings across Saturn's rings"
    )

    var selectedOrigin by remember { mutableStateOf(origins[0]) }
    var selectedMotivation by remember { mutableStateOf(motivations[0]) }
    var selectedPastEvent by remember { mutableStateOf(pastEvents[0]) }
    var isBackstoryGenerating by remember { mutableStateOf(false) }

    var backstory by remember { mutableStateOf("A curious star wanderer lost in a galaxy of glowing clouds...") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome text from checking user intent (welcome message introducing creator)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .testTag("adventure_welcome_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Welcome to the Cosmic Adventure Maker! 🌌",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Design your own custom hero, appearance, and brief backstory. From there, you'll embark on a personalized, interactive bedtime story game where your choices shape the narrative. Save and load your games below!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300,
                    lineHeight = 16.sp
                )
            }
        }

        // Templates Section
        Text(
            text = "✨ OR CHOOSE A BEDTIME TEMPLATE ✨",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.templates.forEach { (tName, tHair, tClothes) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(
                            width = 1.dp,
                            color = if (name == tName) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            name = tName
                            hairColor = tHair
                            clothingStyle = tClothes
                            backstory = "A magical $tName whose $tHair hair carries stardust wishes to sleeping cosmic children..."
                        }
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (tName) {
                                "Orion" -> "🧙‍♂️"
                                "Nova" -> "🧚‍♀️"
                                "Luna" -> "🌙"
                                else -> "🪐"
                            },
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(tHair, fontSize = 10.sp, color = Slate400)
                    }
                }
            }
        }

        // Main inputs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Create Your Bedtime Adventurer",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Name
            Column {
                Text("Hero's Name", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("E.g. Captain Stardust, Whistle, Elara") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adventure_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Hair Color selection (using beautiful color-themed row selectors)
            Column {
                Text("Appearance: Hair Color", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val colorsList = listOf("Silver", "Neon Pink", "Pale Gold", "Aqua Green")
                    colorsList.forEach { col ->
                        val isSelected = hairColor == col
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { hairColor = col }
                                .padding(vertical = 8.dp)
                                .testTag("adventure_hair_${col.lowercase().replace(" ", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                col,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                }
            }

            // Clothing Style
            Column {
                Text("Appearance: Clothing Style", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = clothingStyle,
                    onValueChange = { clothingStyle = it },
                    placeholder = { Text("E.g. Cozy starry pajamas, shimmering glowing cape") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adventure_clothing_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Origins
            Column {
                Text("Hero's Cosmic Origin", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    origins.forEach { originOpt ->
                        val isSel = selectedOrigin == originOpt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable { selectedOrigin = originOpt }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("origin_chip_${originOpt.take(10).lowercase().replace(" ", "_")}")
                        ) {
                            Text(originOpt, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Motivations
            Column {
                Text("Hero's Motivation & Purpose", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    motivations.forEach { motOpt ->
                        val isSel = selectedMotivation == motOpt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable { selectedMotivation = motOpt }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("motivation_chip_${motOpt.take(10).lowercase().replace(" ", "_")}")
                        ) {
                            Text(motOpt, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Past Events
            Column {
                Text("Legendary Past Event", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pastEvents.forEach { evOpt ->
                        val isSel = selectedPastEvent == evOpt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable { selectedPastEvent = evOpt }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("past_event_chip_${evOpt.take(10).lowercase().replace(" ", "_")}")
                        ) {
                            Text(evOpt, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Ask Gemini to forge Backstory Button
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    isBackstoryGenerating = true
                    viewModel.generateDetailedBackstory(name, selectedOrigin, selectedMotivation, selectedPastEvent) { result ->
                        backstory = result
                        isBackstoryGenerating = false
                    }
                },
                enabled = name.isNotBlank() && !isBackstoryGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("forge_backstory_button")
            ) {
                if (isBackstoryGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Forging Bedtime Legend...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✨ Forge Detailed Backstory with Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Backstory
            Column {
                Text("Brief Hero Backstory (Injected into Gemini dynamic logs)", fontSize = 12.sp, color = Slate300, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = backstory,
                    onValueChange = { backstory = it },
                    placeholder = { Text("Describe their cosmic origin, special dreams, or magical powers...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("adventure_backstory_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Button to trigger
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onStartAdventure(name, hairColor, clothingStyle, backstory)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("adventure_start_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Begin Interactive Adventure 🪐", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Saving & Loading State Panel
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "SAVED ADVENTURES & CHRONICLES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            val slots = listOf("slot_1", "slot_2", "slot_3", "autosave")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slots.forEach { slot ->
                    val save = savesList.find { it.slotId == slot }
                    val label = when (slot) {
                        "slot_1" -> "Save Slot 1"
                        "slot_2" -> "Save Slot 2"
                        "slot_3" -> "Save Slot 3"
                        else -> "Last Autosave"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = if (save != null) 0.03f else 0.01f))
                            .border(1.dp, Color.White.copy(alpha = if (save != null) 0.06f else 0.02f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (save != null) MaterialTheme.colorScheme.primary else Slate500
                                )
                                if (save != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        save.saveName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "${save.characterName} (${save.hairColor} Hair, ${save.clothingStyle})",
                                        fontSize = 10.sp,
                                        color = Slate400,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(save.timestamp))
                                    Text(
                                        "Saved: $dateStr",
                                        fontSize = 9.sp,
                                        color = Slate500
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Free Slot (Ready to save progress)", fontSize = 12.sp, color = Slate500)
                                }
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (save != null) {
                                    IconButton(
                                        onClick = { onDeleteSave(slot) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                    Button(
                                        onClick = { onLoadSave(slot) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("load_slot_${slot}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("LOAD", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun GameLoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Whispering to the Gemini stars...",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate300,
                textAlign = TextAlign.Center
            )
            Text(
                "Crafting your customized journey segment",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getItemIcon(itemName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (itemName) {
        "Stardust Compass" -> Icons.Default.Explore
        "Sleeping Moonstone" -> Icons.Default.BrightnessMedium
        "Lullaby Shell" -> Icons.Default.MusicNote
        "Glowing Star Key" -> Icons.Default.Key
        "Nebula Cloud Blanket" -> Icons.Default.Cloud
        "Cosmic Dream-Lantern" -> Icons.Default.Lightbulb
        "Mechanical Skybeetle" -> Icons.Default.Settings
        else -> Icons.Default.Star
    }
}

fun getItemDescription(itemName: String): String {
    return when (itemName) {
        "Stardust Compass" -> "A brass compass with a star needle that always points toward peaceful paths."
        "Sleeping Moonstone" -> "A smooth stone pulsing with moonlight that gently takes away scary thoughts."
        "Lullaby Shell" -> "A sea shell whispering the calming rhythm of moonlit ocean waves."
        "Glowing Star Key" -> "Forged from starlight, it unlocks hidden pathways in the night sky."
        "Nebula Cloud Blanket" -> "A pocket-sized blanket of purple mist, wrapping children in deep warmth."
        "Cosmic Dream-Lantern" -> "Captures a friendly star firefly to fill bedroom corners with clean, soft glows."
        "Mechanical Skybeetle" -> "A miniature clockwork beetle humming a soothing white-noise lullaby."
        else -> "A rare starry object filled with magical bedtime vibes."
    }
}

fun getItemLore(itemName: String): String {
    return when (itemName) {
        "Stardust Compass" -> "Legends say this compass was crafted by the first Star Mapmakers of the Indigo Nebula. Its needle does not point north, but rather home towards your warmest bedtime thoughts."
        "Sleeping Moonstone" -> "Harvested under the silver light of a triple solar eclipse, this smooth moonstone absorbs and neutralizes active thoughts, cooling them into gentle dreams."
        "Lullaby Shell" -> "A spiraling sea shell that carries the echoing murmurs of deep sea whales and warm-tide currents. Press it to your ear to hear the song of Kepler’s peaceful islands."
        "Glowing Star Key" -> "Forged from a shooting star that landed in the Dream Canopies, this key has the power to unlock secret celestial doorpanels hidden in the night sky."
        "Nebula Cloud Blanket" -> "Woven with soft purple nebulous mist and stuffed with clean lavender clouds, it wraps children in a cocoon of light-weight heat and security."
        "Cosmic Dream-Lantern" -> "Hosting a friendly, non-burning cosmic firefly, this brass lantern wards off scary bedroom shadows and casts a soft starscape on the walls."
        "Mechanical Skybeetle" -> "A tiny brass skybeetle made by clockwork forest elves. When wound, its delicate gears hum a gentle white-noise frequency that relaxes active minds."
        else -> "A mystical artifact whispered of in timeless stellar legends, bringing comfort to those who hold it."
    }
}

fun getItemRarity(itemName: String): String {
    return when (itemName) {
        "Stardust Compass" -> "RARE"
        "Sleeping Moonstone" -> "LEGENDARY"
        "Lullaby Shell" -> "COMMON"
        "Glowing Star Key" -> "EPIC"
        "Nebula Cloud Blanket" -> "RARE"
        "Cosmic Dream-Lantern" -> "EPIC"
        "Mechanical Skybeetle" -> "LEGENDARY"
        else -> "MYSTIC"
    }
}

fun getItemColor(itemName: String): Color {
    return when (itemName) {
        "Stardust Compass" -> Color(0xFF4FC3F7) // light blue
        "Sleeping Moonstone" -> Color(0xFFCE93D8) // lavender
        "Lullaby Shell" -> Color(0xFFA5D6A7) // light green
        "Glowing Star Key" -> Color(0xFFFFD54F) // warm yellow
        "Nebula Cloud Blanket" -> Color(0xFFEF9A9A) // rose pink
        "Cosmic Dream-Lantern" -> Color(0xFFFFB74D) // orange glow
        "Mechanical Skybeetle" -> Color(0xFF80CBC4) // teal cyan
        else -> Color(0xFFBCB1E5)
    }
}

@Composable
fun GamePlayView(
    activeData: AdventureState.ActivePlay,
    viewModel: AdventureViewModel,
    allSaves: List<com.example.data.AdventureSaveState>,
    ambientSoundscape: String,
    onSoundscapeChanged: (String) -> Unit,
    ambientVolume: Float,
    onVolumeChanged: (Float) -> Unit,
    onChoiceSelected: (String) -> Unit,
    onSaveRequested: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var isSaveMenuOpen by remember { mutableStateOf(false) }
    var isFullscreenSatchelOpen by remember { mutableStateOf(false) }
    var selectedDetailItem by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Card Header with scene cover image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = activeData.imageUrl,
                contentDescription = "Cosmic Scene",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Dynamic layout inside picture
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        activeData.characterName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${activeData.hairColor} Hair",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Wearing: ${activeData.clothingStyle} • steps: ${activeData.history.size - 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300
                )
            }
        }

        // --- HERO'S BACKSTORY SCROLL ---
        var isBackstoryOpen by remember { mutableStateOf(false) }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isBackstoryOpen = !isBackstoryOpen }
                .testTag("adventure_backstory_scroll_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "VIEW HERO'S LEGENDARY BACKSTORY & ORIGINS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (isBackstoryOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand backstory details",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (isBackstoryOpen) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeData.backstory,
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-line adventure text segment
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "THE CHRONICLE SO FAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = activeData.currentPrompt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    lineHeight = 24.sp
                )
            }
        }

        // --- INTERACTIVE COSMIC REALMS MAP ---
        var selectedRegionId by remember { mutableStateOf("forest") }
        var activeMarkerDetailId by remember { mutableStateOf<String?>(null) }

        // Encounter State log tracking
        var previousRegion by remember { mutableStateOf(activeData.currentRegion) }
        var showEncounterNotification by remember { mutableStateOf(false) }
        var currentEncounterText by remember { mutableStateOf("") }
        var encounterLogs by remember { mutableStateOf<List<String>>(emptyList()) }

        LaunchedEffect(activeData.currentRegion) {
            if (activeData.currentRegion != previousRegion) {
                val region = activeData.currentRegion
                val message = when {
                    region.contains("Forest") -> "Arrived in Whispering Canopy Forest! You met a sleepy snail sharing slow-paced forest lullabies. 🐌🌲"
                    region.contains("Mountains") -> "Scaled the Dreampeak Mountains! Cozy nesting puffbirds snuggled close under a lavender sky. 🐦🏔️"
                    region.contains("Village") -> "Strolled into Lullaby Village! The bakery of dreams handed you warm stardust vanilla cookies. 🍪🏡"
                    else -> "Descended into Celestial Ruins! A clockwork telescope cast gentle galactic lights on your warm bed. 🪞🏛️"
                }
                currentEncounterText = message
                encounterLogs = (listOf(message) + encounterLogs).take(6)
                showEncounterNotification = true
                previousRegion = activeData.currentRegion
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adventure_cosmic_map_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "COSMIC BEDTIME STARMAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "CURRENTLY AT: ${activeData.currentRegion.uppercase()}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap starry nodes to trigger popup descriptions, review difficulty/complexity levels, or chart constellation glidepaths.",
                    fontSize = 11.sp,
                    color = Slate300
                )

                // High-polish Realtime Encounter Notification Banner
                AnimatedVisibility(
                    visible = showEncounterNotification,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adventure_encounter_alert"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "✨ REALM ENCOUNTER MEMORY!",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { showEncounterNotification = false },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color.LightGray.copy(alpha = 0.6f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentEncounterText,
                                fontSize = 11.sp,
                                color = Color.White,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                TextButton(
                                    onClick = {
                                        ttsHelper?.stop()
                                        ttsHelper?.speak("You entered a new zone. $currentEncounterText")
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Listen Narrator", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { showEncounterNotification = false },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Dismiss", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Starmap relative visual Box
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070417)) // deep cosmic background
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    // Coordinates mappings relative to size
                    val regions = listOf(
                        Triple("forest", 0.25f, 0.25f),
                        Triple("mountains", 0.75f, 0.20f),
                        Triple("village", 0.20f, 0.75f),
                        Triple("ruins", 0.80f, 0.70f)
                    )
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background stars
                        val random = java.util.Random(9876)
                        for (i in 0..18) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.15f + random.nextFloat() * 0.6f),
                                radius = 1.5.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(
                                    random.nextFloat() * size.width,
                                    random.nextFloat() * size.height
                                )
                            )
                        }

                        val coords = regions.associate { (id, rx, ry) ->
                            id to androidx.compose.ui.geometry.Offset(rx * size.width, ry * size.height)
                        }

                        val strokeWidth = 1.2.dp.toPx()
                        val pathColor = Color.White.copy(alpha = 0.12f)
                        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)

                        fun drawConnection(from: String, to: String) {
                            val f = coords[from]
                            val t = coords[to]
                            if (f != null && t != null) {
                                drawLine(color = pathColor, start = f, end = t, strokeWidth = strokeWidth, pathEffect = pathEffect)
                            }
                        }

                        drawConnection("forest", "mountains")
                        drawConnection("mountains", "ruins")
                        drawConnection("ruins", "village")
                        drawConnection("village", "forest")
                        drawConnection("forest", "ruins")
                    }

                    // Native-performance path animation helper using animateFloatAsState (the declarative compiled equivalent of Reanimated)
                    val currentTargetOffset = remember(activeData.currentRegion) {
                        when {
                            activeData.currentRegion.contains("Forest") -> androidx.compose.ui.geometry.Offset(0.25f, 0.25f)
                            activeData.currentRegion.contains("Mountains") -> androidx.compose.ui.geometry.Offset(0.75f, 0.20f)
                            activeData.currentRegion.contains("Village") -> androidx.compose.ui.geometry.Offset(0.20f, 0.75f)
                            else -> androidx.compose.ui.geometry.Offset(0.80f, 0.70f)
                        }
                    }

                    val animPathX by animateFloatAsState(
                        targetValue = currentTargetOffset.x,
                        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
                        label = "map_path_x"
                    )
                    val animPathY by animateFloatAsState(
                        targetValue = currentTargetOffset.y,
                        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
                        label = "map_path_y"
                    )

                    // Render glide traveler star sprite
                    val glideAvatarX = maxWidth * animPathX - 16.dp
                    val glideAvatarY = 200.dp * animPathY - 16.dp

                    Box(
                        modifier = Modifier
                            .offset(x = glideAvatarX, y = glideAvatarY)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 12.sp, color = Color.White)
                    }

                    // Render nodes with color complexity borders
                    val regionDataList = listOf(
                        Triple("forest", "🌲", Color(0xFFA5D6A7)),
                        Triple("mountains", "🏔️", Color(0xFFCE93D8)),
                        Triple("village", "🏡", Color(0xFFFFD54F)),
                        Triple("ruins", "🏛️", Color(0xFF80CBC4))
                    )

                    regionDataList.forEach { (id, emoji, color) ->
                        val matchingItem = regions.first { it.first == id }
                        val relativeX = matchingItem.second
                        val relativeY = matchingItem.third

                        val nodeX = maxWidth * relativeX - 22.dp
                        val nodeY = 200.dp * relativeY - 22.dp

                        val regionName = when (id) {
                            "forest" -> "Whispering Canopy Forest"
                            "mountains" -> "Dreampeak Mountains"
                            "village" -> "Lullaby Village"
                            else -> "Echoing Celestial Ruins"
                        }

                        val isCurrent = activeData.currentRegion == regionName
                        val isSelected = selectedRegionId == id

                        Box(
                            modifier = Modifier
                                .offset(x = nodeX, y = nodeY)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) color.copy(alpha = 0.22f)
                                    else if (isSelected) Color.White.copy(alpha = 0.12f)
                                    else Color.White.copy(alpha = 0.04f)
                                )
                                .border(
                                    width = if (isCurrent) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isCurrent) color else if (isSelected) Color.White else Color.White.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                                .clickable { 
                                    selectedRegionId = id
                                    activeMarkerDetailId = id // triggers interactive popup marker component
                                }
                                .testTag("map_node_$id"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(4.dp, color.copy(alpha = 0.15f), CircleShape)
                                )
                            }

                            // Complexity visual star badge at the bottom of the node representing level
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 5.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = when (id) {
                                        "forest" -> "L1"
                                        "mountains" -> "L2"
                                        "village" -> "L3"
                                        else -> "L4"
                                    },
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail section for selected region
                val selectedRegion = remember(selectedRegionId) {
                    when (selectedRegionId) {
                        "forest" -> Triple(
                            "Whispering Canopy Forest 🌲",
                            "A warm forest of glowing bioluminescent trees, soft plush moss carpets, and a sweet sleepy breeze singing high in the leaves.",
                            Color(0xFFA5D6A7)
                        )
                        "mountains" -> Triple(
                            "Dreampeak Mountains 🏔️",
                            "Mountain peaks made of purple candy floss and snowy sugar clouds, reflecting the quiet stars of the sleep Milky Way.",
                            Color(0xFFCE93D8)
                        )
                        "village" -> Triple(
                            "Lullaby Village 🏡",
                            "Star-roofed cottages lit by cozy warm lanterns, where chimneys drift sweet marshmallow smoke.",
                            Color(0xFFFFD54F)
                        )
                        else -> Triple(
                            "Echoing Celestial Ruins 🏛️",
                            "Calm pillar arches built of starlight obsidian under a quiet planetarium sky, preserving ancient dreams of the star makers.",
                            Color(0xFF80CBC4)
                        )
                    }
                }

                val targetRegionName = selectedRegion.first.removeSuffix(" 🌲").removeSuffix(" 🏔️").removeSuffix(" 🏡").removeSuffix(" 🏛️")
                val isCharacterHere = activeData.currentRegion == targetRegionName

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(selectedRegion.third.copy(alpha = 0.04f))
                        .border(1.dp, selectedRegion.third.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedRegion.first,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedRegion.third
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isCharacterHere) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(selectedRegion.third.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "📍 YOU ARE RESTING HERE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedRegion.third
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedRegion.second,
                            fontSize = 11.sp,
                            color = Slate300,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Suggested Sleepy Encounters for kids
                        Text(
                            "✨ POTENTIAL SLEEPY ENCOUNTERS:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectedRegion.third,
                            letterSpacing = 0.5.sp
                        )
                        
                        val encountersList = remember(selectedRegionId) {
                            when (selectedRegionId) {
                                "forest" -> listOf(
                                    "Sleeping snail sharing a slow-paced bedtime poem 🐌",
                                    "The Slumber Bear offering honey-coated chamomile stardust tea 🐻",
                                    "A grove of neon dreamflowers breathing quiet lullabies 🌸"
                                )
                                "mountains" -> listOf(
                                    "A stargazing mountain goat naming clean constellations 🐐",
                                    "Cozy nesting puffbirds resting high under a lavender sky 🐦",
                                    "A crystalline white-light cavern humming comforting deep white-noise 💎"
                                )
                                "village" -> listOf(
                                    "The Bakery of Dreams handing out warm vanilla sleepy cookies 🍪",
                                    "Star-weaving elves spinning starlight bedtime pajamas 🧶",
                                    "A central star fountain splashing warm, gentle lavender water ⛲"
                                )
                                else -> listOf(
                                    "A clockwork telescope displaying soft galaxy projections on clouds 🔭",
                                    "The Mirror of Slumber showing your hero cozy in bed 🪞",
                                    "A friendly sleeping dragon curled like a kitten around star chest 🐉"
                                )
                            }
                        }

                        encountersList.forEach { encounter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(selectedRegion.third)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = encounter,
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                            }
                        }

                        // Action Button
                        if (!isCharacterHere) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val finalEncounterText = encountersList.random()
                                    viewModel.travelToRegion(targetRegionName, selectedRegion.second, finalEncounterText)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = selectedRegion.third),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("travel_to_${selectedRegionId}_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Travel & Meet Resident (" + targetRegionName.substringBefore(" ") + ")",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // Historic Journey Log panel
                if (encounterLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "📔 ADVENTURE ENCOUNTER LOG",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        encounterLogs.forEachIndexed { idx, log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${idx + 1}.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(16.dp)
                                )
                                Text(
                                    text = log,
                                    fontSize = 10.sp,
                                    color = Slate300,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- INTERACTIVE POP-UP DIALOG MARKER COMPONENT ---
        if (activeMarkerDetailId != null) {
            val rId = activeMarkerDetailId!!
            val rData = when (rId) {
                "forest" -> Triple(
                    "Whispering Canopy Forest 🌲",
                    "A warm forest of glowing bioluminescent trees, soft plush moss carpets, and a sweet sleepy breeze singing high in the leaves. Beautiful and easy pace for little dreamers.",
                    Color(0xFFA5D6A7)
                )
                "mountains" -> Triple(
                    "Dreampeak Mountains 🏔️",
                    "Mountain peaks made of purple candy floss and snowy sugar clouds, reflecting the quiet stars of the sleep Milky Way. Excellent visualization cues.",
                    Color(0xFFCE93D8)
                )
                "village" -> Triple(
                    "Lullaby Village 🏡",
                    "Star-roofed cottages lit by cozy warm lanterns, where chimneys drift sweet marshmallow smoke. Filled with friendly neighbors who offer warm cookies.",
                    Color(0xFFFFD54F)
                )
                else -> Triple(
                    "Echoing Celestial Ruins 🏛️",
                    "Calm pillar arches built of starlight obsidian under a quiet planetarium sky, preserving ancient dreams of the star makers.",
                    Color(0xFF80CBC4)
                )
            }

            val ratingText = when (rId) {
                "forest" -> "Gentle Breeze • Level 1"
                "mountains" -> "Mindful Peak • Level 2"
                "village" -> "Cozy Story • Level 3"
                else -> "Cosmic Mystery • Level 4"
            }

            AlertDialog(
                onDismissRequest = { activeMarkerDetailId = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rData.first,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(rData.third.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "STORY COMPLEXITY: $ratingText",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = rData.third
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = rData.second,
                            fontSize = 11.sp,
                            color = Slate300,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "✨ Bedtime Lore & Magic:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = rData.third
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (rId) {
                                "forest" -> "Home to sleep-loving glow bugs who dance in calm circles, slowing down racing minds with their gold dust outlines."
                                "mountains" -> "Protected by gentle breeze giants whose only task is to blow warm, fluffy dream-clouds over sleepy young heads."
                                "village" -> "Synchronized perfectly with the cosmic starlight, every chimney releases sweet dreams to assure soft, safe sleep."
                                else -> "Contains the starlight clock generator. Visitors are guided into peaceful relaxation by its soothing stardust rhythms."
                            },
                            fontSize = 11.sp,
                            color = Slate300,
                            lineHeight = 15.sp
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val regionName = rData.first.removeSuffix(" 🌲").removeSuffix(" 🏔️").removeSuffix(" 🏡").removeSuffix(" 🏛️")
                        val isHere = activeData.currentRegion == regionName

                        TextButton(onClick = { activeMarkerDetailId = null }) {
                            Text("Dismiss", color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (!isHere) {
                            Button(
                                onClick = {
                                    val encounters = when (rId) {
                                        "forest" -> listOf(
                                            "Sleeping snail sharing a slow-paced bedtime poem 🐌",
                                            "The Slumber Bear offering honey-coated chamomile stardust tea 🐻",
                                            "A grove of neon dreamflowers breathing quiet lullabies 🌸"
                                        )
                                        "mountains" -> listOf(
                                            "A stargazing mountain goat naming clean constellations 🐐",
                                            "Cozy nesting puffbirds resting high under a lavender sky 🐦",
                                            "A crystalline white-light cavern humming comforting deep white-noise 💎"
                                        )
                                        "village" -> listOf(
                                            "The Bakery of Dreams handing out warm vanilla sleepy cookies 🍪",
                                            "Star-weaving elves spinning starlight bedtime pajamas 🧶",
                                            "A central star fountain splashing warm, gentle lavender water ⛲"
                                        )
                                        else -> listOf(
                                            "A clockwork telescope displaying soft galaxy projections on clouds 🔭",
                                            "The Mirror of Slumber showing your hero cozy in bed 🪞",
                                            "A friendly sleeping dragon curled like a kitten around star chest 🐉"
                                        )
                                    }
                                    viewModel.travelToRegion(regionName, rData.second, encounters.random())
                                    activeMarkerDetailId = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = rData.third)
                            ) {
                                Text("Travel Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                containerColor = Color(0xFF0F0B24)
            )
        }

        // Ambient Soundscape Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("adventure_ambient_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "BEDTIME SOUNDSCAPES & ATMOSPHERE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (ambientSoundscape != "Off") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Playing: $ambientSoundscape",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Wrap your adventure in deep cosmic hums or soothing nature soundscapes.",
                    fontSize = 11.sp,
                    color = Slate300
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Nature Sounds Category
                Text(
                    "🌲 Nature Soundscapes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val natureSounds = listOf(
                        Triple("Gentle Rain", "🌧️", "adventure_ambient_rain"),
                        Triple("Cozy Waves", "🌊", "adventure_ambient_waves"),
                        Triple("Forest Breeze", "🍃", "adventure_ambient_breeze")
                    )
                    natureSounds.forEach { (name, emoji, tag) ->
                        val isSelected = ambientSoundscape == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSoundscapeChanged(if (isSelected) "Off" else name) }
                                .padding(vertical = 8.dp)
                                .testTag(tag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Space Sounds Category
                Text(
                    "🌌 Space-Themed Soundscapes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val spaceSounds = listOf(
                        Triple("Space Echoes", "🪐", "adventure_ambient_echoes"),
                        Triple("Nebula Whisper", "🌌", "adventure_ambient_whisper"),
                        Triple("Starfield Hum", "🌟", "adventure_ambient_hum")
                    )
                    spaceSounds.forEach { (name, emoji, tag) ->
                        val isSelected = ambientSoundscape == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSoundscapeChanged(if (isSelected) "Off" else name) }
                                .padding(vertical = 8.dp)
                                .testTag(tag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White)
                            }
                        }
                    }
                }

                if (ambientSoundscape != "Off") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = ambientVolume,
                            onValueChange = onVolumeChanged,
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .testTag("adventure_ambient_volume_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.tertiary,
                                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(ambientVolume * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Slate300,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // --- COSMIC SATCHEL INVENTORY CARD ---
        var foundItemPopup by remember { mutableStateOf("") }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("adventure_satchel_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "COSMIC SATCHEL & INVENTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        "${activeData.inventory.size}/5 Items",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collect magical artifacts along your journey. Use them to creatively guide your story scene!",
                        fontSize = 11.sp,
                        color = Slate300,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (activeData.inventory.isNotEmpty()) {
                                selectedDetailItem = activeData.inventory.first()
                            } else {
                                selectedDetailItem = null
                            }
                            isFullscreenSatchelOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("open_fullscreen_satchel_button")
                    ) {
                        Text("✨ BAG MODAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeData.inventory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.01f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Your satchel is empty. Search below to discover treasures!", fontSize = 11.sp, color = Slate500)
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeData.inventory.forEach { item ->
                            val desc = remember(item) { getItemDescription(item) }
                            val icon = remember(item) { getItemIcon(item) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f))
                                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(desc, fontSize = 10.sp, color = Slate400, maxLines = 2, lineHeight = 13.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Drop item
                                    IconButton(
                                        onClick = { viewModel.removeItemFromInventory(item) },
                                        modifier = Modifier.size(32.dp).testTag("drop_$item")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Discard item", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    }
                                    
                                    // Use item
                                    Button(
                                        onClick = { viewModel.useItemFromInventory(item) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(28.dp).testTag("use_$item")
                                    ) {
                                        Text("USE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action to find treasure
                Button(
                    onClick = {
                        viewModel.findTreasures { found ->
                            foundItemPopup = found
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("search_treasure_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔍 Search Surroundings for Hidden Treasures", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // --- EXTREMELY TACTILE DISCOVERY DIALOGUE ---
        if (foundItemPopup.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { foundItemPopup = "" },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Starry Discovery! 🌟", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            "You discovered a rare starry cosmic item:",
                            fontSize = 13.sp,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    getItemIcon(foundItemPopup),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(foundItemPopup, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(getItemDescription(foundItemPopup), fontSize = 11.sp, color = Slate400, lineHeight = 14.sp)
                                }
                            }
                        }
                        if (activeData.inventory.size >= 5) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "⚠️ Warning: Your satchel is full (5/5). Discard an item from your list before keeping this one.",
                                fontSize = 11.sp,
                                color = Color.Red.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addItemToInventory(foundItemPopup)
                            foundItemPopup = ""
                        },
                        enabled = activeData.inventory.size < 5,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add to Satchel", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { foundItemPopup = "" }) {
                        Text("Leave It", color = Slate400)
                    }
                },
                containerColor = Color(0xFF140D2F),
                textContentColor = Color.White,
                titleContentColor = Color.White
            )
        }

        // --- FULLSCREEN COSMIC SATCHEL INTERACTIVE MODAL ---
        if (isFullscreenSatchelOpen) {
            Dialog(
                onDismissRequest = { isFullscreenSatchelOpen = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("fullscreen_satchel_dialog"),
                    color = Color(0xFF0C0721) // Deep space midnight background
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth >= 600.dp
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // --- Modal Header ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🌌 COSMIC SATCHEL",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "High-fidelity interactive containment of bedtime artifacts",
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                                
                                // Close button
                                IconButton(
                                    onClick = { isFullscreenSatchelOpen = false },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                        .testTag("fullscreen_satchel_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close satchel modal",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // If inventory is empty
                            if (activeData.inventory.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkOutline,
                                            contentDescription = null,
                                            tint = Slate500,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Your Satchel is Empty",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Explore the bedtime regions and search the surroundings during your active adventures to discover legendary artifacts of power.",
                                            fontSize = 12.sp,
                                            color = Slate400,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            } else {
                                // Validate if selected item is still in list. If not, default to first item
                                val currentItems = activeData.inventory
                                val safeSelectedItem = if (currentItems.contains(selectedDetailItem)) {
                                    selectedDetailItem
                                } else {
                                    currentItems.firstOrNull()
                                }
                                selectedDetailItem = safeSelectedItem

                                // Dynamic UI layout based on screen width
                                if (isWideScreen) {
                                    // Wide screen layout (Split Screen Side-by-Side)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Left - Item selection list (takes 1.2 fraction)
                                        Column(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .fillMaxHeight()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                "SATCHEL CARRIER (${activeData.inventory.size}/5)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate400,
                                                letterSpacing = 0.5.sp
                                            )
                                            activeData.inventory.forEach { item ->
                                                val isSelected = safeSelectedItem == item
                                                SatchelListItem(
                                                    itemName = item,
                                                    isSelected = isSelected,
                                                    onClick = { selectedDetailItem = item }
                                                )
                                            }
                                        }

                                        // Right - Comprehensive detailed view of the selected item
                                        Box(
                                            modifier = Modifier
                                                .weight(1.8f)
                                                .fillMaxHeight()
                                        ) {
                                            safeSelectedItem?.let { item ->
                                                ItemDetailChamber(
                                                    itemName = item,
                                                    viewModel = viewModel,
                                                    onUsed = { isFullscreenSatchelOpen = false },
                                                    onDiscarded = {
                                                        val remaining = activeData.inventory.filter { it != item }
                                                        selectedDetailItem = remaining.firstOrNull()
                                                    }
                                                )
                                            } ?: Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Select an item to view properties", color = Slate400, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    // Compact screen layout (Vertical Stack)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Top section: horizontal chip selector
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                "SATCHEL CARRIER (${activeData.inventory.size}/5)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate400,
                                                letterSpacing = 0.5.sp
                                            )
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                activeData.inventory.forEach { item ->
                                                    val isSelected = safeSelectedItem == item
                                                    CompactItemChip(
                                                        itemName = item,
                                                        isSelected = isSelected,
                                                        onClick = { selectedDetailItem = item }
                                                    )
                                                }
                                            }
                                        }

                                        // Bottom section: Detail view of selected item
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            safeSelectedItem?.let { item ->
                                                ItemDetailChamber(
                                                    itemName = item,
                                                    viewModel = viewModel,
                                                    onUsed = { isFullscreenSatchelOpen = false },
                                                    onDiscarded = {
                                                        val remaining = activeData.inventory.filter { it != item }
                                                        selectedDetailItem = remaining.firstOrNull()
                                                    }
                                                )
                                            } ?: Box(
                                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Select an item to view properties", color = Slate400, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Options
        Text(
            text = "🌌 MAKE YOUR CHOICE 🌌",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activeData.choices.forEachIndexed { idx, choice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChoiceSelected(choice) }
                        .testTag("adventure_choice_$idx"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "0${idx + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = choice,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Saving UI slots modal / panel triggered right within screen
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { isSaveMenuOpen = !isSaveMenuOpen },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trigger_save_panel_btn")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSaveMenuOpen) "Hide Save Interface" else "Save Current Adventure State", color = Color.White)
        }

        if (isSaveMenuOpen) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.02f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Choose a slot to save progress:",
                    fontSize = 12.sp,
                    color = Slate300,
                    fontWeight = FontWeight.SemiBold
                )

                val saveSlots = listOf("slot_1", "slot_2", "slot_3")
                saveSlots.forEach { slot ->
                    val existing = allSaves.find { it.slotId == slot }
                    val label = when (slot) {
                        "slot_1" -> "Save Slot 1"
                        "slot_2" -> "Save Slot 2"
                        else -> "Save Slot 3"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable {
                                onSaveRequested(slot)
                                isSaveMenuOpen = false
                            }
                            .padding(10.dp)
                            .testTag("save_slot_$slot"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    text = if (existing != null) "Overwrites: ${existing.saveName}" else "Empty Slot",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                        }
                        Text("SAVE PROGRESS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun GameErrorView(errorMessage: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "An Error Occurred",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate300,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Start Creative Journey Again")
            }
        }
    }
}

@Composable
fun SatchelListItem(
    itemName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = remember(itemName) { getItemIcon(itemName) }
    val itemColor = remember(itemName) { getItemColor(itemName) }
    val rarity = remember(itemName) { getItemRarity(itemName) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.02f))
            .border(
                width = 1.dp,
                color = if (isSelected) itemColor else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("fullscreen_item_card_${itemName.replace(" ", "_").lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(itemColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = itemColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = itemName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(itemColor.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = rarity,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = itemColor
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(itemColor)
            )
        }
    }
}

@Composable
fun CompactItemChip(
    itemName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = remember(itemName) { getItemIcon(itemName) }
    val itemColor = remember(itemName) { getItemColor(itemName) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(if (isSelected) itemColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
            .border(
                width = 1.dp,
                color = if (isSelected) itemColor else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("fullscreen_item_card_compact_${itemName.replace(" ", "_").lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) itemColor else Slate300,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = itemName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) itemColor else Color.White
        )
    }
}

@Composable
fun ItemDetailChamber(
    itemName: String,
    viewModel: AdventureViewModel,
    onUsed: () -> Unit,
    onDiscarded: () -> Unit
) {
    val icon = remember(itemName) { getItemIcon(itemName) }
    val itemColor = remember(itemName) { getItemColor(itemName) }
    val desc = remember(itemName) { getItemDescription(itemName) }
    val lore = remember(itemName) { getItemLore(itemName) }
    val rarity = remember(itemName) { getItemRarity(itemName) }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Immersive large spinning icon orb
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(itemColor.copy(alpha = 0.08f))
                    .border(1.5.dp, itemColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(itemColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$itemName visualizer",
                        tint = itemColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grand Heading & Rarity Badge
            Text(
                text = itemName.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(itemColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(itemColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$rarity BEDTIME ARTIFACT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = itemColor,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main gameplay function description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.015f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "PRIMARY BEDTIME EFFECT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Immersive mythology/lore explanation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.015f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "CELESTIAL MYTHOLOGY & LORE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lore,
                    fontSize = 11.sp,
                    color = Slate300,
                    lineHeight = 16.sp,
                    style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.FontStyle.Italic)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controls/Button section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Use Item button
                Button(
                    onClick = {
                        viewModel.useItemFromInventory(itemName)
                        onUsed()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("fullscreen_use_button_${itemName.replace(" ", "_").lowercase()}"),
                    colors = ButtonDefaults.buttonColors(containerColor = itemColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0C0721)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "USE THIS ARTIFACT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C0C2C)
                    )
                }

                // Discard Button
                TextButton(
                    onClick = {
                        viewModel.removeItemFromInventory(itemName)
                        onDiscarded()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("fullscreen_discard_button_${itemName.replace(" ", "_").lowercase()}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DISCARD FROM SATCHEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
