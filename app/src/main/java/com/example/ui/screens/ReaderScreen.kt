package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AppPreferences
import com.example.ui.theme.*
import com.example.util.AmbientSoundHelper
import com.example.util.TextToSpeechHelper
import com.example.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex

enum class ReaderStep {
    READING,
    VOCAB,
    RATING,
    CELEBRATION
}

data class VocabWord(
    val word: String,
    val pronunciation: String,
    val definition: String
)

val BedtimeVocabWords = listOf(
    VocabWord("CONSTELLATION", "/ˌkänstəˈlāSH(ə)n/", "A group of stars that make a friendly picture in the sky!"),
    VocabWord("BIOLUMINESCENT", "/ˌbīōˌlo͞oməˈnesənt/", "Living things that glow with their own magical light!"),
    VocabWord("STARDUST", "/ˈstärˌdəst/", "Tiny glowing magical dust left behind by shooting stars!"),
    VocabWord("NEBULA", "/ˈnebyələ/", "A beautiful cloud of dust and gas in space where newborn stars are made!"),
    VocabWord("LUMINESCENCE", "/ˌlo͞oməˈnesəns/", "A cool, soft glow that shines in the night like a friendly guide!"),
    VocabWord("AURORA", "/ôˈrôrə/", "Dancing curtains of colorful light in the cold polar night sky!"),
    VocabWord("TRANQUILITY", "/traŋˈkwilədē/", "A deep state of peace, quiet, and happy safety where everything is cozy and still.")
)

data class Trophy(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlocked: Boolean
)

val ReadyTrophies = listOf(
    Trophy("star_finder", "Star Finder 🌟", "Completed your very first bedtime cosmic adventure!", "🌟", true),
    Trophy("nebula_voyager", "Nebula Voyager 🪐", "Enjoyed a story with Space Echoes ambient live soundscapes.", "🪐", true),
    Trophy("sleep_champion", "Sleep Champion ⏳", "Timed out a peaceful session using the custom linear-faded Sleep Timer.", "⏳", true),
    Trophy("vocab_linguist", "Luminous Linguist 📚", "Learned a new cosmic word and checked its starry pronunciation guide.", "📚", true),
    Trophy("soundscape_alchemist", "Sound Alchemist 🍃", "Mixed personalized forest breeze or rainy ambient volumes.", "🍃", false)
)

