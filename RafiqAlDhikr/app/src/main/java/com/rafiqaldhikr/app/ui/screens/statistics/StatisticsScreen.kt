package com.rafiqaldhikr.app.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val s = state.stats

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "الإحصائيات والمتابعة",
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                    Text(
                        text = "قليل دائم خير من كثير منقطع",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "هذه الأرقام تعينك على متابعة وردك برفق، وليست للمقارنة أو الضغط على النفس.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(
                        title = "الأيام المتتالية",
                        value = "${ArabicFormatters.toArabicDigits(s.consecutiveDays)} يوم",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "التسبيح اليوم",
                        value = ArabicFormatters.toArabicDigits(s.totalTasbeehToday),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(
                        title = "سلسلة الأذكار (صباح ومساء)",
                        value = "${ArabicFormatters.toArabicDigits(s.adhkarStreak)} يوم",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { WeeklySummaryCard(s) }

            item {
                RafiqCard {
                    Text("العبادات اليومية والأسبوعية والشهرية", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("اليوم — مكتمل / معلّق / فائت", "${ArabicFormatters.toArabicDigits(s.todayCompleted)} / ${ArabicFormatters.toArabicDigits(s.todayPending)} / ${ArabicFormatters.toArabicDigits(s.todayMissed)}")
                    StatRow("آخر ٧ أيام — مكتمل / فائت", "${ArabicFormatters.toArabicDigits(s.weekCompleted)} / ${ArabicFormatters.toArabicDigits(s.weekMissed)}")
                    StatRow("آخر ٣٠ يوماً — مكتمل / فائت", "${ArabicFormatters.toArabicDigits(s.monthCompleted)} / ${ArabicFormatters.toArabicDigits(s.monthMissed)}")
                }
            }

            item {
                RafiqCard {
                    Text("القرآن والحفظ والصيام", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("مجموع قراءة القرآن (٧ أيام)", ArabicFormatters.formatAmountAr(s.quranWeekTotal))
                    StatRow("مجموع قراءة القرآن (٣٠ يوماً)", ArabicFormatters.formatAmountAr(s.quranMonthTotal))
                    StatRow("جلسات الحفظ المسجلة", ArabicFormatters.toArabicDigits(s.memSessionsCount))
                    StatRow("جلسات المراجعة المسجلة", ArabicFormatters.toArabicDigits(s.revSessionsCount))
                    StatRow("أيام الصيام المكتملة", ArabicFormatters.toArabicDigits(s.completedFastingCount))
                }
            }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, modifier: Modifier = Modifier) {
    RafiqCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun WeeklySummaryCard(s: com.rafiqaldhikr.app.data.repository.ProfileStatisticsSnapshot) {
    val days = s.dailyCompleted.size
    val maxValue = (0 until days).maxOfOrNull { s.dailyCompleted[it] + s.dailyMissed[it] }?.coerceAtLeast(1) ?: 1
    val best = (0 until days).maxByOrNull { s.dailyCompleted[it] }?.takeIf { s.dailyCompleted[it] > 0 }
    fun dayName(i: Int) = java.time.LocalDate.ofEpochDay(s.windowStartEpochDay + i)
        .dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("ar"))

    RafiqCard {
        Text("ملخص الأسبوع", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (best != null) "أفضل أيامك: ${dayName(best)} (${ArabicFormatters.toArabicDigits(s.dailyCompleted[best])} مكتملة)"
            else "لا توجد عبادات مكتملة هذا الأسبوع بعد — ابدأ بواحدة صغيرة.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        for (i in 0 until days) {
            val done = s.dailyCompleted[i]
            val missed = s.dailyMissed[i]
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(dayName(i), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(64.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { done / maxValue.toFloat() },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${ArabicFormatters.toArabicDigits(done)} / ${ArabicFormatters.toArabicDigits(missed)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Text(
            "الرقم الأول مكتمل والثاني فائت.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
