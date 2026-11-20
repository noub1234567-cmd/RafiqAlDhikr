package com.rafiqaldhikr.app.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.screens.statistics.StatisticsScreen
import com.rafiqaldhikr.app.ui.screens.statistics.StatisticsViewModel

/** "المتابعة": the statistics and the history of completed / missed worship in one screen with two tabs. */
@Composable
fun ProgressScreen(
    historyViewModel: HistoryViewModel,
    statisticsViewModel: StatisticsViewModel,
    initialTab: Int,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "المتابعة والسجل", onBackClick = onBack)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("الإحصائيات") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("السجل") })
        }
        if (tab == 0) StatisticsScreen(viewModel = statisticsViewModel, onBack = onBack, embedded = true)
        else HistoryScreen(viewModel = historyViewModel, onBack = onBack, embedded = true)
    }
}
