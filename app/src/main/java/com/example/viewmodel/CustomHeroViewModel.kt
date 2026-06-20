package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CustomHero
import com.example.data.CustomHeroDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomHeroViewModel(private val dao: CustomHeroDao) : ViewModel() {
    val customHeroes: StateFlow<List<CustomHero>> = dao.getAllCustomHeroes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveHero(
        id: Int = 0,
        name: String,
        avatarEmoji: String,
        outfitColorHex: String,
        auraGlowStyle: String,
        companionType: String,
        backstoryArchetype: String,
        backstorySummary: String
    ) {
        viewModelScope.launch {
            val hero = CustomHero(
                id = id,
                name = name,
                avatarEmoji = avatarEmoji,
                outfitColorHex = outfitColorHex,
                auraGlowStyle = auraGlowStyle,
                companionType = companionType,
                backstoryArchetype = backstoryArchetype,
                backstorySummary = backstorySummary
            )
            dao.insertHero(hero)
        }
    }

    fun deleteHero(id: Int) {
        viewModelScope.launch {
            dao.deleteHeroById(id)
        }
    }
}

class CustomHeroViewModelFactory(private val dao: CustomHeroDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomHeroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomHeroViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
