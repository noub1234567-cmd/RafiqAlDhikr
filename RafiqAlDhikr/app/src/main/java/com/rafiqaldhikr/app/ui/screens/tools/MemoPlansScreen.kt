package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.memorization.MemoPlanMath
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate

/** "خطط الحفظ": every plan with its progress and today's quota. */
@Composable
fun MemoPlansScreen(onBack: () -> Unit, onNew: () -> Unit, onOpen: (Long) -> Unit, onOpenQuran: () -> Unit) {
    val container = rememberContainer()
    val repo = container.memoPlanRepository
    val profileId by activeProfileIdState(container)
    val plans = profileList(profileId) { repo.observePlans(it) }
    val items = profileList(profileId) { repo.observeAllItems(it) }
    val quranPlans = profileList(profileId) { container.memorizationRepository.observePlans(it) }
    val today = LocalDate.now().toEpochDay()

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "خطط الحفظ", onBackClick = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Text("خطة جديدة") }
            }
            if (quranPlans.isNotEmpty()) {
                item { Text("خطط حفظ القرآن", style = MaterialTheme.typography.titleMedium) }
                items(quranPlans, key = { "q_${it.id}" }) { q ->
                    val total = (q.endAyah - q.startAyah + 1).coerceAtLeast(1)
                    val learned = (q.currentAyah - q.startAyah).coerceIn(0, total)
                    RafiqCard(onClick = onOpenQuran) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(q.title, style = MaterialTheme.typography.titleMedium)
                            Text("حفظت $learned من $total آية", style = MaterialTheme.typography.bodyMedium)
                            Text("كل جلسة: ${q.versesPerSession} آيات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item { Text("خطط الأذكار والأحاديث", style = MaterialTheme.typography.titleMedium) }
            }
            if (plans.isEmpty()) {
                item {
                    RafiqCard {
                        Text(
                            "اختر قسماً مثل الأربعين النووية وعدد الأيام، ويقسّم التطبيق ما تحفظه كل يوم ويرتب مراجعته.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            items(plans, key = { it.id }) { plan ->
                val mine = items.filter { it.planId == plan.id }
                val done = mine.count { it.doneEpochDay >= 0 }
                val remaining = mine.size - done
                val quota = MemoPlanMath.quotaToday(remaining, plan.totalDays, plan.ramp == 1, plan.startEpochDay, plan.studyDaysMask, today, plan.pausedSessions >= 0)
                val dayState = MemoPlanMath.state(plan.startEpochDay, plan.totalDays, plan.studyDaysMask, today, plan.pausedSessions >= 0)
                RafiqCard(onClick = { onOpen(plan.id) }) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(plan.title, style = MaterialTheme.typography.titleMedium)
                        Text("حفظت $done من ${mine.size}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (mine.isNotEmpty() && done >= mine.size) "أتممت الخطة"
                            else when (dayState) {
                                MemoPlanMath.PAUSED -> "متوقفة مؤقتاً"
                                MemoPlanMath.NOT_STARTED -> "تبدأ في ${dayLabel(plan.startEpochDay)}"
                                MemoPlanMath.REST_DAY -> "اليوم يوم راحة"
                                else -> "المطلوب اليوم: $quota"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
