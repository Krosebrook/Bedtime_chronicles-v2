package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
                        .height(86.dp)
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

@Composable
fun GamePlayView(
    activeData: AdventureState.ActivePlay,
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
