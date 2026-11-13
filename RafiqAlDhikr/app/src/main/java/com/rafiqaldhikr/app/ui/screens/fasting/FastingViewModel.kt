package com.rafiqaldhikr.app.ui.screens.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.model.FastingOccasion
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
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
import java.time.YearMonth

data class FastingUiState(
    val activeProfile: UserProfileEntity? = null,
    val displayedMonth: YearMonth = YearMonth.now(),
    val hijriOffsetDays: Int = 0,
    val fastingDays: List<FastingDayEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class FastingViewModel(private val container: AppContainer) : ViewModel() {

    private val monthFlow = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<FastingUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        container.preferences.settingsFlow,
        monthFlow
    ) { profile, settings, ym ->
        Triple(profile, settings.hijriOffsetDays, ym)
    }.flatMapLatest { (profile, offset, ym) ->
        container.fastingRepository.observeFastingDays(profile.id).combine(monthFlow) { days, currentYm ->
            FastingUiState(
                activeProfile = profile,
                displayedMonth = currentYm,
                hijriOffsetDays = offset,
                fastingDays = days
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FastingUiState())

    fun previousMonth() {
        monthFlow.value = monthFlow.value.minusMonths(1)
    }

    fun nextMonth() {
        monthFlow.value = monthFlow.value.plusMonths(1)
    }

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch { container.preferences.setHijriOffsetDays(offset) }
    }

    fun saveFastingDay(
        date: LocalDate,
        occasion: FastingOccasion,
        isPlanned: Boolean,
        isCompleted: Boolean,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int,
        notes: String
    ) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            container.fastingRepository.saveFastingDay(
                profileId = profileId,
                date = date,
                occasion = occasion,
                isPlanned = isPlanned,
                isCompleted = isCompleted,
                reminderEnabled = reminderEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                notes = notes
            )
        }
    }

    fun markCompleted(day: FastingDayEntity, completed: Boolean) {
        viewModelScope.launch { container.fastingRepository.markCompleted(day, completed) }
    }

    fun removeDay(day: FastingDayEntity) {
        viewModelScope.launch { container.fastingRepository.removeFastingDay(day) }
    }
}
