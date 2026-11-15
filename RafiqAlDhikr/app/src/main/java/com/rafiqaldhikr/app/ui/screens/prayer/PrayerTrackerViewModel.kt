package com.rafiqaldhikr.app.ui.screens.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.repository.PrayerItem
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PrayerTrackerUiState(
    val activeProfile: UserProfileEntity? = null,
    val today: LocalDate = LocalDate.now(),
    val day: LocalDate = LocalDate.now(),
    /** Item keys ticked on [day]. */
    val doneOnDay: Set<String> = emptySet(),
    /** Number of fard prayers ticked per day, for the last [WINDOW_DAYS] days ending today. */
    val fardPerDay: Map<LocalDate, Int> = emptyMap()
) {
    companion object {
        const val WINDOW_DAYS = 7
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerTrackerViewModel(private val container: AppContainer) : ViewModel() {

    private val selectedDay = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<PrayerTrackerUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        todayFlow()
    ) { profile, today -> profile to today }
        .flatMapLatest { (profile, today) ->
            val from = today.minusDays(PrayerTrackerUiState.WINDOW_DAYS - 1L)
            combine(
                selectedDay,
                container.prayerLogRepository.observeRange(profile.id, from.toEpochDay(), today.toEpochDay())
            ) { day, logs ->
                val shown = day.coerceIn(from, today)
                val fardKeys = PrayerItem.entries.filter { it.isFard }.map { it.key }.toSet()
                PrayerTrackerUiState(
                    activeProfile = profile,
                    today = today,
                    day = shown,
                    doneOnDay = logs.filter { it.dateEpochDay == shown.toEpochDay() }.map { it.itemKey }.toSet(),
                    fardPerDay = logs.filter { it.itemKey in fardKeys }
                        .groupingBy { LocalDate.ofEpochDay(it.dateEpochDay) }.eachCount()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerTrackerUiState())

    fun selectDay(day: LocalDate) {
        selectedDay.value = day
    }

    fun toggle(item: PrayerItem) {
        val state = uiState.value
        val profileId = state.activeProfile?.id ?: return
        viewModelScope.launch {
            container.prayerLogRepository.toggle(profileId, state.day.toEpochDay(), item)
        }
    }
}
