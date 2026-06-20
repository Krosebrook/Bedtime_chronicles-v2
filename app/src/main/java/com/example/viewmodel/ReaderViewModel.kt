package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DatabaseProvider
import com.example.data.GeneratedStoryContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val storyDao = database.generatedStoryDao()
    
    private val _story = MutableStateFlow<GeneratedStoryContent?>(null)
    val story: StateFlow<GeneratedStoryContent?> = _story

    val allStories: StateFlow<List<GeneratedStoryContent>> = storyDao.getAllStories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadStory(id: String) {
        viewModelScope.launch {
            storyDao.getStoryById(id).collect { roomStory ->
                if (roomStory != null) {
                    _story.value = roomStory
                    com.example.data.RecentlyViewedManager.addRecentlyViewed(roomStory)
                } else {
                    // Fallback to MMKV persistent storage cache for offline/recently viewed stories
                    val mmkvStory = com.example.data.RecentlyViewedManager.getRecentlyViewed().find { it.id == id }
                    _story.value = mmkvStory
                }
            }
        }
    }
}
