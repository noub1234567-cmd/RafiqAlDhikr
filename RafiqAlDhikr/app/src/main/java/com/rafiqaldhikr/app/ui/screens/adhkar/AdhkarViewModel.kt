package com.rafiqaldhikr.app.ui.screens.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
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
import com.rafiqaldhikr.app.core.calendar.todayFlow
import java.time.LocalDate

data class AdhkarUiState(
    val activeProfile: UserProfileEntity? = null,
    val selectedCategory: AdhkarCategory = AdhkarCategory.MORNING,
    val entries: List<AdhkarEntryEntity> = emptyList(),
    val progressByEntryId: Map<Long, AdhkarProgressEntity> = emptyMap(),
    val remindersByCategory: Map<AdhkarCategory, AdhkarReminderEntity> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AdhkarViewModel(private val container: AppContainer) : ViewModel() {

    private val selectedCategoryFlow = MutableStateFlow(AdhkarCategory.MORNING)
    private val today: LocalDate get() = LocalDate.now()

    val uiState: StateFlow<AdhkarUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        todayFlow()
    ) { profile, day -> profile to day }
        .flatMapLatest { (profile, today) ->
            combine(
                selectedCategoryFlow,
                container.adhkarRepository.observeEntries(profile.id),
                container.adhkarRepository.observeProgressForDay(profile.id, today),
                container.adhkarRepository.observeReminders(profile.id)
            ) { category, entries, progress, reminders ->
                AdhkarUiState(
                    activeProfile = profile,
                    selectedCategory = category,
                    entries = entries.filter { it.category == category },
                    progressByEntryId = progress.associateBy { it.adhkarEntryId },
                    remindersByCategory = reminders.associateBy { it.category }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdhkarUiState())

    fun selectCategory(category: AdhkarCategory) {
        selectedCategoryFlow.value = category
    }

    fun increment(entry: AdhkarEntryEntity) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            container.adhkarRepository.incrementDhikr(profileId, entry, today)
        }
    }

    fun toggleCompleted(entry: AdhkarEntryEntity) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            container.adhkarRepository.toggleDhikrCompleted(profileId, entry, today)
        }
    }

    fun resetCategory() {
        val state = uiState.value
        val profileId = state.activeProfile?.id ?: return
        viewModelScope.launch {
            container.adhkarRepository.resetCategoryForDay(profileId, state.selectedCategory, today)
        }
    }

    fun saveEntry(entry: AdhkarEntryEntity) {
        viewModelScope.launch {
            container.adhkarRepository.saveCustomEntry(entry)
        }
    }

    fun deleteEntry(entry: AdhkarEntryEntity) {
        viewModelScope.launch {
            container.adhkarRepository.deleteEntry(entry)
        }
    }

    fun saveReminder(reminder: AdhkarReminderEntity) {
        viewModelScope.launch {
            container.adhkarRepository.updateReminder(reminder)
        }
    }
}
