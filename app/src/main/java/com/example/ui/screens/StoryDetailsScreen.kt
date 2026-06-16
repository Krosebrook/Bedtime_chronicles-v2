package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.data.StoryItem
import com.example.data.sampleStories
import com.example.data.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailsScreen(storyId: String, onBack: () -> Unit, onStartReading: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences.getInstance(context) }
    val story = sampleStories.find { it.id == storyId } ?: sampleStories.first()
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Story Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    IconButton(onClick = { }) { Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onStartReading(story.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Start Journey", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            HeroBanner(story)
            Spacer(modifier = Modifier.height(24.dp))
            SummarySection()
            Spacer(modifier = Modifier.height(24.dp))
            BedtimeProgressSection(story, prefs)
            Spacer(modifier = Modifier.height(24.dp))
            PersonalizeSection()
            Spacer(modifier = Modifier.height(24.dp))
            TechnicalDetailsRow()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroBanner(story: StoryItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = story.imageUrl,
            contentDescription = story.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 100f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = PrimaryBlue, shape = CircleShape) {
                    Text("PREMIUM STORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Surface(color = Color.DarkGray.copy(alpha=0.8f), shape = CircleShape) {
                    Text("AGES 5-8", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(story.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(story.readTime, color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(story.category, color = Color.LightGray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SummarySection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Join a brave young explorer on an intergalactic journey to find the legendary Crystal Moon. Hidden within the Nebula of Whispers, this celestial body is said to grant peaceful dreams to the entire universe. A heartwarming tale about courage, kindness, and the magic found in the stars.",
            color = Slate300,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun PersonalizeSection() {
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
            Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Personalize the Magic", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Hero's Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        var name by remember { mutableStateOf("") }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Who is going on this adventure?", color = Slate500) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Adventure Setting", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingCard(Icons.Default.RocketLaunch, "Deep Space", true, Modifier.weight(1f))
            SettingCard(Icons.Default.Forest, "Enchanted Woods", false, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingCard(Icons.Default.WaterDrop, "Ocean Depths", false, Modifier.weight(1f))
            SettingCard(Icons.Default.Castle, "Sky Kingdom", false, Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Narration Tone", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToneChip("Gentle & Calm", true)
            ToneChip("Energetic", false)
            ToneChip("Funny", false)
        }
    }
}

@Composable
fun SettingCard(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Slate300)
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else Slate300)
    }
}

@Composable
fun ToneChip(name: String, isSelected: Boolean) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) Color.White else Slate300
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    Surface(
        color = bgColor,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(name, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

@Composable
fun TechnicalDetailsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailCard(Icons.Default.Headset, "Audio", "Enabled")
        DetailCard(Icons.Default.Palette, "Illustrations", "AI Dynamic")
        DetailCard(Icons.Default.Language, "Language", "English")
    }
}

@Composable
fun DetailCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GlassWhite)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BedtimeProgressSection(story: StoryItem, prefs: AppPreferences) {
    var completedCount by remember { mutableStateOf(prefs.getCompletedStoryCount()) }
    var currentStoryProgress by remember { mutableStateOf(prefs.getStoryProgress(story.id)) }
    val totalStoriesCount = sampleStories.size
    val dailyGoal = 3 // Standard daily target

    // Routine progress
    val routineProgress = (completedCount.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
    // Collection progress
    val collectionProgress = (completedCount.toFloat() / totalStoriesCount.toFloat()).coerceIn(0f, 1f)
    // Estimate sentences/pages count in a story: typically around 12 parts
    val estimatedPages = 12
    val storyProgressFloat = if (currentStoryProgress > 0) {
        (currentStoryProgress.toFloat() / estimatedPages.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("story_details_progress_section")
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "BEDTIME PROGRESS & GOALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Your Bedtime Routine & Library Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Two-column layout for Routine & Collection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card: Nightly Bedtime Goal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nightly Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate300)
                    Icon(Icons.Default.Nightlight, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$completedCount / $dailyGoal Stories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { routineProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .testTag("routine_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (completedCount >= dailyGoal) "Sleep routine complete! 😴" else "Let's read to wind down",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }

            // Right Card: Story Collection Completed
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Library Mastery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate300)
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(collectionProgress * 100).toInt()}% Done",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { collectionProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .testTag("collection_progress_bar"),
                    color = Color(0xFFFF9800),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discovered $completedCount / $totalStoriesCount Tales",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom element: Specific Story progress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (currentStoryProgress > 0) MaterialTheme.colorScheme.primary else Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "This Story's Progress",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentStoryProgress > 0) Color.White else Slate300
                        )
                    }
                    if (currentStoryProgress > 0) {
                        Text(
                            text = "Reset Progress",
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    prefs.setStoryProgress(story.id, 0)
                                    currentStoryProgress = 0
                                }
                                .testTag("reset_story_progress_button")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (currentStoryProgress > 0) {
                    val percentage = (storyProgressFloat * 100).toInt().coerceIn(1, 100)
                    Text(
                        text = "Last read at Page $currentStoryProgress ($percentage% processed)",
                        fontSize = 13.sp,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { storyProgressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .testTag("current_story_progress_bar"),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                } else {
                    Text(
                        text = "Ready to start fresh bedtime adventure!",
                        fontSize = 13.sp,
                        color = Slate500
                    )
                }
            }
        }
    }
}

