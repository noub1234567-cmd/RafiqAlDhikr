package com.rafiqaldhikr.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuranUiState(
    val activeProfile: UserProfileEntity? = null,
    val goal: QuranGoalEntity? = null,
    val logs: List<QuranReadingLogEntity> = emptyList(),
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(private val container: AppContainer) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<QuranUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            combine(
                container.quranRepository.observeGoal(profile.id),
                container.quranRepository.observeLogs(profile.id)
            ) { goal, logs ->
                val todayEpoch = today.toEpochDay()
                val weekStart = today.minusDays(6).toEpochDay()
                val monthStart = today.minusDays(29).toEpochDay()
                QuranUiState(
                    activeProfile = profile,
                    goal = goal ?: QuranGoalEntity(profileId = profile.id),
                    logs = logs,
                    todayTotal = logs.filter { it.dateEpochDay == todayEpoch }.sumOf { it.amountRead },
                    weekTotal = logs.filter { it.dateEpochDay in weekStart..todayEpoch }.sumOf { it.amountRead },
                    monthTotal = logs.filter { it.dateEpochDay in monthStart..todayEpoch }.sumOf { it.amountRead }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranUiState())

    fun saveGoal(goal: QuranGoalEntity) {
        viewModelScope.launch { container.quranRepository.saveGoal(goal) }
    }

    fun logReading(amount: Double, note: String) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch { container.quranRepository.logReading(profileId, amount, note, today) }
    }

    fun deleteLog(log: QuranReadingLogEntity) {
        viewModelScope.launch { container.quranRepository.deleteLog(log) }
    }
}
