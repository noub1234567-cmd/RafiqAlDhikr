package com.rafiqaldhikr.app.ui.screens.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.planner.PlannerWeek
import com.rafiqaldhikr.app.core.planner.ReportLine
import com.rafiqaldhikr.app.core.stats.WeekSummary
import com.rafiqaldhikr.app.core.stats.WeeklySummary
import com.rafiqaldhikr.app.core.yearlog.YearLogInputs
import com.rafiqaldhikr.app.data.local.db.entities.PlannerReportEntity
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
import kotlinx.serialization.json.Json
import java.time.LocalDate

data class WeeklyReportUiState(
    val loaded: Boolean = false,
    val thisWeek: WeekSummary? = null,
    val lastWeek: WeekSummary? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyReportViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<WeeklyReportUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            val db = container.database
            val repo = container.plannerRepository
            val dataFlow = combine(
                listOf(
                    todayFlow(),
                    container.prayerLogRepository.observeRange(profile.id, 0, Long.MAX_VALUE / 2),
                    db.adhkarDao().observeEntries(profile.id),
                    db.adhkarDao().observeAllProgress(profile.id),
                    db.worshipDao().observeLogs(profile.id),
                    db.quranDao().observeLogs(profile.id),
                    db.fastingDao().observeFastingDays(profile.id),
                    db.tasbeehDao().observeLogs(profile.id),
                    db.worshipDao().observePlannerActivityIds(profile.id)
                )
            ) { it }
            val currentPlanner = todayFlow().flatMapLatest { today ->
                repo.observeWeek(profile.id, PlannerWeek.weekStart(today).toEpochDay()).flatMapLatest { week ->
                    if (week == null) flowOf(0 to 0)
                    else repo.observeSlots(week.id).map { slots -> slots.count { it.doneAtMillis != 0L } to slots.size }
                }
            }
            val reports = repo.observeReports(profile.id, PlannerRepository.KIND_WEEK)
            combine(dataFlow, currentPlanner, reports) { v, planner, reps ->
                @Suppress("UNCHECKED_CAST")
                val input = YearLogInputs(
                    prayerLogs = v[1] as List<com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity>,
                    adhkarEntries = v[2] as List<com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity>,
                    adhkarProgress = v[3] as List<com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity>,
                    activityLogs = v[4] as List<com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity>,
                    quranLogs = v[5] as List<com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity>,
                    fastingDays = v[6] as List<com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity>,
                    tasbeehLogs = v[7] as List<com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity>
                )
                @Suppress("UNCHECKED_CAST")
                val plannerIds = (v[8] as List<Long>).toSet()
                val thisStart = PlannerWeek.weekStart(v[0] as LocalDate).toEpochDay()
                val lastStart = thisStart - 7
                val lastPlanner = plannerTotals(reps.firstOrNull { it.startEpochDay == lastStart })
                WeeklyReportUiState(
                    loaded = true,
                    thisWeek = WeeklySummary.compute(thisStart, input, plannerIds, planner.first, planner.second),
                    lastWeek = WeeklySummary.compute(lastStart, input, plannerIds, lastPlanner.first, lastPlanner.second)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyReportUiState())

    private fun plannerTotals(report: PlannerReportEntity?): Pair<Int, Int> {
        if (report == null) return 0 to 0
        val lines = runCatching { Json.decodeFromString<List<ReportLine>>(report.summaryJson) }.getOrDefault(emptyList())
        return lines.sumOf { it.done } to lines.sumOf { it.planned }
    }
}
