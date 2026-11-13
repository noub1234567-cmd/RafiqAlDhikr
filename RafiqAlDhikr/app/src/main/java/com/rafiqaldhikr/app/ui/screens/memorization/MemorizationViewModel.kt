package com.rafiqaldhikr.app.ui.screens.memorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity
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

data class MemorizationUiState(
    val activeProfile: UserProfileEntity? = null,
    val plans: List<MemorizationPlanEntity> = emptyList(),
    val sessions: List<MemorizationSessionEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MemorizationViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<MemorizationUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            combine(
                container.memorizationRepository.observePlans(profile.id),
                container.memorizationRepository.observeSessions(profile.id)
            ) { plans, sessions ->
                MemorizationUiState(profile, plans, sessions)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemorizationUiState())

    fun savePlan(plan: MemorizationPlanEntity) {
        viewModelScope.launch { container.memorizationRepository.savePlan(plan) }
    }

    fun deletePlan(plan: MemorizationPlanEntity) {
        viewModelScope.launch { container.memorizationRepository.deletePlan(plan) }
    }

    fun recordSession(plan: MemorizationPlanEntity, isRevision: Boolean, fromAyah: Int, toAyah: Int, notes: String) {
        viewModelScope.launch {
            container.memorizationRepository.recordSession(plan, isRevision, fromAyah, toAyah, notes)
        }
    }

    fun deleteSession(session: MemorizationSessionEntity) {
        viewModelScope.launch { container.memorizationRepository.deleteSession(session) }
    }
}
