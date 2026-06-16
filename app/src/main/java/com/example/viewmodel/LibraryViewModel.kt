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

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val storyDao = database.generatedStoryDao()
    
    val stories: StateFlow<List<GeneratedStoryContent>> = storyDao.getAllStories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val isLoading = MutableStateFlow(false)
}
