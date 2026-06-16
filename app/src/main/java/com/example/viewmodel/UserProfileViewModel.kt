package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.UserProfile
import com.example.data.UserProfileDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserProfileViewModel(private val dao: UserProfileDao) : ViewModel() {
    val userProfile: StateFlow<UserProfile?> = dao.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        
    fun updateProfile(username: String, biography: String, avatarUri: String?) {
        viewModelScope.launch {
            dao.insertProfile(
                UserProfile(
                    id = 1,
                    username = username,
                    biography = biography,
                    avatarUri = avatarUri
                )
            )
        }
    }
}

class UserProfileViewModelFactory(private val dao: UserProfileDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
