package com.rafiqaldhikr.app.ui.screens.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SeasonsUiState(
    val activeProfile: UserProfileEntity? = null,
    val selectionsById: Map<String, SeasonalSelectionEntity> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonsViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<SeasonsUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            container.seasonalRepository.observeSelections(profile.id).map { list ->
                SeasonsUiState(profile, list.associateBy { it.seasonId })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeasonsUiState())

    fun saveSelection(selection: SeasonalSelectionEntity) {
        viewModelScope.launch { container.seasonalRepository.saveSelection(selection) }
    }
}
