package com.rafiqaldhikr.app.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.planner.PlannerReports
import com.rafiqaldhikr.app.core.planner.PlannerWeek
import com.rafiqaldhikr.app.core.planner.ReportLine
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.repository.PlannerRepository
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
import kotlinx.serialization.json.Json

data class ReportItem(val id: Long, val startEpochDay: Long, val endEpochDay: Long, val lines: List<ReportLine>) {
    val planned: Int get() = lines.sumOf { it.planned }
    val done: Int get() = lines.sumOf { it.done }
}

data class PlannerReportsUiState(
    val activeProfile: UserProfileEntity? = null,
    /** Live summary of the week in progress (null when there is no table this week). */
    val current: ReportItem? = null,
    val reports: List<ReportItem> = emptyList(),
    val months: List<ReportItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerReportsViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.plannerRepository

    val uiState: StateFlow<PlannerReportsUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        todayFlow()
    ) { profile, today -> profile to today }
        .flatMapLatest { (profile, today) ->
            val start = PlannerWeek.weekStart(today).toEpochDay()
            val currentFlow = repo.observeWeek(profile.id, start).flatMapLatest { week ->
                if (week == null) flowOf(null)
                else repo.observeSlots(week.id).map { slots ->
                    ReportItem(
                        0, start, start + PlannerWeek.DAYS - 1,
                        PlannerReports.summarize(slots.map { Triple(it.title, it.type, it.doneAtMillis != 0L) })
                    )
                }
            }
            fun items(kind: String) = repo.observeReports(profile.id, kind).map { list ->
                list.map { r ->
                    ReportItem(
                        r.id, r.startEpochDay, r.endEpochDay,
                        runCatching { Json.decodeFromString<List<ReportLine>>(r.summaryJson) }.getOrDefault(emptyList())
                    )
                }
            }
            combine(currentFlow, items(PlannerRepository.KIND_WEEK), items(PlannerRepository.KIND_MONTH)) { cur, reps, months ->
                PlannerReportsUiState(profile, cur, reps, months)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlannerReportsUiState())

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteReport(id) }
    }
}
