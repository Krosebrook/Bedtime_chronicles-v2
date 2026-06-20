package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AppPreferences
import com.example.data.DatabaseProvider
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.viewmodel.UserProfileViewModel
import com.example.viewmodel.UserProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, onNavigateToHelp: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { DatabaseProvider.getDatabase(context).userProfileDao() }
    val factory = remember { UserProfileViewModelFactory(dao) }
    val viewModel: UserProfileViewModel = viewModel(factory = factory)
    
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    var isEditing by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(userProfile) {
        if (userProfile != null && !isEditing) {
            username = userProfile?.username ?: ""
            biography = userProfile?.biography ?: ""
            avatarUri = userProfile?.avatarUri
        }
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Ignore if not a persistable URI
            }
            avatarUri = it.toString()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            viewModel.updateProfile(username, biography, avatarUri)
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(GlassWhite)
                    .clickable(enabled = isEditing) {
                        launcher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    val isEmoji = com.example.data.BedtimeAssets.AVATAR_EMOJIS.contains(avatarUri)
                    if (isEmoji) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatarUri!!, fontSize = 64.sp)
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Avatar Placeholder",
                        modifier = Modifier.size(80.dp),
                        tint = Slate400
                    )
                }
                
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change Avatar", tint = Color.White)
                    }
                }
            }
            
            if (isEditing) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Or choose a magical avatar symbol:", fontSize = 12.sp, color = Slate300)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    items(com.example.data.BedtimeAssets.AVATAR_EMOJIS) { emoji ->
                        val isSelected = avatarUri == emoji
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else GlassWhite.copy(alpha = 0.1f))
                                .border(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { avatarUri = emoji }
                                .testTag("avatar_emoji_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val appPreferences = AppPreferences.getInstance(context)
            val isDarkMode by appPreferences.isDarkMode.collectAsStateWithLifecycle()
            val isMidnightMode by appPreferences.isMidnightMode.collectAsStateWithLifecycle()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deep-Space Dark Mode", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { appPreferences.setDarkMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
            
            if (isDarkMode) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        "Bedtime Theme Experience",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Switch styles to protect young eyes in late-night reading environments.",
                        fontSize = 12.sp,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cosmic Button
                        Button(
                            onClick = { appPreferences.setMidnightMode(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isMidnightMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (!isMidnightMode) Color.Black else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).border(
                                width = 1.dp,
                                color = if (isMidnightMode) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ).testTag("theme_mode_cosmic")
                        ) {
                            Text("✨ Cosmic", fontWeight = FontWeight.Bold)
                        }
                        // Midnight Button
                        Button(
                            onClick = { appPreferences.setMidnightMode(true) },
                            modifier = Modifier.weight(1f).border(
                                width = 1.dp,
                                color = if (!isMidnightMode) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ).testTag("theme_mode_midnight"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMidnightMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isMidnightMode) Color.Black else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🦉 Midnight", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Text-to-Speech Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                
                // Pitch setting
                val ttsPitch by appPreferences.ttsPitch.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speech Pitch",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.1fx", ttsPitch),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = ttsPitch,
                    onValueChange = { appPreferences.setTtsPitch(it) },
                    valueRange = 0.5f..2.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tts_pitch_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Rate setting
                val ttsRate by appPreferences.ttsRate.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speech Rate",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.1fx", ttsRate),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = ttsRate,
                    onValueChange = { appPreferences.setTtsRate(it) },
                    valueRange = 0.5f..2.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tts_rate_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                    )
                )

                // Narrator Voice Selection UI
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Narrator Voice",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val currentNarrator by appPreferences.ttsNarrator.collectAsStateWithLifecycle()
                
                val narrators = listOf(
                    Triple("default", "Standard Storyteller", "🎙️ Warm & natural bedtime voice"),
                    Triple("cosmic_sage", "Cosmic Sage", "🧙‍♂️ Deep, wise & mystical"),
                    Triple("starlight_sprite", "Starlight Sprite", "🧚‍♀️ Light, bright & bubbly"),
                    Triple("gentle_dreamer", "Gentle Dreamer", "🌙 Soft, slow & soothing")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    narrators.forEach { (id, name, desc) ->
                        val isSelected = currentNarrator == id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { appPreferences.setTtsNarrator(id) }
                                .padding(12.dp)
                                .testTag("narrator_voice_$id"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (id) {
                                        "cosmic_sage" -> "🧙‍♂️"
                                        "starlight_sprite" -> "🧚‍♀️"
                                        "gentle_dreamer" -> "🌙"
                                        else -> "🎙️"
                                    },
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Bedtime Autoplay Next Option
            val autoplayNext by appPreferences.autoplayNext.collectAsStateWithLifecycle()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auto-play Next Story",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Automatically begins reading the next chronological story in a series once the current concludes.",
                            fontSize = 12.sp,
                            color = Slate300
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = autoplayNext,
                        onCheckedChange = { appPreferences.setAutoplayNext(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("setting_autoplay_next_toggle")
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = biography,
                    onValueChange = { biography = it },
                    label = { Text("Biography") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
            } else {
                Text(
                    text = if (username.isNotBlank()) username else "Astronaut",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (biography.isNotBlank()) {
                    Text(
                        text = biography,
                        fontSize = 16.sp,
                        color = Slate300,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text(
                        text = "No biography added yet. Tap the edit icon to add some details!",
                        fontSize = 14.sp,
                        color = Slate400,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Help & Fairy FAQ Launch Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToHelp() }
                    .testTag("profile_faq_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Help & Guides FAQ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Learn about Sleep Timers, Cosmic Satchel, and child safety features.",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Navigate to help",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(36.dp))
            
            val unlockedBadgesSet = appPreferences.getUnlockedBadges()
            
            // --- BEDTIME CHRONICLES GAME PANEL ---
            Text(
                text = "My Bedtime Chronicles Journey",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth().testTag("achievements_section_title"),
                textAlign = TextAlign.Start
            )
            Text(
                text = "Keep reading bedtime stories to grow your stats and unlock all cosmic badges!",
                fontSize = 12.sp,
                color = Slate300,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                textAlign = TextAlign.Start
            )
            
            // 3-Card Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Card
                val readingStreak = appPreferences.getReadingStreak()
                Card(
                    modifier = Modifier.weight(1f).testTag("stat_card_streak"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Reading Streak", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                        Text("$readingStreak Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                // Completed Stories Card
                val completedTales = appPreferences.getCompletedStoryCount()
                Card(
                    modifier = Modifier.weight(1f).testTag("stat_card_completed_tales"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📚", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Completed", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                        Text("$completedTales Tales", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                // Unlocked Badges Card
                Card(
                    modifier = Modifier.weight(1f).testTag("stat_card_badges"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Badges Unlocked", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                        Text("${unlockedBadgesSet.size} / 12", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Badges Trophies List
            Text(
                text = "Unlocked Bedtime Badges",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Loop through the 12 Bedtime Badges defined in BedtimeAssets
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("unlocked_badges_list")
            ) {
                com.example.data.BedtimeAssets.BADGE_DEFINITIONS.forEach { badge ->
                    val isUnlocked = unlockedBadgesSet.contains(badge.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("badge_item_${badge.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge Icon/Emoji with unlocked status glow effect
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isUnlocked) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        } else {
                                            Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Text(badge.emoji, fontSize = 24.sp)
                                } else {
                                    // Grayed out lock icon
                                    Text("🔒", fontSize = 18.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isUnlocked) Color.White else Color.Gray
                                )
                                Text(
                                    text = badge.description,
                                    fontSize = 11.sp,
                                    color = if (isUnlocked) Slate300 else Slate400.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "To unlock: " + badge.condition,
                                    fontSize = 9.sp,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Unlocked",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
