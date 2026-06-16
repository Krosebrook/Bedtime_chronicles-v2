package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StickerBookViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val storyDao = database.generatedStoryDao()
    private val placedStickerDao = database.placedStickerDao()
    private val appPreferences = AppPreferences.getInstance(application)

    // Unlocked stickers dynamically resolved based on stories found in history
    val unlockedStickers: StateFlow<Set<String>> = storyDao.getAllStories()
        .map { stories ->
            val unlocked = mutableSetOf<String>()
            // Default starter stickers are always unlocked
            unlocked.add("star")
            unlocked.add("comet")
            unlocked.add("planet")

            // Identify themed matches from historical titles or paragraphs content
            availableStickers.forEach { sticker ->
                if (sticker.unlockKeywords.isEmpty()) {
                    unlocked.add(sticker.id)
                } else {
                    val isUnlocked = stories.any { story ->
                        val combinedText = "${story.title} ${story.content}".lowercase()
                        sticker.unlockKeywords.any { keyword ->
                            combinedText.contains(keyword.lowercase())
                        }
                    }
                    if (isUnlocked) {
                        unlocked.add(sticker.id)
                    }
                }
            }
            unlocked.toSet()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf("star", "comet", "planet")
        )

    val placedStickers: StateFlow<List<PlacedSticker>> = placedStickerDao.getAllPlacedStickers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedBackground = MutableStateFlow(appPreferences.getStickerBackground())
    val selectedBackground: StateFlow<String> = _selectedBackground.asStateFlow()

    fun placeSticker(stickerId: String) {
        viewModelScope.launch {
            // Spawn in the center area initially with clean stats
            val newSticker = PlacedSticker(
                stickerId = stickerId,
                x = 0.5f,
                y = 0.4f,
                scale = 1.0f,
                rotation = 0.0f
            )
            placedStickerDao.insertPlacedSticker(newSticker)
        }
    }

    fun updateStickerPosition(sticker: PlacedSticker, x: Float, y: Float) {
        viewModelScope.launch {
            placedStickerDao.updatePlacedSticker(sticker.copy(x = x, y = y))
        }
    }

    fun updateStickerScaleAndRotation(sticker: PlacedSticker, scale: Float, rotation: Float) {
        viewModelScope.launch {
            placedStickerDao.updatePlacedSticker(sticker.copy(scale = scale, rotation = rotation))
        }
    }

    fun deleteSticker(id: Int) {
        viewModelScope.launch {
            placedStickerDao.deletePlacedSticker(id)
        }
    }

    fun clearCanvas() {
        viewModelScope.launch {
            placedStickerDao.clearAllPlacedStickers()
        }
    }

    fun selectBackground(bgId: String) {
        appPreferences.setStickerBackground(bgId)
        _selectedBackground.value = bgId
    }
}
