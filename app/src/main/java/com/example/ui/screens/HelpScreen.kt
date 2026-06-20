package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GenerateContentRequest
import com.example.data.Content
import com.example.data.Part
import com.example.data.RetrofitClient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// FAQ data structure representing bedtime chronicles user scenarios
data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Core parameters for lifted sandbox state so examples can wire parameters dynamically
    var sandboxCommand by remember { mutableStateOf("poem") } // "summary", "translate", "poem", "quiz"
    var sandboxPromptSubject by remember { mutableStateOf("Sparky the friendly starlight puppy cuddle-spinning with bioluminescent glow-bugs.") }

    var ttsInitialized by remember { mutableStateOf(false) }
    val ttsHelper = remember {
        TextToSpeechHelper(context) { success ->
            ttsInitialized = success
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.stop()
            ttsHelper.shutdown()
        }
    }

    val categories = listOf("All", "AI Guide", "Adventure", "Sleep", "Customization")

    val faqList = listOf(
        FaqItem(
            id = "ai_companion_intro",
            question = "What is the AI Bedtime Companion?",
            answer = "The AI Bedtime Companion is powered by the Google Gemini API. It processes comforting bedtime choices (like selected cosmic characters, dream origins, and sleep goals) to generate tailor-made stories that settle young evening minds and conclude with relaxing, safe final highlights.",
            category = "AI Guide"
        ),
        FaqItem(
            id = "ai_basic_commands",
            question = "How do I ask questions or give basic commands?",
            answer = "You can direct the AI by selecting characters, and inserting cozy traits into story configurations. Effective basic prompting focuses on low-stakes, nurturing interactions, such as: 'Help Captain Stardust check constellations' or 'Cuddle with sleepy stardust puffbirds'. Always avoid high stakes or scary themes to ensure gentle sleep transitions.",
            category = "AI Guide"
        ),
        FaqItem(
            id = "ai_advanced_commands_faq",
            question = "What Advanced AI Commands are supported?",
            answer = "Our screen provides 4 rich, pre-packaged 'Advanced AI Bedtime Commands': 1) Summarization (📜) to condense journeys into 2-sentence mantras, 2) Translation (🌍) to translate comforting blessings, 3) Cozy Bedtime Poems (🌸) for rhyming lyrics, and 4) Calm Quizzes (🌟) with stress-free memory matches.",
            category = "AI Guide"
        ),
        FaqItem(
            id = "child_safety",
            question = "Are the Gemini stories safe for young children?",
            answer = "Absolutely. We enforce a strictly designed child safety system. Every Gemini API prompt utilizes strict filtering constraints, cozy sleep themes, and positive, reassuring outcomes so that children only receive cozy, gentle, and child-safe moral guidance before sleeping.",
            category = "AI Guide"
        ),
        FaqItem(
            id = "create_adventure",
            question = "How do I start a new personalized adventure?",
            answer = "To begin, go to the Home screen and tap the floating \"Tap to Create!\" button. From there, select a hero character, name them, choose their origins, select their bedtime motivation, and pick a cosmic milestone task. Press the \"Begin Adventure\" button, and Gemini API will assemble a safe, tailored story for you!",
            category = "Adventure"
        ),
        FaqItem(
            id = "cosmic_satchel",
            question = "What is the Cosmic Satchel and how do I search?",
            answer = "The Cosmic Satchel is your magical inventory with a 5-item carry limit. In an adventure story, you can click \"Search Surroundings\" to search the cosmic space and discover rare bedtime tools like the Stardust Compass or Sleeping Moonstone. Tap \"Use\" on an item to consume it—doing so drops the item from your Satchel and triggers a customized story path written by Gemini!",
            category = "Adventure"
        ),
        FaqItem(
            id = "sleep_timer",
            question = "How does the Sleep Timer protect sleeping kids?",
            answer = "When reading or listening to a bedtime story, you can specify a sleep countdown timer. Unlike standard players that cut off audio abruptly (which easily awakens light sleep states), our system runs a linear 10-second fade volume decay to transition your child gently into full silent slumber.",
            category = "Sleep"
        ),
        FaqItem(
            id = "offline_mode",
            question = "Can I use the app on airplanes or without reception?",
            answer = "Yes! If the network connection is lost or offline, the app seamlessly activates a high-fidelity offline system fallback utilizing localized offline pre-built story assets, custom synthesized soundscapes, and offline database profiles so bedtime is never delayed.",
            category = "Sleep"
        ),
        FaqItem(
            id = "change_voice",
            question = "How do I change the narrator's voice or speed?",
            answer = "Navigate to your Profile tab first. Locate the \"Text-to-Speech Settings\" panel. You can try 4 tailored bedtime narrator styles (Standard, Cosmic Sage, Starlight Sprite, and Gentle Dreamer), adjust speech pitch sliders, or alter reading rate speeds using fully customizable slider handles.",
            category = "Customization"
        )
    )

    // Filter FAQs based on query and selected category
    val filteredFaqs = faqList.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() || 
                           item.question.contains(searchQuery, ignoreCase = true) || 
                           item.answer.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Help & Fairy Guides", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("help_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        // Background subtle magical grid/atmosphere
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header overview Card which defines visual presence
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("help_overview_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Magical guide icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Cosmic Library Guide",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Welcome, cosmic explorer! Infinity Bedtime Chronicles blends interactive storytelling with calming sleep techniques and Gemini AI to help children fall asleep happily.",
                                fontSize = 12.sp,
                                color = Slate300,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Core Mechanics Section title
                Text(
                    text = "Core Magical Features",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 2-row feature summary list
                FeatureSummaryRow()

                Spacer(modifier = Modifier.height(24.dp))

                // Section Title - Frequently Asked Questions
                Text(
                    text = "Interactive AI Sandbox & Bedtime Guides",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // FAQ Search query input field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search help guides for questions...", color = Slate400, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("help_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search FAQ", tint = Slate300)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("help_search_clear")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search", tint = Slate300)
                            }
                        }
                    },
                    singleLine = true
                )

                // FAQ Horizontal Category Chips Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else GlassWhite
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else GlassBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("help_category_chip_${category.replace(" ", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else Slate300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // AI Guide Special Sections
                if (selectedCategory == "AI Guide") {
                    
                    // 1. Render Interactive AI Sandbox
                    AiCommandSandbox(
                        context = context,
                        ttsHelper = ttsHelper,
                        selectedCommand = sandboxCommand,
                        onCommandChange = { sandboxCommand = it },
                        promptSubject = sandboxPromptSubject,
                        onPromptChange = { sandboxPromptSubject = it }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 2. Render Help Section explaining basic commands, how to ask questions, and examples of effective prompts
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_rules_card"),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite),
                        border = BorderStroke(1.dp, GlassBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ HOW TO ASK: PROMPT RECIPES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Safe Prompting & Content Rules",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "To assemble beautiful, comforting results from Gemini AI models, construct custom adventure prompts utilizing these 3 Cozy Recipes:",
                                fontSize = 11.sp,
                                color = Slate300,
                                lineHeight = 15.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val rules = listOf(
                                Pair("🦖 Nurturing Character Focus", "Incorporate friendly character descriptions, such as 'Sparky the shy green dragon' or 'Pippin the sleepy stardust owl'."),
                                Pair("🎒 Low-Stakes Bedtime Quests", "Focus on gentle motivations with happy resolutions, such as 'looking for cozy stardust pillows' or 'resting on lilac marshmallow clouds'."),
                                Pair("💤 Soothing Sensory Descriptors", "Add sleepy nouns and warm cues like 'soft slow-paced starlight breeze', 'lavender night skies', or 'comfortable breathing rhythms'.")
                            )
                            
                            rules.forEach { (title, description) ->
                                Row(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🌙", fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(description, fontSize = 10.sp, color = Slate300, lineHeight = 13.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = GlassBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                  text = "💡 HIGH-PERFORMANCE PROMPT EXAMPLES",
                                  fontSize = 10.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.secondary,
                                  letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                  text = "Tap a pro-preset bedtime prompt to load it into the sandbox above and witness creative transformations live!",
                                  fontSize = 10.sp,
                                  color = Slate300,
                                  lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val examples = listOf(
                                Triple(
                                    "A slow-paced bedtime narrative with Captain Stardust finding a cozy cloud bed where starlight owls hum a sweet sleep frequency.",
                                    "summary",
                                    "📜 Summarize Example"
                                ),
                                Triple(
                                    "May you sail safely through the warm starlight skies, relax your breathing, and sleep in total peace.",
                                    "translate",
                                    "🌍 Translate Example"
                                ),
                                Triple(
                                    "Sparky the friendly starlight puppy cuddle-spinning with glowing yellow light bugs.",
                                    "poem",
                                    "🌸 Cozy Poem Example"
                                ),
                                Triple(
                                    "A magic clockwork telescope projection-casting golden stars on snug cloud beds.",
                                    "quiz",
                                    "🌟 Calm Quiz Example"
                                )
                            )
                            
                            examples.forEach { (exampleText, cmd, label) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = label,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "PRO-PRESET",
                                                fontSize = 7.sp,
                                                color = Slate400,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"$exampleText\"",
                                            fontSize = 11.sp,
                                            color = Slate300,
                                            lineHeight = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                sandboxCommand = cmd
                                                sandboxPromptSubject = exampleText
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("Try This prompt in Sandbox", fontSize = 8.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (filteredFaqs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info, 
                                contentDescription = "No results",
                                tint = Slate400,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No matches found in bedtime logs.\nTry searching with different cosmic terms!",
                                color = Slate400,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (selectedCategory == "All") "Frequently Asked Questions" else "$selectedCategory Questions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredFaqs.forEach { faqItem ->
                            FaqCardRow(faqItem)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Bottom help guides reassuring section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("help_contact_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Still have questions about sleeping stars?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "If you have ideas, feedback, or need extra safety guidance, our support pixie team is here to assist parents anytime.",
                            color = Slate300,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* Simulated launcher */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("help_contact_button")
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact Parents Help Center", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureSummaryRow() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val features = listOf(
            Triple("💡 Gemini Brain", "Pre-vetted child safety criteria translates custom user choices into moral bedtime stories.", Icons.Default.ModelTraining),
            Triple("🔊 High-Fi Ambient", "DSP synthetic wave audio tracks (white noise, rain, waves) help cancel out distractions.", Icons.Default.VolumeUp),
            Triple("💤 Fade-out Timer", "Protects sweet dreams. 10-second volume decay smoothly transitions kids to deep sleep.", Icons.Default.Snooze),
            Triple("🏆 Milestone Badges", "Children gain bedtime stickers and unlocked badges for consistent reading habits.", Icons.Default.WorkspacePremium)
        )
        
        features.forEach { (name, details, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassWhite),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = name, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(details, fontSize = 11.sp, color = Slate300, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FaqCardRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "faq_chevron_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("faq_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else GlassBorder
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.question,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand FAQ details",
                    tint = Slate300,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = GlassBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.answer,
                        color = Slate300,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.testTag("faq_answer_${item.id}")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Category: ${item.category}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiCommandSandbox(
    context: Context,
    ttsHelper: TextToSpeechHelper?,
    selectedCommand: String,
    onCommandChange: (String) -> Unit,
    promptSubject: String,
    onPromptChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var apiError by remember { mutableStateOf<String?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    // Reset results when command changes, save resources
    LaunchedEffect(selectedCommand) {
        resultText = ""
        apiError = null
        if (isSpeaking) {
            ttsHelper?.stop()
            isSpeaking = false
        }
    }

    LaunchedEffect(ttsHelper) {
        ttsHelper?.onSpeechFinishedListener = {
            isSpeaking = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isSpeaking) {
                ttsHelper?.stop()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_sandbox_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with glowing badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW: AI SANDBOX",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bedtime Prompt Explorer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Observe how specialized 'Advanced Commands' direct Google Gemini to transform standard bedtime inputs into beautiful, cozy new shapes.",
                fontSize = 11.sp,
                color = Slate300,
                lineHeight = 15.sp
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Grid-like buttons of Advanced Commands (minimum tap target of 48dp)
            Text(
                text = "1. SELECT ADVANCED COMMAND",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Summarize
                    val summarySelected = selectedCommand == "summary"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (summarySelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else GlassWhite)
                            .border(1.dp, if (summarySelected) MaterialTheme.colorScheme.primary else GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { onCommandChange("summary") }
                            .testTag("command_btn_summary"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text("📜", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Summarize", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (summarySelected) Color.White else Slate300)
                        }
                    }
                    
                    // Translate
                    val translateSelected = selectedCommand == "translate"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (translateSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else GlassWhite)
                            .border(1.dp, if (translateSelected) MaterialTheme.colorScheme.primary else GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { onCommandChange("translate") }
                            .testTag("command_btn_translate"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text("🌍", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Translate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (translateSelected) Color.White else Slate300)
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Poem
                    val poemSelected = selectedCommand == "poem"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (poemSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else GlassWhite)
                            .border(1.dp, if (poemSelected) MaterialTheme.colorScheme.primary else GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { onCommandChange("poem") }
                            .testTag("command_btn_poem"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text("🌸", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cozy Poem", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (poemSelected) Color.White else Slate300)
                        }
                    }
                    
                    // Quiz
                    val quizSelected = selectedCommand == "quiz"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (quizSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else GlassWhite)
                            .border(1.dp, if (quizSelected) MaterialTheme.colorScheme.primary else GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { onCommandChange("quiz") }
                            .testTag("command_btn_quiz"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text("🌟", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calm Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (quizSelected) Color.White else Slate300)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // 2. Text field input for customizing the bedtime prompt
            Text(
                text = "2. CUSTOM STORY THEME OR TEXT FIELD (PROMPT INGREDIENT)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            OutlinedTextField(
                value = promptSubject,
                onValueChange = onPromptChange,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .testTag("ai_sandbox_prompt_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                ),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Action button (at least 48dp height)
            Button(
                onClick = {
                    if (promptSubject.isNotBlank()) {
                        isLoading = true
                        resultText = ""
                        apiError = null
                        if (isSpeaking) {
                            ttsHelper?.stop()
                            isSpeaking = false
                        }
                        
                        coroutineScope.launch {
                            try {
                                val finalPrompt = when (selectedCommand) {
                                    "summary" -> "Summarize the following bedtime chronicle into a highly comforting, slow-paced 2-sentence review and a soothing starlight sleep mantra. Keep it peaceful and relaxing: $promptSubject"
                                    "translate" -> "Translate this bedtime greeting: '$promptSubject' into a comforting target language (Spanish or Japanese) to make a bilingual sleep guide. First show the English, then show the cozy translation. Keep it peaceful and calming."
                                    "poem" -> "Write a sweet, calming, four-line comforting rhyming bedtime lullaby poem about: $promptSubject. Use relaxing stardust references."
                                    else -> "Create exactly three calming, friendly bedtime memory matching questions and keys about: $promptSubject to gently occupy and settle active minds before sleep. Include cozy reassuring answers immediately after."
                                }
                                
                                val responseText = withContext(Dispatchers.IO) {
                                    val request = GenerateContentRequest(
                                        contents = listOf(Content(parts = listOf(Part(text = finalPrompt))))
                                    )
                                    val response = RetrofitClient.service.generateContent(
                                        "Bearer dev-cozy-storytime-token-2026", 
                                        request
                                    )
                                    response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                                        ?: throw Exception("No response returned from the Gemini API.")
                                }
                                resultText = responseText
                            } catch (e: Exception) {
                                e.printStackTrace()
                                apiError = "Cosmic connection delayed. Please verify internet access. Engaging magic fallbacks."
                                // generate local fallback content as guaranteed child service standard
                                resultText = when (selectedCommand) {
                                    "summary" -> "Lullaby Village is warm and safe under starlight. Relax your shoulder muscles, take a slow stardust breath, and let sweet dreams drift you away. Sleep well, gentle traveler."
                                    "translate" -> "ENGLISH: May your dreams be as bright as the stars.\n\nSPANISH: Que tus sueños sean tan brillantes como las estrellas. ✨"
                                    "poem" -> "The little stars are shining cute,\nThe sleepy mountain winds are mute.\nLay down your head and close your eyes,\nAs dreams of stardust start to rise. 🌸"
                                    else -> "1. Who lit the starlight lanterns?\nAnswer: Friendly dream-elves to guide you to sleep!\n\n2. Where does the sweet marshmallow smoke drift?\nAnswer: Safely up into the soft moonlit sky!"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("ai_sandbox_submit_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading && promptSubject.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Casting AI Spell...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cast AI Spell (Run Command)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Output Section
            if (resultText.isNotEmpty() || apiError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(color = GlassBorder, thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "🧙‍♂️ NARRATOR ORACLE OUTPUT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                if (apiError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF421C1C))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = apiError!!,
                            color = Color(0xFFFFB4AB),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = resultText,
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 17.sp,
                            modifier = Modifier.testTag("ai_sandbox_output_text")
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    if (isSpeaking) {
                                        ttsHelper?.stop()
                                        isSpeaking = false
                                    } else {
                                        isSpeaking = true
                                        ttsHelper?.speak(resultText)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSpeaking) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("ai_sandbox_speak_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSpeaking) Color.Black else Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSpeaking) "Mute Narrative" else "Listen Narrator 🔊",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSpeaking) Color.Black else Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Text(
                                text = "Cozy Linear-decay volume on shutdown",
                                fontSize = 8.sp,
                                color = Slate400,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}
