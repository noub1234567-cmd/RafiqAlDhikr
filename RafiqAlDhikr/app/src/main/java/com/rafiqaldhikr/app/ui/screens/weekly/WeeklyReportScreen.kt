package com.rafiqaldhikr.app.ui.screens.weekly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.stats.WeekSummary
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

private fun n(v: Int) = ArabicFormatters.toArabicDigits(v)

@Composable
fun WeeklyReportScreen(viewModel: WeeklyReportViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cur = state.thisWeek
    val last = state.lastWeek

    Scaffold(topBar = { RafiqTopBar(title = "تقرير الأسبوع", onBackClick = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (cur == null || last == null) return@LazyColumn
            item {
                RafiqCard(ornament = true) {
                    Text("ملخص أسبوعك", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "الأسبوع من السبت إلى الجمعة. الأسبوع الحالي يُحسب حتى اليوم.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                RafiqCard {
                    HeaderRow()
                    Rows(cur, last).forEach { (label, a, b) ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(a, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, modifier = Modifier.width(84.dp))
                            Text(b, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(84.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Spacer(Modifier.weight(1f))
        Text("هذا الأسبوع", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(84.dp))
        Text("الأسبوع الماضي", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center,
            modifier = Modifier.width(84.dp))
    }
}

private fun Rows(c: WeekSummary, l: WeekSummary): List<Triple<String, String, String>> = listOf(
    Triple("الصلوات المفروضة", "${n(c.fardPrayers)}/${n(WeekSummary.FARD_PER_WEEK)}", "${n(l.fardPrayers)}/${n(WeekSummary.FARD_PER_WEEK)}"),
    Triple("السنن والنوافل", n(c.sunnahPrayers), n(l.sunnahPrayers)),
    Triple("أيام اكتمال أذكار الصباح والمساء", "${n(c.adhkarDays)}/${n(7)}", "${n(l.adhkarDays)}/${n(7)}"),
    Triple("أيام قراءة القرآن", "${n(c.quranDays)}/${n(7)}", "${n(l.quranDays)}/${n(7)}"),
    Triple("أيام الصيام", n(c.fastingDays), n(l.fastingDays)),
    Triple("التسبيح", n(c.tasbeehTotal), n(l.tasbeehTotal)),
    Triple("العبادات المنجزة", n(c.activitiesDone), n(l.activitiesDone)),
    Triple("العبادات الفائتة", n(c.activitiesMissed), n(l.activitiesMissed)),
    Triple("الجدول الأسبوعي (تمّت/المخطّط)", "${n(c.plannerDone)}/${n(c.plannerPlanned)}", "${n(l.plannerDone)}/${n(l.plannerPlanned)}")
)
