package com.rafiqaldhikr.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.GoalPeriod
import com.rafiqaldhikr.app.core.model.VerifiedAdhkarCatalog
import com.rafiqaldhikr.app.data.local.datastore.DashboardConfig
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import com.rafiqaldhikr.app.di.AppContainer
import com.rafiqaldhikr.app.notifications.MissedActivityEvaluator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.rafiqaldhikr.app.core.calendar.todayFlow
import java.time.LocalDate

data class DashboardUiState(
    val activeProfile: UserProfileEntity? = null,
    val greeting: String = ArabicFormatters.greetingForHour(),
    val gregorianDateAr: String = "",
    val hijriDateAr: String = "",
    val todayLogs: List<ActivityLogEntity> = emptyList(),
    val upcomingReminders: List<Pair<WorshipActivityEntity, Long>> = emptyList(),
    val dailySuggestion: String = VerifiedAdhkarCatalog.dailySuggestions.first(),
    val weeklyCompleted: Int = 0,
    val weeklyTarget: Int = 7,
    val adhkarDoneToday: Int = 0,
    val adhkarTotalCount: Int = 0,
    val quranDoneToday: Double = 0.0,
    val quranTargetToday: Double = 4.0,
    val quranUnitAr: String = "صفحة",
    val config: DashboardConfig = DashboardConfig()
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now()

    val uiState: StateFlow<DashboardUiState> = combine(
        container.profileRepository.activeProfileFlow.filterNotNull(),
        container.preferences.settingsFlow,
        todayFlow()
    ) { profile, settings, day -> Triple(profile, settings, day) }
        .flatMapLatest { (profile, settings, today) ->
            viewModelScope.launch {
                container.worshipRepository.ensureTodayLogsAndEvaluate(profile.id, today)
            }
            val baseFlow = combine(
                container.worshipRepository.observeActivities(profile.id),
                container.worshipRepository.observeLogsForDay(profile.id, today),
                container.worshipRepository.observeLogs(profile.id),
                container.preferences.observeDashboardConfig(profile.id)
            ) { activities, todayLogs, allLogs, config ->
                Quad(activities, todayLogs, allLogs, config)
            }
            val extraFlow = combine(
                container.adhkarRepository.observeEntries(profile.id),
                container.adhkarRepository.observeProgressForDay(profile.id, today),
                container.quranRepository.observeGoal(profile.id),
                container.quranRepository.observeLogs(profile.id)
            ) { adhkarEntries, adhkarProgress, quranGoal, quranLogs ->
                Quad(adhkarEntries, adhkarProgress, quranGoal, quranLogs)
            }

            combine(baseFlow, extraFlow) { base, extra ->
                val (activities, todayLogs, allLogs, config) = base
                val (adhkarEntries, adhkarProgress, quranGoal, quranLogs) = extra

                val now = System.currentTimeMillis()
                val upcoming = activities.mapNotNull { act ->
                    val next = MissedActivityEvaluator.computeNextTriggerMillis(act, now)
                    if (next != null) act to next else null
                }.sortedBy { it.second }.take(4)

                val weekStart = today.minusDays(6).toEpochDay()
                val weeklyLogs = allLogs.filter { it.dateEpochDay in weekStart..today.toEpochDay() }
                val weeklyDone = weeklyLogs.count { it.status == ActivityStatus.COMPLETED }
                val weeklyGoalActs = activities.count { it.isEnabled && it.goalPeriod == GoalPeriod.WEEKLY }
                val weeklyTarget = (weeklyLogs.size.coerceAtLeast(weeklyGoalActs)).coerceAtLeast(1)

                val suggestionIndex = (today.dayOfYear % VerifiedAdhkarCatalog.dailySuggestions.size)
                val quranToday = quranLogs.filter { it.dateEpochDay == today.toEpochDay() }.sumOf { it.amountRead }
                val unit = if (quranGoal?.goalType?.name == "CUSTOM") {
                    quranGoal.customUnitLabel
                } else {
                    quranGoal?.goalType?.unitAr ?: "صفحة"
                }

                DashboardUiState(
                    activeProfile = profile,
                    greeting = ArabicFormatters.greetingForHour(),
                    gregorianDateAr = ArabicFormatters.formatGregorianDate(today),
                    hijriDateAr = HijriCalendarHelper.toHijri(today, settings.hijriOffsetDays).formattedArabic(),
                    todayLogs = todayLogs,
                    upcomingReminders = upcoming,
                    dailySuggestion = VerifiedAdhkarCatalog.dailySuggestions[suggestionIndex],
                    weeklyCompleted = weeklyDone,
                    weeklyTarget = weeklyTarget,
                    adhkarDoneToday = adhkarProgress.count { it.isCompleted },
                    adhkarTotalCount = adhkarEntries.size,
                    quranDoneToday = quranToday,
                    quranTargetToday = quranGoal?.targetAmount ?: 4.0,
                    quranUnitAr = unit,
                    config = config
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun markCompleted(log: ActivityLogEntity) {
        viewModelScope.launch {
            container.worshipRepository.markLogCompleted(log)
        }
    }

    fun revertLog(log: ActivityLogEntity) {
        viewModelScope.launch {
            container.worshipRepository.revertLogToPendingOrMissed(log)
        }
    }

    fun rescheduleLog(log: ActivityLogEntity, newMillis: Long) {
        viewModelScope.launch {
            container.worshipRepository.rescheduleActivityReminder(log, newMillis)
        }
    }

    fun updateDashboardConfig(config: DashboardConfig) {
        val profileId = uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            container.preferences.updateDashboardConfig(profileId, config)
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
