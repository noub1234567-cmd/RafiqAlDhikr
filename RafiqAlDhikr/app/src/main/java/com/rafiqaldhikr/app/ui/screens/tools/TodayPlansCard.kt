package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.memorization.MemoPlanMath
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.navigation.NavRoute
import java.time.LocalDate

/** Home card: what each memorization plan asks for today, with a shortcut into the plan. */
@Composable
fun TodayPlansCard(onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    val profileId by activeProfileIdState(container)
    val plans = profileList(profileId) { container.memoPlanRepository.observePlans(it) }
    val items = profileList(profileId) { container.memoPlanRepository.observeAllItems(it) }
    if (plans.isEmpty()) return
    val today = LocalDate.now().toEpochDay()
    RafiqCard(onClick = { onNavigate(NavRoute.MemoPlans.route) }) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("خطة اليوم", style = MaterialTheme.typography.titleMedium)
            plans.take(3).forEach { plan ->
                val mine = items.filter { it.planId == plan.id }
                val remaining = mine.count { it.doneEpochDay < 0 }
                val paused = plan.pausedSessions >= 0
                val quota = MemoPlanMath.quotaToday(remaining, plan.totalDays, plan.ramp == 1, plan.startEpochDay, plan.studyDaysMask, today, paused)
                val state = MemoPlanMath.state(plan.startEpochDay, plan.totalDays, plan.studyDaysMask, today, paused)
                val label = when {
                    mine.isNotEmpty() && remaining == 0 -> "أتممت الخطة"
                    state == MemoPlanMath.PAUSED -> "متوقفة مؤقتاً"
                    state == MemoPlanMath.NOT_STARTED -> "لم تبدأ بعد"
                    state == MemoPlanMath.REST_DAY -> "يوم راحة"
                    else -> "المطلوب اليوم: $quota"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plan.title, style = MaterialTheme.typography.bodyMedium)
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (quota > 0) TextButton(onClick = { onNavigate(NavRoute.MemoPlan.create(plan.id)) }) { Text("احفظ الآن") }
                }
            }
        }
    }
}
