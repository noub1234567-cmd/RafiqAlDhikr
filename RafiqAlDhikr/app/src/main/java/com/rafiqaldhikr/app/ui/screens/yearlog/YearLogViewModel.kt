package com.rafiqaldhikr.app.ui.screens.yearlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.yearlog.YearLogInputs
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class YearLogUiState(
    val loaded: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val inputs: YearLogInputs = YearLogInputs()
)

@OptIn(ExperimentalCoroutinesApi::class)
class YearLogViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<YearLogUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            val db = container.database
            val today = todayFlow()
            combine(
                listOf(
                    today,
                    container.prayerLogRepository.observeRange(profile.id, 0, Long.MAX_VALUE / 2),
                    db.adhkarDao().observeEntries(profile.id),
                    db.adhkarDao().observeAllProgress(profile.id),
                    db.worshipDao().observeLogs(profile.id),
                    db.quranDao().observeLogs(profile.id),
                    db.fastingDao().observeFastingDays(profile.id),
                    db.tasbeehDao().observeLogs(profile.id)
                )
            ) { v ->
                @Suppress("UNCHECKED_CAST")
                YearLogUiState(
                    loaded = true,
                    today = v[0] as LocalDate,
                    inputs = YearLogInputs(
                        prayerLogs = v[1] as List<com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity>,
                        adhkarEntries = v[2] as List<com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity>,
                        adhkarProgress = v[3] as List<com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity>,
                        activityLogs = v[4] as List<com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity>,
                        quranLogs = v[5] as List<com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity>,
                        fastingDays = v[6] as List<com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity>,
                        tasbeehLogs = v[7] as List<com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity>
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), YearLogUiState())
}
