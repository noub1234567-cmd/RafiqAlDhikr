package com.rafiqaldhikr.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.repository.ProfileStatisticsSnapshot
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val activeProfile: UserProfileEntity? = null,
    val stats: ProfileStatisticsSnapshot = ProfileStatisticsSnapshot()
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(private val container: AppContainer) : ViewModel() {
    val uiState: StateFlow<StatisticsUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            container.statisticsRepository.observeStatistics(profile.id).map { stats ->
                StatisticsUiState(profile, stats)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
}
