package com.example.hujjah.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hujjah.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val userName: StateFlow<String> = userPreferences.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Awi Septian Prasetyo"
        )

    val isDarkMode: StateFlow<Boolean> = userPreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val arabicFontSize: StateFlow<Int> = userPreferences.arabicFontSize
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 22
        )

    val profileImageBase64: StateFlow<String> = userPreferences.profileImageBase64
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun updateUserName(name: String) {
        viewModelScope.launch {
            try {
                userPreferences.setUserName(name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferences.setDarkMode(enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setArabicFontSize(size: Int) {
        viewModelScope.launch {
            try {
                userPreferences.setArabicFontSize(size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfileImage(base64: String) {
        viewModelScope.launch {
            try {
                userPreferences.setProfileImageBase64(base64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
