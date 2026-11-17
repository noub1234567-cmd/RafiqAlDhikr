package com.rafiqaldhikr.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.rafiqaldhikr.app.di.AppContainer
import com.rafiqaldhikr.app.ui.screens.adhkar.AdhkarViewModel
import com.rafiqaldhikr.app.ui.screens.dashboard.DashboardViewModel
import com.rafiqaldhikr.app.ui.screens.dua.DuaViewModel
import com.rafiqaldhikr.app.ui.screens.fasting.FastingViewModel
import com.rafiqaldhikr.app.ui.screens.history.HistoryViewModel
import com.rafiqaldhikr.app.ui.screens.memorization.MemorizationViewModel
import com.rafiqaldhikr.app.ui.screens.prayer.PrayerTrackerViewModel
import com.rafiqaldhikr.app.ui.screens.profiles.ProfilesViewModel
import com.rafiqaldhikr.app.ui.screens.quran.QuranViewModel
import com.rafiqaldhikr.app.ui.screens.seasons.SeasonsViewModel
import com.rafiqaldhikr.app.ui.screens.settings.SettingsViewModel
import com.rafiqaldhikr.app.ui.screens.statistics.StatisticsViewModel
import com.rafiqaldhikr.app.ui.screens.tasbeeh.TasbeehViewModel
import com.rafiqaldhikr.app.ui.screens.worship.WorshipActsViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            DashboardViewModel::class.java -> DashboardViewModel(container)
            AdhkarViewModel::class.java -> AdhkarViewModel(container)
            QuranViewModel::class.java -> QuranViewModel(container)
            MemorizationViewModel::class.java -> MemorizationViewModel(container)
            FastingViewModel::class.java -> FastingViewModel(container)
            TasbeehViewModel::class.java -> TasbeehViewModel(container)
            DuaViewModel::class.java -> DuaViewModel(container)
            WorshipActsViewModel::class.java -> WorshipActsViewModel(container)
            SeasonsViewModel::class.java -> SeasonsViewModel(container)
            HistoryViewModel::class.java -> HistoryViewModel(container)
            StatisticsViewModel::class.java -> StatisticsViewModel(container)
            ProfilesViewModel::class.java -> ProfilesViewModel(container)
            PrayerTrackerViewModel::class.java -> PrayerTrackerViewModel(container)
            com.rafiqaldhikr.app.ui.screens.yearlog.YearLogViewModel::class.java ->
                com.rafiqaldhikr.app.ui.screens.yearlog.YearLogViewModel(container)
            com.rafiqaldhikr.app.ui.screens.ramadan.RamadanViewModel::class.java ->
                com.rafiqaldhikr.app.ui.screens.ramadan.RamadanViewModel(container)
            com.rafiqaldhikr.app.ui.screens.khatma.KhatmaViewModel::class.java ->
                com.rafiqaldhikr.app.ui.screens.khatma.KhatmaViewModel(container)
            com.rafiqaldhikr.app.ui.screens.planner.PlannerViewModel::class.java ->
                com.rafiqaldhikr.app.ui.screens.planner.PlannerViewModel(container)
            com.rafiqaldhikr.app.ui.screens.planner.PlannerReportsViewModel::class.java ->
                com.rafiqaldhikr.app.ui.screens.planner.PlannerReportsViewModel(container)
            SettingsViewModel::class.java -> SettingsViewModel(container)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}
