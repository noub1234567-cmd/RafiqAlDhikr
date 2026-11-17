package com.rafiqaldhikr.app.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.planner.PlannerWeek
import com.rafiqaldhikr.app.data.local.db.entities.PlannerSlotEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerWeekEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlannerUiState(
    val activeProfile: UserProfileEntity? = null,
    val today: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = PlannerWeek.weekStart(LocalDate.now()),
    val week: PlannerWeekEntity? = null,
    val slots: List<PlannerSlotEntity> = emptyList(),
    val hasPrevious: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.plannerRepository

    val uiState: StateFlow<PlannerUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        todayFlow()
    ) { profile, today -> profile to today }
        .flatMapLatest { (profile, today) ->
            val start = PlannerWeek.weekStart(today)
            val slotsFlow = repo.observeWeek(profile.id, start.toEpochDay()).flatMapLatest { week ->
                if (week == null) flowOf(week to emptyList<PlannerSlotEntity>())
                else repo.observeSlots(week.id).map { week to it }
            }
            combine(slotsFlow, repo.observePreviousCount(profile.id, start.toEpochDay())) { (week, slots), prev ->
                PlannerUiState(profile, today, start, week, slots, prev > 0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlannerUiState())

    fun createNew(rows: Int) {
        val s = uiState.value
        val pid = s.activeProfile?.id ?: return
        viewModelScope.launch { repo.createWeek(pid, s.weekStart.toEpochDay(), rows) }
    }

    fun copyLast() {
        val s = uiState.value
        val pid = s.activeProfile?.id ?: return
        viewModelScope.launch { repo.copyLastWeek(pid, s.weekStart.toEpochDay()) }
    }

    fun setRows(rows: Int) {
        val w = uiState.value.week ?: return
        viewModelScope.launch { repo.setRows(w.id, rows) }
    }

    fun setAutoRepeat(enabled: Boolean) {
        val w = uiState.value.week ?: return
        viewModelScope.launch { repo.setAutoRepeat(w.id, enabled) }
    }

    fun saveSlot(
        slotId: Long?, rowIndex: Int, dayIndex: Int, title: String, type: String,
        timeMinutes: Int, extraDays: Set<Int>
    ) {
        val w = uiState.value.week ?: return
        viewModelScope.launch {
            repo.saveSlot(w.id, slotId, rowIndex, dayIndex, title, type, timeMinutes, extraDays)
        }
    }

    fun toggleDone(slotId: Long) {
        viewModelScope.launch { repo.toggleDone(slotId, System.currentTimeMillis()) }
    }

    fun deleteSlot(slotId: Long) {
        viewModelScope.launch { repo.deleteSlot(slotId) }
    }
}
