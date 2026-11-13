package com.rafiqaldhikr.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
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

data class HistoryUiState(
    val activeProfile: UserProfileEntity? = null,
    val selectedStatus: ActivityStatus? = null,
    val logs: List<ActivityLogEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val container: AppContainer) : ViewModel() {

    private val filterFlow = MutableStateFlow<ActivityStatus?>(null)

    val uiState: StateFlow<HistoryUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            combine(
                container.worshipRepository.observeLogs(profile.id),
                filterFlow
            ) { allLogs, statusFilter ->
                val filtered = if (statusFilter == null) allLogs else allLogs.filter { it.status == statusFilter }
                HistoryUiState(profile, statusFilter, filtered)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun selectFilter(status: ActivityStatus?) {
        filterFlow.value = status
    }

    fun markCompleted(log: ActivityLogEntity) {
        viewModelScope.launch { container.worshipRepository.markLogCompleted(log) }
    }

    fun undoCompletion(log: ActivityLogEntity) {
        viewModelScope.launch { container.worshipRepository.revertLogToPendingOrMissed(log) }
    }

    fun reschedule(log: ActivityLogEntity, newMillis: Long) {
        viewModelScope.launch { container.worshipRepository.rescheduleActivityReminder(log, newMillis) }
    }

    fun deleteLog(log: ActivityLogEntity) {
        viewModelScope.launch { container.worshipRepository.deleteLog(log) }
    }
}
