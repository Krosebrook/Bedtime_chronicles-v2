package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PlacedSticker
import com.example.data.Sticker
import com.example.data.availableStickers
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.viewmodel.StickerBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerBookScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: StickerBookViewModel = viewModel()

    val unlockedStickers by viewModel.unlockedStickers.collectAsStateWithLifecycle()
    val placedStickers by viewModel.placedStickers.collectAsStateWithLifecycle()
    val selectedBackground by viewModel.selectedBackground.collectAsStateWithLifecycle()

    var selectedSticker by remember { mutableStateOf<PlacedSticker?>(null) }
    var stickerToUnlockDetail by remember { mutableStateOf<Sticker?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showBackgroundPicker by remember { mutableStateOf(false) }

    // Pulsate outline highlight for selected sticker
    val infiniteTransition = rememberInfiniteTransition(label = "SelectedPulse")
    val selectedPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Cosmic Sticker Book 🎨", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sticker_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearCanvas(); selectedSticker = null },
                        modifier = Modifier.testTag("clear_canvas_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Canvas", tint = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Drag and drop Canvas with active sizing
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFD4B2FF).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Tap outside sticker to deselect
                            selectedSticker = null
                        }
                    }
                    .testTag("sticker_canvas")
            ) {
                val canvasWidth = constraints.maxWidth
                val canvasHeight = constraints.maxHeight

                // Draw high quality astronomical dynamic backgrounds
                when (selectedBackground) {
                    "nebula" -> {
                        // Ambient stellar background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0C091F), Color(0xFF04020A))
                                    )
                                )
                        ) {
                            InteractiveConstellationCanvas(modifier = Modifier.fillMaxSize())
                        }
                    }
                    "candy" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFBB6CE), Color(0xFFD6BCFA))
                                    )
                                )
                        ) {
                            Text("☁️", fontSize = 64.sp, modifier = Modifier.align(Alignment.TopStart).padding(32.dp), alpha = 0.15f)
                            Text("☁️", fontSize = 84.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(48.dp), alpha = 0.2f)
                            Text("🧁", fontSize = 48.sp, modifier = Modifier.align(Alignment.TopEnd).padding(24.dp), alpha = 0.1f)
                        }
                    }
                    "meadow" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    )
                                )
                        ) {
                            Text("🌙", fontSize = 80.sp, modifier = Modifier.align(Alignment.TopEnd).padding(40.dp), alpha = 0.4f)
                            Text("🏔️", fontSize = 120.sp, modifier = Modifier.align(Alignment.BottomCenter), alpha = 0.15f)
                        }
                    }
                    else -> { // forest
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF065F46), Color(0xFF064E3B))
                                    )
                                )
                        ) {
                            Text("🌲", fontSize = 84.sp, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), alpha = 0.25f)
                            Text("🌲", fontSize = 110.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), alpha = 0.25f)
                            Text("✨", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopStart).padding(48.dp), alpha = 0.3f)
                        }
                    }
                }

                // Place stickers
                placedStickers.forEach { placed ->
                    val stickerDef = availableStickers.find { it.id == placed.stickerId } ?: return@forEach

                    val xPx = placed.x * canvasWidth
                    val yPx = placed.y * canvasHeight

                    var dragOffsetState by remember(placed.id) { mutableStateOf(Offset(xPx, yPx)) }
                    var isDragging by remember { mutableStateOf(false) }

                    LaunchedEffect(placed.x, placed.y, isDragging) {
                        if (!isDragging) {
                            dragOffsetState = Offset(placed.x * canvasWidth, placed.y * canvasHeight)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (dragOffsetState.x - 36.dp.toPx()).toInt(),
                                    (dragOffsetState.y - 36.dp.toPx()).toInt()
                                )
                            }
                            .size(72.dp)
                            .scale(placed.scale)
                            .rotate(placed.rotation)
                            .pointerInput(placed.id) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                        selectedSticker = placed
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetState = Offset(
                                            (dragOffsetState.x + dragAmount.x).coerceIn(0f, canvasWidth.toFloat()),
                                            (dragOffsetState.y + dragAmount.y).coerceIn(0f, canvasHeight.toFloat())
                                        )
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        val pctX = dragOffsetState.x / canvasWidth
                                        val pctY = dragOffsetState.y / canvasHeight
                                        viewModel.updateStickerPosition(placed, pctX, pctY)
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                    }
                                )
                            }
                            .pointerInput(placed.id) {
                                detectTapGestures {
                                    selectedSticker = placed
                                }
                            }
                            .testTag("placed_sticker_${placed.id}")
                    ) {
                        // Highlight border glow ring
                        if (selectedSticker?.id == placed.id) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        (2 * selectedPulse).dp,
                                        Color(0xFFE9C5FF),
                                        CircleShape
                                    )
                                    .background(Color(0xFFE9C5FF).copy(alpha = 0.08f), CircleShape)
                            )
                        }

                        Text(
                            text = stickerDef.emoji,
                            fontSize = 48.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Friendly canvas empty hint state
                if (placedStickers.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🌈 Your Dreamy Canvas", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(
                                "Tap stickers on the shelf below to spawn them!\nDrag them anywhere, or change backgrounds.",
                                fontSize = 11.sp,
                                color = Slate300,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Inline editor controller for the selected sticker
            AnimatedVisibility(
                visible = selectedSticker != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedSticker?.let { activeSticker ->
                    // Find placed sticker currently to coordinate sliders
                    val realSticker = placedStickers.find { it.id == activeSticker.id } ?: activeSticker
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sticker_controls"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Scale, contentDescription = null, size = 16.dp, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Size", style = MaterialTheme.typography.labelSmall, color = Slate300)
                                }
                                Slider(
                                    value = realSticker.scale,
                                    onValueChange = { scaleValue ->
                                        viewModel.updateStickerScaleAndRotation(realSticker, scaleValue, realSticker.rotation)
                                    },
                                    valueRange = 0.5f..2.5f,
                                    modifier = Modifier.height(28.dp).testTag("slider_scale")
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RotateRight, contentDescription = null, size = 16.dp, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rotation", style = MaterialTheme.typography.labelSmall, color = Slate300)
                                }
                                Slider(
                                    value = realSticker.rotation,
                                    onValueChange = { rotValue ->
                                        viewModel.updateStickerScaleAndRotation(realSticker, realSticker.scale, rotValue)
                                    },
                                    valueRange = -180f..180f,
                                    modifier = Modifier.height(28.dp).testTag("slider_rotation")
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSticker(realSticker.id); selectedSticker = null },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                                    .testTag("delete_sticker_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Sticker", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            // Bottom drawer options: Background toggles & sticker selection panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Controls Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Categories selection
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "Stars", "Characters", "Objects", "Locations").forEach { cat ->
                                val isChosen = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("category_tab_$cat")
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosen) Color.White else Slate300
                                    )
                                }
                            }
                        }

                        // Background Toggle Trigger Button
                        Button(
                            onClick = { showBackgroundPicker = !showBackgroundPicker },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp).testTag("select_background_button")
                        ) {
                            Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Art Canvas", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Background selection drawer tray
                    AnimatedVisibility(
                        visible = showBackgroundPicker,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                Triple("nebula", "Midnight Cosmos", "🌌"),
                                Triple("candy", "Candy Kingdom", "🍭"),
                                Triple("meadow", "Silent Moonhill", "🏔️"),
                                Triple("forest", "Enchanted Woods", "🌳")
                            ).forEach { (id, label, iconEmoji) ->
                                val isSelected = selectedBackground == id
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectBackground(id) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(iconEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Slate300)
                                }
                            }
                        }
                    }

                    // Sticker Picker Roll Horizontal Drawer
                    val filteredStickers = availableStickers.filter {
                        selectedCategory == "All" || it.category == selectedCategory
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("sticker_shelf"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(filteredStickers) { sticker ->
                            val isUnlocked = unlockedStickers.contains(sticker.id)

                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                        else Color.Black.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isUnlocked) Color.Transparent else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        if (isUnlocked) {
                                            viewModel.placeSticker(sticker.id)
                                        } else {
                                            stickerToUnlockDetail = sticker
                                        }
                                    }
                                    .testTag("sticker_shelf_item_${sticker.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = sticker.emoji,
                                            fontSize = 32.sp,
                                            modifier = Modifier.scale(if (isUnlocked) 1f else 0.7f)
                                        )
                                        if (!isUnlocked) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                    .align(Alignment.Center)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    modifier = Modifier.size(14.dp).align(Alignment.Center),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sticker.name,
                                        fontSize = 8.sp,
                                        color = if (isUnlocked) Slate300 else Slate400,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Locked sticker hint popup dialog
    stickerToUnlockDetail?.let { sticker ->
        AlertDialog(
            onDismissRequest = { stickerToUnlockDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Unlock ${sticker.emoji} ${sticker.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This sticker is hidden in the starlight! To unlock it, generate or read bedtime stories containing characters or locations matching this theme:",
                        fontSize = 13.sp,
                        color = Slate300
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sticker.unlockHint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Text(
                        text = "Tip: Try making custom Cosmic Adventures with these words to unlock instantly!",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { stickerToUnlockDetail = null },
                    modifier = Modifier.testTag("dismiss_dialog_button")
                ) {
                    Text("Got it!", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
