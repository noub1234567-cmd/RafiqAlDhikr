package com.rafiqaldhikr.app.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfilesUiState(
    val profiles: List<UserProfileEntity> = emptyList(),
    val activeProfileId: Long = -1L
)

class ProfilesViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<ProfilesUiState> = combine(
        container.profileRepository.profilesFlow,
        container.preferences.settingsFlow
    ) { profiles, settings ->
        ProfilesUiState(profiles, settings.activeProfileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfilesUiState())

    fun createProfile(name: String, colorHex: Long, note: String) {
        viewModelScope.launch {
            val id = container.profileRepository.createProfile(name, colorHex, note)
            container.profileRepository.switchProfile(id)
        }
    }

    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch { container.profileRepository.updateProfile(profile) }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch { container.profileRepository.switchProfile(profileId) }
    }

    fun deleteProfile(profile: UserProfileEntity) {
        viewModelScope.launch { container.profileRepository.deleteProfile(profile) }
    }
}
