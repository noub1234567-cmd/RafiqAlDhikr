package com.rafiqaldhikr.app.ui.screens.worship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorshipActsUiState(
    val activeProfile: UserProfileEntity? = null,
    val activities: List<WorshipActivityEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorshipActsViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<WorshipActsUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            container.worshipRepository.observeActivities(profile.id).map { list ->
                WorshipActsUiState(profile, list)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorshipActsUiState())

    fun saveActivity(activity: WorshipActivityEntity) {
        viewModelScope.launch { container.worshipRepository.saveActivity(activity) }
    }

    fun toggleEnabled(activity: WorshipActivityEntity, enabled: Boolean) {
        viewModelScope.launch { container.worshipRepository.toggleActivityEnabled(activity, enabled) }
    }

    fun markCompletedToday(activity: WorshipActivityEntity) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch { container.worshipRepository.markActivityCompleted(profileId, activity) }
    }

    fun deleteActivity(activity: WorshipActivityEntity) {
        viewModelScope.launch { container.worshipRepository.deleteActivity(activity) }
    }
}