data class ConfettiParticle(
    val xOffset: Float,
    val ySpeed: Float,
    val color: Color,
    val size: Float,
    val angleSpeed: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(storyId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ReaderViewModel = viewModel()
    val story by viewModel.story.collectAsStateWithLifecycle()
    
    val prefs = remember { AppPreferences.getInstance(context) }
    val isMidnightMode by prefs.isMidnightMode.collectAsStateWithLifecycle()
    
    var activeStoryId by remember { mutableStateOf(storyId ?: "") }
    val safeStoryId = activeStoryId

    val allStories by viewModel.allStories.collectAsStateWithLifecycle()
    val autoplayNext by prefs.autoplayNext.collectAsStateWithLifecycle()
    
    var activeAutoplayBannerText by remember { mutableStateOf<String?>(null) }
    var autoplayPendingStart by remember { mutableStateOf(false) }

    var isReading by remember { mutableStateOf(false) }
    var ttsInitialized by remember { mutableStateOf(false) }
    var activeSentenceIndex by remember { mutableStateOf(-1) }
    var isBookMode by remember { mutableStateOf(true) }
    
    var currentStep by remember { mutableStateOf(ReaderStep.READING) }
    var ratingSelected by remember { mutableStateOf(-1) }
    var savedToLibrary by remember { mutableStateOf(false) }
    var showTrophiesSheet by remember { mutableStateOf(false) }

    val randomVocabWord = remember(activeStoryId) {
        val wordIndex = if (activeStoryId.isNotEmpty()) {
            val h = activeStoryId.hashCode()
            if (h < 0) {
                if (h == Int.MIN_VALUE) 0 else -h
            } else {
                h
            }
        } else {
            0
        }
        BedtimeVocabWords[wordIndex % BedtimeVocabWords.size]
    }
    
    // Ambient Soundscape state
    var ambientVolume by remember { mutableStateOf(prefs.getAmbientVolume()) }
    var ambientSoundscape by remember { mutableStateOf(prefs.getAmbientSoundscape()) }
    
    // Sleep Timer state
    var sleepTimerRemainingSeconds by remember { mutableStateOf(0) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showAmbientDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    val currentNarrator by prefs.ttsNarrator.collectAsStateWithLifecycle()
    
    // Progress Resume Dialog state
    var showResumeToast by remember { mutableStateOf(false) }
    var savedProgressIndex by remember { mutableStateOf(0) }

    // Split sentences helper
    fun splitIntoSentences(text: String): List<String> {
        val regex = "(?<=[.!?])\\s+".toRegex()
        return text.split(regex).map { it.trim() }.filter { it.isNotEmpty() }
    }

    val sentences = remember(story) {
        story?.let { splitIntoSentences(it.content) } ?: emptyList()
    }

    // Initialize Synthesizer and TTS Helper
    val ambientHelper = remember { AmbientSoundHelper() }
    val ttsHelper = remember(sentences, safeStoryId) {
        TextToSpeechHelper(context) { success ->
            ttsInitialized = success
        }.apply {
            onSentenceStartedListener = { utteranceId ->
                val index = utteranceId.toIntOrNull()
                if (index != null && index >= 0 && index < sentences.size) {
                    activeSentenceIndex = index
                }
            }
            onSentenceFinishedListener = { utteranceId ->
                val index = utteranceId.toIntOrNull()
                if (index != null && isReading) {
                    val nextIndex = index + 1
                    if (nextIndex < sentences.size) {
                        activeSentenceIndex = nextIndex
                        speakSentence(sentences[nextIndex], nextIndex.toString())
                        prefs.setStoryProgress(safeStoryId, nextIndex)
                    } else {
                        val currentStory = story
                        if (autoplayNext && currentStory != null) {
                            val seriesCategory = currentStory.category
                            val seriesStories = allStories
                                .filter { it.category.equals(seriesCategory, ignoreCase = true) }
                                .sortedBy { it.createdAt }
                            
                            val currentIdx = seriesStories.indexOfFirst { it.id == currentStory.id }
                            if (currentIdx != -1 && currentIdx + 1 < seriesStories.size) {
                                val nextStory = seriesStories[currentIdx + 1]
                                activeAutoplayBannerText = "Up Next in Series: ${nextStory.title} 🌌"
                                autoplayPendingStart = true
                                isReading = false
                                activeSentenceIndex = 0
                                prefs.setStoryProgress(safeStoryId, 0)
                                activeStoryId = nextStory.id
                            } else {
                                isReading = false
                                activeSentenceIndex = -1
                                prefs.setStoryProgress(safeStoryId, 0)
                                currentStep = ReaderStep.VOCAB
                            }
                        } else {
                            isReading = false
                            activeSentenceIndex = -1
                            prefs.setStoryProgress(safeStoryId, 0)
                            currentStep = ReaderStep.VOCAB
                        }
                    }
                }
            }
        }
    }

    // Clean up readers on exit
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.stop()
            ttsHelper.shutdown()
            ambientHelper.stop()
        }
    }

    // Dismiss active autoplay banner after delay
    LaunchedEffect(activeAutoplayBannerText) {
        if (activeAutoplayBannerText != null) {
            delay(5000L)
            activeAutoplayBannerText = null
        }
    }

    // Load story and fetch initial progress
    LaunchedEffect(activeStoryId) {
        if (activeStoryId.isNotEmpty()) {
            viewModel.loadStory(activeStoryId)
            val saved = prefs.getStoryProgress(activeStoryId)
            if (saved > 0) {
                savedProgressIndex = saved
                showResumeToast = true
            } else {
                savedProgressIndex = 0
                showResumeToast = false
            }
        }
    }

    // Trigger autoplay of the next story after it loads
    LaunchedEffect(story) {
        val currentStory = story
        if (currentStory != null && autoplayPendingStart) {
            autoplayPendingStart = false
            delay(1500L) // Bedtime spacer pause
            while (!ttsInitialized) {
                delay(100L)
            }
            val newSentences = splitIntoSentences(currentStory.content)
            if (newSentences.isNotEmpty()) {
                activeSentenceIndex = 0
                isReading = true
                ttsHelper.speakSentence(newSentences[0], "0")
                prefs.setStoryProgress(currentStory.id, 0)
            }
        }
    }

    // Control Ambient Audio synthesis cycles
    LaunchedEffect(ambientSoundscape) {
        ambientHelper.start(ambientSoundscape)
        prefs.setAmbientSoundscape(ambientSoundscape)
    }

    LaunchedEffect(ambientVolume) {
        ambientHelper.setVolume(ambientVolume)
        prefs.setAmbientVolume(ambientVolume)
    }

    // Sleep Timer countdown processor
    LaunchedEffect(sleepTimerRemainingSeconds, isReading, ambientSoundscape) {
        if (sleepTimerRemainingSeconds > 0) {
            while (sleepTimerRemainingSeconds > 0) {
                delay(1000L)
                sleepTimerRemainingSeconds--
                
                // Beautiful Bedtime Touch: Fade out ambient soundscape during final 10 seconds
                if (sleepTimerRemainingSeconds in 1..10 && ambientSoundscape != "Off") {
                    val progress = sleepTimerRemainingSeconds / 10.0f
                    ambientHelper.setVolume(ambientVolume * progress)
                }
                
                if (sleepTimerRemainingSeconds == 0) {
                    // Safe shutoff of all features
                    ttsHelper.stop()
                    ambientHelper.stop()
                    isReading = false
                    ambientSoundscape = "Off"
                    activeSentenceIndex = -1
                    break
                }
            }
        }
    }

    if (story == null) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Loading Story...", fontSize = 16.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val safeStory = story!!

    // Formatting seconds left to readable time string MM:SS
    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (currentStep == ReaderStep.READING) {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(safeStory.category.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.5.sp)
                            Text(safeStory.title, fontSize = 14.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            ttsHelper.stop()
                            ambientHelper.stop()
                            onBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { prefs.setMidnightMode(!isMidnightMode) },
                            modifier = Modifier.testTag("reader_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isMidnightMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                tint = if (isMidnightMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = if (isMidnightMode) "Switch to Light Theme" else "Switch to Night Mode (Warm)"
                            )
                        }
                        IconButton(
                            onClick = { showVoiceDialog = true },
                            modifier = Modifier.testTag("reader_voice_toggle")
                        ) {
                            Icon(
                                Icons.Default.Hearing,
                                tint = if (currentNarrator != "default") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = "Character Voices"
                            )
                        }
                        IconButton(onClick = { showAmbientDialog = true }) {
                            Icon(
                                Icons.Default.Star,
                                tint = if (ambientSoundscape != "Off") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = "Soundscapes"
                            )
                        }
                        IconButton(onClick = { showTimerDialog = true }) {
                            Icon(
                                Icons.Default.Refresh,
                                tint = if (sleepTimerRemainingSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = "Sleep Timer"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedVisibility(
                visible = activeAutoplayBannerText != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .zIndex(99f)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("autoplay_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Bedtime Series Autoplay",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                activeAutoplayBannerText ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            if (currentStep == ReaderStep.READING) {
                val progress = if (sentences.isNotEmpty()) {
                    ((activeSentenceIndex + 1).toFloat() / sentences.size.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    LinearProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .testTag("reading_progress_bar")
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(21f / 9f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = safeStory.coverImageUrl,
                        contentDescription = "Scene",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Saved Progress Resume Card
                AnimatedVisibility(
                    visible = showResumeToast && savedProgressIndex > 0 && savedProgressIndex < sentences.size,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resume Reading?", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                "You left off around ${((savedProgressIndex * 100) / sentences.size).coerceIn(1, 100)}% of the story last time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate300,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = {
                                    showResumeToast = false
                                    prefs.setStoryProgress(safeStoryId, 0)
                                }) {
                                    Text("Start Over", color = Slate400)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        showResumeToast = false
                                        activeSentenceIndex = savedProgressIndex
                                        isReading = true
                                        ttsHelper.speakSentence(sentences[savedProgressIndex], savedProgressIndex.toString())
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Resume", color = Color.Black)
                                }
                            }
                        }
                    }
                }

                // Animated Header Entry
                MagicalEntryText(delayMillis = 150) { animModifier ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().then(animModifier)
                    ) {
                        Text(
                            text = safeStory.title,
                            style = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.height(3.dp).width(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp)))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Layout Selector Tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeModeColor = MaterialTheme.colorScheme.primary
                    val inactiveModeColor = Color.White.copy(alpha = 0.6f)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isBookMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { isBookMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "Scenic Flow",
                                tint = if (!isBookMode) activeModeColor else inactiveModeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Scenic Flow",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isBookMode) activeModeColor else inactiveModeColor
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBookMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { isBookMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Bedtime Book",
                                tint = if (isBookMode) activeModeColor else inactiveModeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Bedtime Book",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBookMode) activeModeColor else inactiveModeColor
                            )
                        }
                    }
                }

                // Removed inline progress bar since it's moved to the top edge now

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive sentence reading body block
                if (isBookMode) {
                    val sentencesPerPage = 3
                    val totalPages = (sentences.size + sentencesPerPage - 1) / sentencesPerPage
                    val currentPageIndex = if (activeSentenceIndex >= 0) activeSentenceIndex / sentencesPerPage else 0
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Magical subtle page transition (Fade + Scale + soft horizontal slide)
                        AnimatedContent(
                            targetState = currentPageIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(
                                        animationSpec = tween(500, easing = EaseOutQuad),
                                        initialOffsetX = { width -> (width * 0.12f).toInt() }
                                    ) + fadeIn(
                                        animationSpec = tween(500, easing = EaseOutQuad)
                                    ) + scaleIn(
                                        initialScale = 0.94f,
                                        animationSpec = tween(500, easing = EaseOutQuad)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(400, easing = EaseInQuad),
                                            targetOffsetX = { width -> (-width * 0.12f).toInt() }
                                        ) + fadeOut(
                                            animationSpec = tween(400, easing = EaseInQuad)
                                        ) + scaleOut(
                                            targetScale = 1.06f,
                                            animationSpec = tween(400, easing = EaseInQuad)
                                        )
                                    )
                                } else {
                                    (slideInHorizontally(
                                        animationSpec = tween(500, easing = EaseOutQuad),
                                        initialOffsetX = { width -> (-width * 0.12f).toInt() }
                                    ) + fadeIn(
                                        animationSpec = tween(500, easing = EaseOutQuad)
                                    ) + scaleIn(
                                        initialScale = 1.06f,
                                        animationSpec = tween(500, easing = EaseOutQuad)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(400, easing = EaseInQuad),
                                            targetOffsetX = { width -> (width * 0.12f).toInt() }
                                        ) + fadeOut(
                                            animationSpec = tween(400, easing = EaseInQuad)
                                        ) + scaleOut(
                                            targetScale = 0.94f,
                                            animationSpec = tween(400, easing = EaseInQuad)
                                        )
                                    )
                                }
                            },
                            label = "PageTurningTransition"
                        ) { pageIdx ->
                            val startIdx = pageIdx * sentencesPerPage
                            val endIdx = minOf(startIdx + sentencesPerPage, sentences.size)
                            val pageSentences = if (startIdx in sentences.indices) {
                                sentences.subList(startIdx, endIdx)
                            } else {
                                emptyList()
                            }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        RoundedCornerShape(24.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    // Page indicator header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PAGE ${pageIdx + 1} OF $totalPages",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    pageSentences.forEachIndexed { pageOffset, sentence ->
                                        val actualIndex = startIdx + pageOffset
                                        val isActive = actualIndex == activeSentenceIndex
                                        MagicalPageSentence(pageIndex = pageIdx, pageOffset = pageOffset) { animModifier ->
                                            Text(
                                                text = sentence,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                                    fontSize = 21.sp,
                                                    lineHeight = 34.sp
                                                ),
                                                color = if (isActive) MaterialTheme.colorScheme.primary else Slate300.copy(alpha = if (activeSentenceIndex == -1) 1.0f else 0.45f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .then(animModifier)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        activeSentenceIndex = actualIndex
                                                        isReading = true
                                                        ttsHelper.speakSentence(sentence, actualIndex.toString())
                                                        prefs.setStoryProgress(safeStoryId, actualIndex)
                                                    }
                                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Book Page turning controller navigation buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val previousPageStartIndex = (currentPageIndex - 1) * sentencesPerPage
                                    if (previousPageStartIndex >= 0) {
                                        activeSentenceIndex = previousPageStartIndex
                                        prefs.setStoryProgress(safeStoryId, previousPageStartIndex)
                                        if (isReading) {
                                            ttsHelper.speakSentence(sentences[previousPageStartIndex], previousPageStartIndex.toString())
                                        }
                                    }
                                },
                                enabled = currentPageIndex > 0,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (currentPageIndex > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Previous Page",
                                    tint = if (currentPageIndex > 0) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(32.dp))
                            
                            Text(
                                text = "Pg ${currentPageIndex + 1} / $totalPages",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate300,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.width(32.dp))
                            
                            IconButton(
                                onClick = {
                                    if (currentPageIndex < totalPages - 1) {
                                        val nextPageStartIndex = (currentPageIndex + 1) * sentencesPerPage
                                        if (nextPageStartIndex < sentences.size) {
                                            activeSentenceIndex = nextPageStartIndex
                                            prefs.setStoryProgress(safeStoryId, nextPageStartIndex)
                                            if (isReading) {
                                                ttsHelper.speakSentence(sentences[nextPageStartIndex], nextPageStartIndex.toString())
                                            }
                                        }
                                    } else {
                                        ttsHelper.stop()
                                        isReading = false
                                        currentStep = ReaderStep.VOCAB
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    if (currentPageIndex < totalPages - 1) Icons.Default.KeyboardArrowRight else Icons.Default.Check,
                                    contentDescription = if (currentPageIndex < totalPages - 1) "Next Page" else "Conclude Story",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    // Scenic Flow Column Mode
                    Column(modifier = Modifier.fillMaxWidth()) {
                        sentences.forEachIndexed { index, sentence ->
                            val isActive = index == activeSentenceIndex
                            MagicalFlowSentence(index = index) { animModifier ->
                                Text(
                                    text = sentence,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 21.sp,
                                        lineHeight = 34.sp
                                    ),
                                    color = if (isActive) MaterialTheme.colorScheme.primary else Slate300.copy(alpha = if (activeSentenceIndex == -1) 1.0f else 0.45f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(animModifier)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            activeSentenceIndex = index
                                            isReading = true
                                            ttsHelper.speakSentence(sentence, index.toString())
                                            prefs.setStoryProgress(safeStoryId, index)
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                ttsHelper.stop()
                                isReading = false
                                currentStep = ReaderStep.VOCAB
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Night's Story 🌟", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

            // Beautiful Bottom Bedtime Media Controls Overlay Sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        )
                    )
                    .padding(bottom = 24.dp, top = 32.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Current playing info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isReading && activeSentenceIndex >= 0) "Reading Bedtime Story" else "Bedtime Reader Ready",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (activeSentenceIndex >= 0 && activeSentenceIndex < sentences.size) {
                                        "Phrase ${activeSentenceIndex + 1} of ${sentences.size}"
                                    } else {
                                        "Click text to read along"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }

                            // Sleep timer remaining badge
                            if (sleepTimerRemainingSeconds > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = formatTime(sleepTimerRemainingSeconds),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev phrase
                            IconButton(
                                onClick = {
                                    val prevIndex = activeSentenceIndex - 1
                                    if (prevIndex >= 0) {
                                        activeSentenceIndex = prevIndex
                                        isReading = true
                                        ttsHelper.speakSentence(sentences[prevIndex], prevIndex.toString())
                                        prefs.setStoryProgress(safeStoryId, prevIndex)
                                    }
                                },
                                enabled = activeSentenceIndex > 0
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Previous phrase", modifier = Modifier.size(28.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Play Pause Button
                            Button(
                                onClick = {
                                    if (isReading) {
                                        ttsHelper.stop()
                                        isReading = false
                                    } else {
                                        val startIdx = if (activeSentenceIndex >= 0) activeSentenceIndex else 0
                                        activeSentenceIndex = startIdx
                                        isReading = true
                                        ttsHelper.speakSentence(sentences[startIdx], startIdx.toString())
                                        prefs.setStoryProgress(safeStoryId, startIdx)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(56.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    if (isReading) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    tint = Color.Black,
                                    contentDescription = "Play Story",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Next phrase
                            IconButton(
                                onClick = {
                                    val nextIndex = activeSentenceIndex + 1
                                    if (nextIndex < sentences.size) {
                                        activeSentenceIndex = nextIndex
                                        isReading = true
                                        ttsHelper.speakSentence(sentences[nextIndex], nextIndex.toString())
                                        prefs.setStoryProgress(safeStoryId, nextIndex)
                                    } else {
                                        ttsHelper.stop()
                                        isReading = false
                                        currentStep = ReaderStep.VOCAB
                                    }
                                },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    if (activeSentenceIndex < sentences.size - 1) Icons.Default.KeyboardArrowRight else Icons.Default.Check,
                                    contentDescription = if (activeSentenceIndex < sentences.size - 1) "Next phrase" else "Conclude Story",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Ambient Sound Mixer Bar
                        if (ambientSoundscape != "Off") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Ambient Sound Mix",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.dashPathEffectNeeded() ?: Modifier.width(8.dp))
                                Text(
                                    text = "$ambientSoundscape Mix:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300,
                                    modifier = Modifier.width(110.dp)
                                )
                                Slider(
                                    value = ambientVolume,
                                    onValueChange = { ambientVolume = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Speech Speed (Narration Rate) Slider
                        val ttsRate by prefs.ttsRate.collectAsStateWithLifecycle()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Narration Speed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Speech Speed: ${String.format(java.util.Locale.US, "%.1fx", ttsRate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300,
                                modifier = Modifier.width(110.dp)
                            )
                            Slider(
                                value = ttsRate,
                                onValueChange = { prefs.setTtsRate(it) },
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.weight(1f).testTag("speech_rate_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                )
                            )
                        }

                        // Bedtime Autoplay Series Switch
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .clickable { prefs.setAutoplayNext(!autoplayNext) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-play Next Series Story",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Switch(
                                checked = autoplayNext,
                                onCheckedChange = { prefs.setAutoplayNext(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .scale(0.8f)
                                    .testTag("reader_autoplay_next_toggle")
                            )
                        }
                    }
                }
            }
        } else {
            when (currentStep) {
                    ReaderStep.VOCAB -> {
                        VocabScreen(word = randomVocabWord, ttsHelper = ttsHelper) {
                            prefs.incrementVocabularyWordsLearnedCount()
                            if (prefs.getVocabularyWordsLearnedCount() >= 5) {
                                prefs.unlockBadge("vocabulary-star")
                            }
                            currentStep = ReaderStep.RATING
                        }
                    }
                    ReaderStep.RATING -> {
                        RatingScreen { rating, saveToLib ->
                            ratingSelected = rating
                            savedToLibrary = saveToLib
                            if (saveToLib) {
                                prefs.setSavedStory(safeStoryId, true)
                            }
                            
                            // Gamified Statistics Increment & Badges Unlocking Rules
                            val completedStoriesCount = prefs.incrementCompletedStoryCount()
                            
                            // 1. First Story Badge Check
                            if (completedStoriesCount >= 1) {
                                prefs.unlockBadge("first-adventure")
                            }
                            
                            // 2. Bookworm and Legend Badges Checks
                            if (completedStoriesCount >= 10) {
                                prefs.unlockBadge("bookworm")
                            }
                            if (completedStoriesCount >= 25) {
                                prefs.unlockBadge("legend")
                            }
                            
                            // 3. Increment Reading Streak & Badges Check
                            prefs.incrementReadingStreak()
                            val streak = prefs.getReadingStreak()
                            if (streak >= 3) {
                                prefs.unlockBadge("story-streak-3")
                            }
                            if (streak >= 7) {
                                prefs.unlockBadge("story-streak-7")
                            }
                            
                            // 4. Mode-specific (Classic, Sleep, Mad Libs) Count and Badge Unlock
                            val catLower = safeStory.category.lowercase().trim()
                            val modeCount = prefs.incrementModeStoryCount(catLower)
                            if (catLower.contains("classic") && modeCount >= 5) {
                                prefs.unlockBadge("classic-champion")
                            }
                            if (catLower.contains("sleep") && modeCount >= 3) {
                                prefs.unlockBadge("dream-weaver")
                            }
                            if (catLower.contains("mad") && modeCount >= 3) {
                                prefs.unlockBadge("mad-libs-master")
                            }
                            
                            // 5. Early Bird / Night Owl Badge Check by Clock Hours
                            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            if (hour >= 20 || hour < 5) {
                                prefs.unlockBadge("night-owl")
                            } else if (hour in 5..10) {
                                prefs.unlockBadge("early-bird")
                            }
                            
                            // 6. Hero Collector Checklist Checked
                            if (prefs.getUsedHeroesCount() >= 9) {
                                prefs.unlockBadge("all-heroes")
                            }
                            
                            currentStep = ReaderStep.CELEBRATION
                        }
                    }
                    ReaderStep.CELEBRATION -> {
                        CelebrationScreen(
                            onShowTrophies = { showTrophiesSheet = true },
                            onBackHome = {
                                ttsHelper.stop()
                                ambientHelper.stop()
                                onBack()
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    // Modern Sleep Timer Dialogue Menu
    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Snooze Timer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Sleep Timer Options", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            },
            text = {
                Column {
                    Text(
                        "Configure a timer to close down the reading and ambient sounds so the device rests after you drift off.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    listOf(
                        "Off" to 0,
                        "1 Minute (Demo)" to 60,
                        "5 Minutes" to 300,
                        "15 Minutes" to 900,
                        "30 Minutes" to 1800,
                        "60 Minutes" to 3600
                    ).forEach { (label, duration) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    sleepTimerRemainingSeconds = duration
                                    showTimerDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (sleepTimerRemainingSeconds == duration) {
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = Color.White)
                                if (sleepTimerRemainingSeconds == duration) {
                                    Text("Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Done", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Ambient Soundscape Selector Dialogue Menu
    if (showAmbientDialog) {
        AlertDialog(
            onDismissRequest = { showAmbientDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Ambience Choice",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Select Ambient Environment", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            },
            text = {
                Column {
                    Text(
                        "Layer peaceful, organic ambient noises directly with the bedtime narrator voice for total physical immersion.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    listOf(
                        "Off",
                        "Nebula Whisper",
                        "Starfield Hum",
                        "Space Echoes",
                        "Gentle Rain",
                        "Cozy Waves",
                        "Forest Breeze"
                    ).forEach { sName ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    ambientSoundscape = sName
                                    showAmbientDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (ambientSoundscape == sName) {
                                    MaterialTheme.colorScheme.tertiaryContainer
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sName, color = Color.White)
                                if (ambientSoundscape == sName) {
                                    Text("Playing", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAmbientDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.tertiary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Character Voice Selector Dialog
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
                    Text("Choose Story Teller Voice", style = MaterialTheme.typography.titleMedium, color = Color.White)
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
                                    // Live updates the speech options
                                    ttsHelper.stop()
                                    // if isReading was active, restart current index with the new voice
                                    if (isReading && activeSentenceIndex >= 0 && activeSentenceIndex < sentences.size) {
                                        ttsHelper.speakSentence(sentences[activeSentenceIndex], activeSentenceIndex.toString())
                                    }
                                }
                                .testTag("reader_narrator_voice_$id"),
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
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(desc, color = Slate300, style = MaterialTheme.typography.bodySmall)
                                }
                                if (isSelected) {
                                    Text("Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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

    // Trophies dialog
    if (showTrophiesSheet) {
        AlertDialog(
            onDismissRequest = { showTrophiesSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Trophies",
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("My Bedtime Trophies", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Collect gorgeous cosmic badges by maintaining your cozy reading routines!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val unlockedBadgesSet = prefs.getUnlockedBadges()
                    com.example.data.BedtimeAssets.BADGE_DEFINITIONS.forEach { badge ->
                        val isUnlocked = unlockedBadgesSet.contains(badge.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = badge.emoji,
                                    fontSize = 28.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isUnlocked) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isUnlocked) Slate300 else Color.Gray
                                    )
                                }
                                if (isUnlocked) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Unlocked",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrophiesSheet = false }) {
                    Text("Awesome!", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Utility extension helper to support standard layout styling
private fun Modifier.dashPathEffectNeeded(): Modifier? = null

@Composable
fun VocabScreen(
    word: VocabWord,
    ttsHelper: TextToSpeechHelper,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .border(
                    2.dp,
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color(0xFFC084FC), Color(0xFF6366F1).copy(alpha = 0.3f))
                    ),
                    RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow Star symbol on top
                Box(contentAlignment = Alignment.Center) {
                    val pulse = rememberInfiniteTransition(label = "Pulse")
                    val scale by pulse.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "pulse"
                    )
                    
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Cosmic Flashcard Aura",
                        tint = Color(0xFFC084FC).copy(alpha = 0.15f),
                        modifier = Modifier.size(110.dp * scale)
                    )
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Cosmic Flashcard Icon",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "YOU LEARNED A NEW WORD! 🌟",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFC084FC),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = word.word,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            ttsHelper.speakSentence(word.word, "single_word_tts_key")
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Listen button",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = word.pronunciation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate300,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = word.definition,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CONTINUE JOURNEY", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun RatingScreen(
    onContinue: (rating: Int, saveToLibrary: Boolean) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    var saveToLibraryChecked by remember { mutableStateOf(false) }

    val emojis = listOf(
        "😴" to "Sleepy",
        "😐" to "Okay",
        "😊" to "Happy",
        "🌟" to "Excellent",
        "🚀" to "Superb"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HOW WAS YOUR ADVENTURE?",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tap to rate tonight's story",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Emojis Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emojis.forEachIndexed { idx, (emoji, name) ->
                val isSelected = selectedIndex == idx
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.3f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ), label = "emoji_spring"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedIndex = idx }
                        .padding(12.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 32.sp,
                        modifier = Modifier.scale(scale)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Slate400,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save to My Library Card row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Save to My Library",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Keep this bedtime adventure saved for quick replays",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
                Switch(
                    checked = saveToLibraryChecked,
                    onCheckedChange = { saveToLibraryChecked = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Button(
            onClick = {
                onContinue(selectedIndex, saveToLibraryChecked)
            },
            enabled = selectedIndex != -1,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Finish Night", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun CelebrationScreen(
    onShowTrophies: () -> Unit,
    onBackHome: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences.getInstance(context) }
    val currentStreak = prefs.getReadingStreak()

    // Confetti System
    val particles = remember {
        val seeded = kotlin.random.Random(101)
        List(40) {
            val color = listOf(
                Color(0xFFFDE047), // Gold
                Color(0xFFC084FC), // Lilac
                Color(0xFF6366F1), // Space Blue
                Color(0xFF38BDF8), // Cyan
                Color(0xFFF43F5E)  // Sweet Pink
            ).random(seeded)
            ConfettiParticle(
                xOffset = seeded.nextFloat(),
                ySpeed = seeded.nextFloat() * 180f + 120f,
                color = color,
                size = seeded.nextFloat() * 10f + 10f,
                angleSpeed = seeded.nextFloat() * 120f - 60f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ConfettiLoop")
    val progressTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Star Finder rays rotating
    val beamTransition = rememberInfiniteTransition(label = "GoldBeams")
    val rotationAngle by beamTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Falling Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            particles.forEach { p ->
                val y = (p.ySpeed * progressTime) % height
                val x = p.xOffset * width
                
                drawRect(
                    color = p.color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 1.5f),
                    alpha = ((height - y) / height).coerceIn(0f, 1f)
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AMAZING JOB, HERO!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Star Finder Gold Badge Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            ) {
                // Spinning cosmic rays
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Draw 8 transparent starlight ray blades
                    val bladeCount = 8
                    val sweepAngle = 18f
                    rotate(rotationAngle, center) {
                        for (i in 0 until bladeCount) {
                            val startAngle = i * (360f / bladeCount) - (sweepAngle / 2)
                            drawArc(
                                color = Color(0xFFFDE047).copy(alpha = 0.08f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = size,
                                topLeft = Offset(0f, 0f)
                            )
                        }
                    }
                }

                // Inner Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(Color(0xFFFEF08A), Color(0xFFEAB308), Color(0xFF854D0E))
                            )
                        )
                        .border(
                            3.dp,
                            Color.White.copy(alpha = 0.6f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Gold Medal Star",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Star Finder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reading Streak Card (Double Height styling)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .border(
                        1.dp,
                        Color(0xFFFE08A4).copy(alpha = 0.25f),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Flaming campfire icon style
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFFFE08A4).copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$currentStreak ${if (currentStreak == 1) "DAY" else "DAYS"} STREAK!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "You're building an incredible bedtime habit! Cozy sleep is ahead, champ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Button(
                onClick = onShowTrophies,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "My Trophies Shelf Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SEE MY TROPHIES",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = "Home Icon", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GO HOME", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

// Subtle entry animation helpers to create a more magical, immersive bedtime reading experience
@Composable
fun MagicalEntryText(
    delayMillis: Int,
    content: @Composable (Modifier) -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(20f) }
    
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 800, easing = EaseOutQuad))
    }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        offsetY.animateTo(0f, animationSpec = tween(durationMillis = 800, easing = EaseOutQuad))
    }
    
    content(
        Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value
        }
    )
}

@Composable
fun MagicalPageSentence(
    pageIndex: Int,
    pageOffset: Int,
    content: @Composable (Modifier) -> Unit
) {
    val alpha = remember(pageIndex) { Animatable(0f) }
    val offsetY = remember(pageIndex) { Animatable(12f) }
    
    LaunchedEffect(pageIndex) {
        delay(pageOffset * 150L)
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 600, easing = EaseOutQuad))
    }
    LaunchedEffect(pageIndex) {
        delay(pageOffset * 150L)
        offsetY.animateTo(0f, animationSpec = tween(durationMillis = 600, easing = EaseOutQuad))
    }
    
    content(
        Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value
        }
    )
}

@Composable
fun MagicalFlowSentence(
    index: Int,
    content: @Composable (Modifier) -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(12f) }
    
    LaunchedEffect(Unit) {
        val staggerDelay = (index.coerceAtMost(8) * 120L)
        delay(staggerDelay)
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 600, easing = EaseOutQuad))
    }
    LaunchedEffect(Unit) {
        val staggerDelay = (index.coerceAtMost(8) * 120L)
        delay(staggerDelay)
        offsetY.animateTo(0f, animationSpec = tween(durationMillis = 600, easing = EaseOutQuad))
    }
    
    content(
        Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value
        }
    )
}
