package com.rafiqaldhikr.app.ui.screens.khatma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.data.local.db.entities.KhatmaEntity
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

data class KhatmaUiState(
    val activeProfile: UserProfileEntity? = null,
    val today: LocalDate = LocalDate.now(),
    val khatma: KhatmaEntity? = null,
    val finishedCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class KhatmaViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<KhatmaUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        todayFlow()
    ) { profile, today -> profile to today }
        .flatMapLatest { (profile, today) ->
            combine(
                container.khatmaRepository.observeActive(profile.id),
                container.khatmaRepository.observeFinishedCount(profile.id)
            ) { khatma, finished ->
                KhatmaUiState(profile, today, khatma, finished)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KhatmaUiState())

    fun start(days: Int) {
        val state = uiState.value
        val profileId = state.activeProfile?.id ?: return
        if (state.khatma != null) return
        val today = state.today.toEpochDay()
        viewModelScope.launch {
            container.khatmaRepository.start(profileId, "ختمة", today, today + days.coerceIn(1, 3650) - 1)
        }
    }

    fun setPosition(readAyahs: Int) {
        val state = uiState.value
        val k = state.khatma ?: return
        viewModelScope.launch { container.khatmaRepository.setReadAyahs(k.id, readAyahs, state.today.toEpochDay()) }
    }

    fun addAyahs(ayahs: Int) {
        val state = uiState.value
        val k = state.khatma ?: return
        viewModelScope.launch { container.khatmaRepository.addAyahs(k.id, ayahs, state.today.toEpochDay()) }
    }

    fun delete() {
        val k = uiState.value.khatma ?: return
        viewModelScope.launch { container.khatmaRepository.delete(k.id) }
    }
}
