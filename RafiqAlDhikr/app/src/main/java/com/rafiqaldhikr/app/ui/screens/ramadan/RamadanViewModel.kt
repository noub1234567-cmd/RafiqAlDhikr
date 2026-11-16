package com.rafiqaldhikr.app.ui.screens.ramadan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.ramadan.RamadanCalendar
import com.rafiqaldhikr.app.core.ramadan.RamadanStatus
import com.rafiqaldhikr.app.data.local.db.entities.KhatmaEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.repository.PrayerItem
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

data class RamadanUiState(
    val activeProfile: UserProfileEntity? = null,
    val today: LocalDate = LocalDate.now(),
    val status: RamadanStatus? = null,
    /** Days on which taraweeh was ticked. */
    val taraweehDays: Set<Long> = emptySet(),
    val khatma: KhatmaEntity? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class RamadanViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<RamadanUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        todayFlow(),
        container.preferences.settingsFlow
    ) { profile, today, settings -> Triple(profile, today, settings.hijriOffsetDays) }
        .flatMapLatest { (profile, today, offset) ->
            val status = RamadanCalendar.statusFor(today, offset)
            val start = when (status) {
                is RamadanStatus.During -> status.start
                is RamadanStatus.Before -> status.start
            }
            val length = when (status) {
                is RamadanStatus.During -> status.length
                is RamadanStatus.Before -> status.length
            }
            combine(
                container.prayerLogRepository.observeRange(
                    profile.id, start.toEpochDay(), start.toEpochDay() + length
                ),
                container.khatmaRepository.observeActive(profile.id)
            ) { logs, khatma ->
                RamadanUiState(
                    activeProfile = profile,
                    today = today,
                    status = status,
                    taraweehDays = logs.filter { it.itemKey == PrayerItem.TARAWEEH.key }.map { it.dateEpochDay }.toSet(),
                    khatma = khatma
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RamadanUiState())

    fun toggleTaraweeh(day: LocalDate) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            container.prayerLogRepository.toggle(profileId, day.toEpochDay(), PrayerItem.TARAWEEH)
        }
    }

    /** Starts a khatma that ends on the last day of Ramadan (from today if Ramadan has begun). */
    fun startRamadanKhatma() {
        val state = uiState.value
        val profileId = state.activeProfile?.id ?: return
        if (state.khatma != null) return
        val (start, length) = when (val s = state.status) {
            is RamadanStatus.During -> state.today to s.length
            is RamadanStatus.Before -> s.start to s.length
            null -> return
        }
        val end = when (val s = state.status) {
            is RamadanStatus.During -> s.start.plusDays(s.length - 1L)
            is RamadanStatus.Before -> s.start.plusDays(s.length - 1L)
            null -> return
        }
        viewModelScope.launch {
            container.khatmaRepository.start(profileId, "ختمة رمضان", start.toEpochDay(), end.toEpochDay())
        }
    }
}
