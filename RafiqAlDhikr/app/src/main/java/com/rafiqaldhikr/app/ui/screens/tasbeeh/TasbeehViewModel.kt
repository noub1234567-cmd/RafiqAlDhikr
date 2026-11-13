package com.rafiqaldhikr.app.ui.screens.tasbeeh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
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

data class TasbeehUiState(
    val activeProfile: UserProfileEntity? = null,
    val types: List<TasbeehTypeEntity> = emptyList(),
    val selectedTypeId: Long? = null,
    val todayCounts: Map<Long, Int> = emptyMap(),
    val historyLogs: List<TasbeehLogEntity> = emptyList(),
    val hapticEnabled: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TasbeehViewModel(private val container: AppContainer) : ViewModel() {

    private val selectedTypeIdFlow = MutableStateFlow<Long?>(null)
    private val today = LocalDate.now()

    val uiState: StateFlow<TasbeehUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        container.preferences.settingsFlow
    ) { profile, settings -> profile to settings.tasbeehHapticEnabled }
        .flatMapLatest { (profile, haptic) ->
            combine(
                container.tasbeehRepository.observeTypes(profile.id),
                container.tasbeehRepository.observeTodayLogs(profile.id, today),
                container.tasbeehRepository.observeAllLogs(profile.id),
                selectedTypeIdFlow
            ) { types, todayLogs, allLogs, selectedId ->
                val resolvedId = types.firstOrNull { it.id == selectedId }?.id ?: types.firstOrNull()?.id
                TasbeehUiState(
                    activeProfile = profile,
                    types = types,
                    selectedTypeId = resolvedId,
                    todayCounts = todayLogs.associate { it.tasbeehTypeId to it.count },
                    historyLogs = allLogs.filter { it.count > 0 },
                    hapticEnabled = haptic
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasbeehUiState())

    fun selectType(typeId: Long) {
        selectedTypeIdFlow.value = typeId
    }

    fun incrementSelected() {
        val state = uiState.value
        val profileId = state.activeProfile?.id ?: return
        val typeId = state.selectedTypeId ?: return
        viewModelScope.launch {
            container.tasbeehRepository.increment(profileId, typeId, today)
        }
    }

    fun resetSelectedToday() {
        val state = uiState.value
        val profileId = state.activeProfile?.id ?: return
        val typeId = state.selectedTypeId ?: return
        viewModelScope.launch {
            container.tasbeehRepository.resetToday(profileId, typeId, today)
        }
    }

    fun saveCustomType(type: TasbeehTypeEntity) {
        viewModelScope.launch { container.tasbeehRepository.saveType(type) }
    }

    fun deleteType(type: TasbeehTypeEntity) {
        viewModelScope.launch { container.tasbeehRepository.deleteType(type) }
    }
}
