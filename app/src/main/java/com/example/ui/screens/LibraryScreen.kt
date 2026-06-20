package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Slate400
import com.example.data.GeneratedStoryContent
import com.example.data.StoryItem
import com.example.viewmodel.LibraryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onBack: () -> Unit, onNavigateToStory: (String) -> Unit) {
    val viewModel: LibraryViewModel = viewModel()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.example.data.AppPreferences.getInstance(context) }
    val autoplayNext by prefs.autoplayNext.collectAsStateWithLifecycle()
    
    var selectedGenre by remember { mutableStateOf<String?>("All") }
    var recentlyViewedList by remember { mutableStateOf(emptyList<GeneratedStoryContent>()) }

    LaunchedEffect(selectedGenre) {
        if (selectedGenre == "Recently Viewed") {
            recentlyViewedList = com.example.data.RecentlyViewedManager.getRecentlyViewed()
        }
    }

    val allGenres = listOf(
        "All",
        "Recently Viewed",
        "Space Exploration",
        "Mythical Creatures",
        "Gentle Bedtime",
        "Adventure",
        "Fantasy",
        "Magic",
        "Cosmic",
        "Sleepy"
    )

    val filteredStories = if (selectedGenre == "Recently Viewed") {
        recentlyViewedList
    } else if (selectedGenre == "All") {
        stories
    } else {
        stories.filter { story ->
            val mappedCategory = when (story.category.lowercase()) {
                "space exploration", "space", "cosmic" -> "Space Exploration"
                "mythical creatures", "fantasy", "magic", "magical" -> "Mythical Creatures"
                "gentle bedtime", "sleepy", "dreamy", "winter", "comfort", "sleep" -> "Gentle Bedtime"
                else -> story.category
            }
            mappedCategory.equals(selectedGenre, ignoreCase = true) || story.category.equals(selectedGenre, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Your Library", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allGenres) { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre) },
                            modifier = Modifier.testTag("filter_chip_${genre.replace(" ", "_").lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Autoplay Next Series Toggle Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔄",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                "Auto-play Next Bedtime Story",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Conclude one tale, begin the next series episode",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                    Switch(
                        checked = autoplayNext,
                        onCheckedChange = { prefs.setAutoplayNext(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("library_autoplay_next_toggle")
                    )
                }

                if (selectedGenre == "Recently Viewed") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Offline Accessible Tales",
                            fontSize = 13.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                        if (recentlyViewedList.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    com.example.data.RecentlyViewedManager.clearRecentlyViewed()
                                    recentlyViewedList = emptyList()
                                },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Text("Clear History", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredStories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No stories found in this category.\nGo craft a new tale to see it here!", 
                            color = Slate400, 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredStories) { story ->
                            // Reuse the StoryItem model shape for StoryCard
                            val mappedStory = StoryItem(
                                id = story.id,
                                title = story.title,
                                readTime = "5 min",
                                category = story.category,
                                imageUrl = story.coverImageUrl,
                                isAiCrafted = true
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                               StoryCard(mappedStory, onClick = { onNavigateToStory(story.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
