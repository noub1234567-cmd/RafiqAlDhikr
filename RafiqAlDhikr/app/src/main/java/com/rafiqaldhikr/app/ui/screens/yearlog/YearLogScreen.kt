package com.rafiqaldhikr.app.ui.screens.yearlog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.yearlog.LogKind
import com.rafiqaldhikr.app.core.yearlog.YearLog
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate

private const val WEEKS = 53

@Composable
fun YearLogScreen(viewModel: YearLogViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var kind by remember { mutableStateOf(LogKind.PRAYER) }
    var selected by remember(kind) { mutableStateOf<LocalDate?>(null) }

    val levelsByKind = remember(state.inputs) { LogKind.entries.associateWith { YearLog.levels(it, state.inputs) } }
    val levels = levelsByKind.getValue(kind)
    val days = remember(levels, kind) { YearLog.qualifyingDays(kind, levels) }
    val todayEpoch = state.today.toEpochDay()

    Scaffold(topBar = { RafiqTopBar(title = "السجل السنوي", onBackClick = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LogKind.entries.forEach { k ->
                        FilterChip(selected = k == kind, onClick = { kind = k }, label = { Text(k.labelAr) })
                    }
                }
            }

            item {
                RafiqCard {
                    Heatmap(
                        today = state.today,
                        levels = levels,
                        selected = selected,
                        onSelect = { selected = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    val sel = selected
                    Text(
                        if (sel == null) "المس يوماً لعرض تفاصيله"
                        else "$sel — " + when (val l = levels[sel.toEpochDay()] ?: 0) {
                            0 -> "لا نشاط"
                            else -> "المستوى $l من ٤"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                RafiqCard {
                    val yearStart = todayEpoch - 364
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Stat("السلسلة الحالية", "${YearLog.currentStreak(days, todayEpoch)} يوماً")
                        Stat("أطول سلسلة", "${YearLog.longestStreak(days)} يوماً")
                        Stat("أيام هذه السنة", "${days.count { it in yearStart..todayEpoch }}")
                    }
                    if (kind == LogKind.PRAYER || kind == LogKind.ADHKAR) {
                        Spacer(Modifier.height(4.dp))
                        Text("اليوم يُحسب في السلسلة عند إتمام جميع ${if (kind == LogKind.PRAYER) "الفرائض الخمس" else "أذكار الصباح والمساء"}.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                RafiqCard {
                    Text("تحديات هذا الشهر", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    LogKind.entries.forEach { k ->
                        val c = YearLog.challenge(k, YearLog.qualifyingDays(k, levelsByKind.getValue(k)), state.today)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(k.labelAr)
                            Text(if (c.completed) "اكتمل ✓" else "${c.done} من ${c.target} يوماً")
                        }
                        LinearProgressIndicator(
                            progress = { (c.done.toFloat() / c.target).coerceAtMost(1f) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }
            }

            item {
                RafiqCard {
                    Text("الأوسمة", style = MaterialTheme.typography.titleMedium)
                    Text("تُمنح عند بلوغ سلسلة متواصلة من الأيام.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    LogKind.entries.forEach { k ->
                        val longest = YearLog.longestStreak(YearLog.qualifyingDays(k, levelsByKind.getValue(k)))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(k.labelAr, modifier = Modifier.width(72.dp))
                            YearLog.badges(k, longest).forEach { b ->
                                Text(
                                    text = (if (b.earned) "★ " else "☆ ") + b.days,
                                    color = if (b.earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Heatmap(today: LocalDate, levels: Map<Long, Int>, selected: LocalDate?, onSelect: (LocalDate) -> Unit) {
    val base = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    fun colorFor(level: Int): Color = when (level) {
        0 -> empty
        1 -> base.copy(alpha = 0.30f)
        2 -> base.copy(alpha = 0.55f)
        3 -> base.copy(alpha = 0.80f)
        else -> base
    }
    // First column starts on the Saturday of the week that began WEEKS-1 weeks before this one.
    val thisWeekStart = today.minusDays(YearLog.weekRow(today).toLong())
    val firstDay = thisWeekStart.minusWeeks((WEEKS - 1).toLong())
    val scroll = rememberScrollState()
    LaunchedEffect(Unit) { scroll.scrollTo(scroll.maxValue) }

    // Chronological left-to-right regardless of the UI direction, newest week at the right edge.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier = Modifier.horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            for (w in 0 until WEEKS) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (r in 0 until 7) {
                        val date = firstDay.plusDays((w * 7 + r).toLong())
                        if (date.isAfter(today)) {
                            Box(Modifier.size(14.dp))
                        } else {
                            val isSel = date == selected
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.tertiary else colorFor(levels[date.toEpochDay()] ?: 0),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .clickable { onSelect(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}
