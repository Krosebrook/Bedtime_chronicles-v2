package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.data.StoryItem
import com.example.data.sampleStories
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay

data class StoryCategory(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val imageUrl: String, val emoji: String = "✨")

val sampleCategories = listOf(
    StoryCategory("Courage", Icons.Default.Star, "https://lh3.googleusercontent.com/aida-public/AB6AXuAhDB631M-vLrCeBAlpf2IOQscUeN8Sjxnm7n0TsKW2WG2SsOMu9zyMHfUBetoqpRn3qR_t0toNNuqZ8uXiXwADmpxDpAL8kjZx8nTNV3iEIZ_SoczN4ggeL9-8TYgTI6z9bqITlOXFiWtKDQRQZ2rO1xtt15M6p_VIHMiNsSVjgJCEPruZJxWva783KTHus-cdRQCGTVc0sHGMTc6C0IGu7W1PKSLP6NimsLpIV5yMkTib3YVsw-e9rw4CgVJ6c_RXj04mRAUw7fY", "🦁"),
    StoryCategory("Kindness", Icons.Default.Favorite, "https://lh3.googleusercontent.com/aida-public/AB6AXuBIOKJARSbpH3XoJuixmaeO192K9kjRVQ1cSy4HtEcEuEjYEZZGqfn6F5Dewj8W_KaXzXmq9hCmyIVoQ8en-fSKIEzWOR_M7OJJ54fhyWIhQfzuaUOtFurySxFd1xgWfXH54tI_jv-9-i7PtvP_oUzJrcAnUMtRawLle2ivePGjkvAyHe5epQ9dxX0MfWXjmhS1r4iibuOnsD3hWf7GSeYpFVXI9j9uYGWVilXy2EllZ6SS6ici1o-H4lFmaTq113tBsI0rECaK5GA", "💗"),
    StoryCategory("Friendship", Icons.Default.Groups, "https://lh3.googleusercontent.com/aida-public/AB6AXuAkpvSxcLcwXtCUTDOJztawVqDdTLqqIYUvVKx0pERU1KcNLZvxdedll9VpQgmrIWQL6D8CGj3_S_kfRXdLl1UWqT0-VqCB0EgyH8zfAMGDSx544NAGASBeEAQg25KKPhnLexhQnV9y64lPlFfuM9quz2wlGPl-XlLI8pyhcta1Zj9DnI2r0v3pNnQpc_kdX4uM80HwFtZnNnBjV8TdG_ET-W7KjfgAezMlqlEcvXaspz51xRePI0He5NbZ_1-9YAqqBvi5nHStIUo", "🤝"),
    StoryCategory("Wonder", Icons.Default.AutoAwesome, "https://lh3.googleusercontent.com/aida-public/AB6AXuAhDB631M-vLrCeBAlpf2IOQscUeN8Sjxnm7n0TsKW2WG2SsOMu9zyMHfUBetoqpRn3qR_t0toNNuqZ8uXiXwADmpxDpAL8kjZx8nTNV3iEIZ_SoczN4ggeL9-8TYgTI6z9bqITlOXFiWtKDQRQZ2rO1xtt15M6p_VIHMiNsSVjgJCEPruZJxWva783KTHus-cdRQCGTVc0sHGMTc6C0IGu7W1PKSLP6NimsLpIV5yMkTib3YVsw-e9rw4CgVJ6c_RXj04mRAUw7fY", "✨"),
    StoryCategory("Imagination", Icons.Default.Brush, "https://lh3.googleusercontent.com/aida-public/AB6AXuDzLJOJXgyvRM_hVP7B8VhVyW-tyCHeLmfm9QXmk6O6B_7vblb19izZPBAAcjVjiDiDtcdpr3a3AJH9RyiT5dis4EKy28RglLQv1W4zGoY-Buvo40T9fLEubemXkvHyoQvjsMfNfG9uH0rBp5mVs4QvMMWMfuordBql6dt_kaMMHWf2nX52IYzV4rBEYvbT4kcotM9RSCwy4yFNhIP3IVFZIS6UNyX2YnH-sMU4GQBQMrVCw7orE5OYuaFWoPMIdkD5JeoweYRs9zM", "🌈"),
    StoryCategory("Comfort", Icons.Default.Cloud, "https://lh3.googleusercontent.com/aida-public/AB6AXuDVCPpx1C5l9uVA_ARgxN07-9zv7umumEX3ExmL4NVK3dhNKlFUJIVo3reOZZxVZm9O36tcC9chAYnE1efAZlxx-khnnHVeC9ICy9fGmOb4YmUT-JtlM1sH9a02rnqoIRb6LDE3bWQnEuA--lioHOve0VIuFUQ-Ib_c_bOYS4t9hZqDo_wFLsLIiTJir18NY2y25peblBBLgy1YU_v5yIRiFzdxTVaCw0yDJk2WaxKes2vygIBxVu7B8UaT36f5dfFgRyyczUfcxE0", "🧸")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStory: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToAdventure: () -> Unit,
    onNavigateToStickerBook: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToCharacterCreator: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Infinity Bedtime Chronicles", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHelp,
                        modifier = Modifier.testTag("home_help_button")
                    ) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Help & FAQ", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToStickerBook) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Sticker Book", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(onNavigateToProfile, onNavigateToCreate, onNavigateToLibrary)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Welcoming Message for new users
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, top = 16.dp, bottom = 8.dp)
                    .testTag("welcome_message_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Welcome to Infinity Bedtime Chronicles! 🌙",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create magical, custom bedtime stories using AI, build your own heroes, and enjoy soothing audio soundscapes to drift off to sleep peacefully.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Quick Tip: Tap the 'Cosmic Adventure Maker' below to craft your very first custom story!",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            SearchBar()

            // Cosmic Adventure Launcher Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToAdventure() }
                    .testTag("adventure_launcher_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "NEW CHRONICLE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cosmic Adventure Maker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Design your custom bedtime hero and choose your own path!",
                                fontSize = 11.sp,
                                color = Slate300
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to Adventure",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Celestial Character Creator Launcher Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToCharacterCreator() }
                    .testTag("character_creator_launcher_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "CHARACTER CREATOR",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                    "Celestial Character Creator",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Design customized bedtime heroes, appearance, names & stories!",
                                fontSize = 11.sp,
                                color = Slate300
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to Character Creator",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Sticker Book Launcher Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToStickerBook() }
                    .testTag("sticker_book_launcher_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "STICKER BOOK",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cosmic Sticker Saga",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Unlock characters and locations as you read tales!",
                                fontSize = 11.sp,
                                color = Slate300
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to Sticker Book",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Constellation Block
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Cosmic Constellation Creator ✨",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Tap to ignite stars or drag to draw custom starlight paths across the cosmos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                InteractiveConstellationCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            CategoriesRow()
            TabsRow()
            StoriesGrid(onNavigateToStory)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search for a magical tale...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = GlassWhite,
            focusedContainerColor = GlassWhite,
            unfocusedBorderColor = GlassBorder,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun CategoriesRow() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sampleCategories.forEach { category ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 112.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        AsyncImage(
                            model = category.imageUrl,
                            contentDescription = category.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${category.emoji} ${category.name}", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun TabsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("New Arrivals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.height(2.dp).width(40.dp).background(MaterialTheme.colorScheme.primary))
        }
        Text("Favorites", fontWeight = FontWeight.Medium, color = Slate500)
        Text("AI Crafted", fontWeight = FontWeight.Medium, color = Slate500)
    }
}

@Composable
fun StoriesGrid(onNavigateToStory: (String) -> Unit) {
    // In a regular Column with scroll, we recreate grid logic manually or use fixed height.
    // For simplicity, we just use two columns with rows.
    val itemsPerRow = 2
    val chunked = sampleStories.chunked(itemsPerRow)
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        chunked.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { story ->
                    Box(modifier = Modifier.weight(1f)) {
                        StoryCard(story, onClick = { onNavigateToStory(story.id) })
                    }
                }
                if (rowItems.size < itemsPerRow) {
                    repeat(itemsPerRow - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StoryCard(story: StoryItem, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(12.dp))
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
                            startY = 150f
                        )
                    )
            )
            if (story.isEditorsPick) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DeepIndigo)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("EDITOR'S PICK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            if (story.isAiCrafted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MagicPurple)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("AI CRAFTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(story.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("${story.readTime} • ${story.category}", fontSize = 10.sp, color = Slate400)
    }
}

@Composable
fun BottomNavigationBar(onNavigateToProfile: () -> Unit, onNavigateToCreate: () -> Unit, onNavigateToLibrary: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "create_button_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_scale"
    )

    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shadow_scale"
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shadow_alpha"
    )

    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_press_scale"
    )

    var showTooltip by remember { mutableStateOf(false) }

    LaunchedEffect(showTooltip) {
        if (showTooltip) {
            delay(5000)
            showTooltip = false
        }
    }

    val tooltipAlpha by animateFloatAsState(
        targetValue = if (showTooltip) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 300),
        label = "tooltip_alpha"
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = Slate500,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Slate500,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Library") },
            label = { Text("Library", fontSize = 10.sp) },
            selected = false,
            onClick = onNavigateToLibrary
        )
        
        // Helper animation for floating tooltip bounce
        val tooltipBounce by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tooltip_bounce"
        )

        // Center Create Button with Pulse Glow Animation and Tooltip
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Idle Tooltip to guide new users
            if (!isPressed && !showTooltip) {
                Box(
                    modifier = Modifier
                        .offset(y = (-56 + tooltipBounce).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Tap to Create!",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pulsing background ring representing the stardust magical glow
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(shadowScale)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = shadowAlpha),
                        shape = CircleShape
                    )
            )

            // Primary Create Button with subtle pulsing scale
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale * pressScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MagicPurple
                            )
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isPressed = false
                                }
                            },
                            onLongPress = {
                                showTooltip = true
                            },
                            onTap = {
                                onNavigateToCreate()
                            }
                        )
                    }
                    .testTag("center_create_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Create",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Long-press explanation tooltip popup
            if (showTooltip || tooltipAlpha > 0.0f) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = androidx.compose.ui.unit.IntOffset(0, -310),
                    onDismissRequest = { showTooltip = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .padding(8.dp)
                            .alpha(tooltipAlpha)
                            .testTag("create_button_tooltip"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BackgroundDark,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MagicPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Bedtime Magic",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Weave custom bedtime stories using AI! Choose a brave companion, custom theme, and cozy setting. Our gentle storyteller crafts text and magical scenery illustrations with twilight ambient loops to guide you into a peaceful slumber.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showTooltip = false },
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("tooltip_dismiss_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = "Magic Got It!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Saved") },
            label = { Text("Saved", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 10.sp) },
            selected = false,
            onClick = onNavigateToProfile
        )
    }
}

@Composable
fun InteractiveConstellationCanvas(modifier: Modifier = Modifier) {
    // Current stars state
    var stars by remember {
        mutableStateOf(
            List(20) {
                StarNode(
                    x = (10..90).random() / 100f,
                    y = (10..90).random() / 100f,
                    radius = (4..8).random().toFloat(),
                    brightness = (4..10).random() / 10f,
                    speedX = ((-15..15).random() / 100000f),
                    speedY = ((-15..15).random() / 100000f)
                )
            }
        )
    }

    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var ripples by remember { mutableStateOf(listOf<TouchRipple>()) }

    // Pulsate lines and stars for glowing animation
    val infiniteTransition = rememberInfiniteTransition(label = "StarAnimation")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Animation ticker side-effect
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { _ ->
                stars = stars.map { star ->
                    var newX = star.x + star.speedX
                    var newY = star.y + star.speedY
                    var newSpeedX = star.speedX
                    var newSpeedY = star.speedY
                    
                    if (newX < 0.02f || newX > 0.98f) {
                        newSpeedX = -newSpeedX
                        newX = newX.coerceIn(0.02f, 0.98f)
                    }
                    if (newY < 0.02f || newY > 0.98f) {
                        newSpeedY = -newSpeedY
                        newY = newY.coerceIn(0.02f, 0.98f)
                    }
                    star.copy(x = newX, y = newY, speedX = newSpeedX, speedY = newSpeedY)
                }

                if (ripples.isNotEmpty()) {
                    ripples = ripples.mapNotNull { ripple ->
                        if (ripple.progress >= 1.0f) null
                        else ripple.copy(progress = ripple.progress + 0.02f)
                    }
                }
            }
            kotlinx.coroutines.delay(16)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B24),
                        Color(0xFF05030E)
                    )
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchPoint = offset
                        val relativeX = offset.x / size.width
                        val relativeY = offset.y / size.height
                        stars = stars + StarNode(
                            x = relativeX.coerceIn(0f, 1f),
                            y = relativeY.coerceIn(0f, 1f),
                            radius = (5..9).random().toFloat(),
                            brightness = 1.0f,
                            speedX = ((-12..12).random() / 100000f),
                            speedY = ((-12..12).random() / 100000f)
                        )
                        ripples = ripples + TouchRipple(offset.x, offset.y, 0f)
                    },
                    onDrag = { change, _ ->
                        touchPoint = change.position
                    },
                    onDragEnd = {
                        touchPoint = null
                    },
                    onDragCancel = {
                        touchPoint = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    touchPoint = offset
                    val relativeX = offset.x / size.width
                    val relativeY = offset.y / size.height
                    stars = stars + StarNode(
                        x = relativeX.coerceIn(0f, 1f),
                        y = relativeY.coerceIn(0f, 1f),
                        radius = (7..11).random().toFloat(),
                        brightness = 1.0f,
                        speedX = ((-12..12).random() / 100000f),
                        speedY = ((-12..12).random() / 100000f)
                    )
                    ripples = ripples + TouchRipple(offset.x, offset.y, 0f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Connect nearby stars with a starlight line
            stars.forEachIndexed { i, starA ->
                val posA = Offset(starA.x * width, starA.y * height)
                
                for (j in i + 1 until stars.size) {
                    val starB = stars[j]
                    val posB = Offset(starB.x * width, starB.y * height)
                    
                    val dx = posA.x - posB.x
                    val dy = posA.y - posB.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    
                    val threshold = 180f
                    if (distance < threshold) {
                        val alpha = ((1.0f - (distance / threshold)) * 0.22f * pulse).coerceIn(0f, 1f)
                        drawLine(
                            color = Color(0xFFA197FF),
                            start = posA,
                            end = posB,
                            strokeWidth = 1.dp.toPx(),
                            alpha = alpha
                        )
                    }
                }

                // Connect active touchpoint to nearby stars
                touchPoint?.let { touch ->
                    val dx = posA.x - touch.x
                    val dy = posA.y - touch.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    val touchThreshold = 250f
                    if (distance < touchThreshold) {
                        val alpha = ((1.0f - (distance / touchThreshold)) * 0.7f).coerceIn(0f, 1f)
                        drawLine(
                            color = Color(0xFFD4B2FF),
                            start = posA,
                            end = touch,
                            strokeWidth = 1.5.dp.toPx(),
                            alpha = alpha
                        )
                    }
                }
            }

            // Draw Stars
            stars.forEach { star ->
                val starCenter = Offset(star.x * width, star.y * height)
                val currentBrightness = star.brightness * pulse
                
                // Outer Glow
                drawCircle(
                    color = Color(0xFF886BFF),
                    radius = star.radius * 3f,
                    center = starCenter,
                    alpha = 0.15f * currentBrightness
                )
                
                // Core
                drawCircle(
                    color = Color.White,
                    radius = star.radius,
                    center = starCenter,
                    alpha = 0.9f * currentBrightness
                )
            }

            // Draw Ripples
            ripples.forEach { ripple ->
                val maxRippleRadius = 90f
                val currentRadius = ripple.progress * maxRippleRadius
                val alpha = (1.0f - ripple.progress).coerceIn(0f, 1f)
                
                drawCircle(
                    color = Color(0xFFE9C5FF),
                    radius = currentRadius,
                    center = Offset(ripple.x, ripple.y),
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = alpha * 0.5f
                )
            }
        }
    }
}

data class StarNode(
    val x: Float,
    val y: Float,
    val radius: Float,
    val brightness: Float,
    val speedX: Float,
    val speedY: Float
)

data class TouchRipple(
    val x: Float,
    val y: Float,
    val progress: Float
)
