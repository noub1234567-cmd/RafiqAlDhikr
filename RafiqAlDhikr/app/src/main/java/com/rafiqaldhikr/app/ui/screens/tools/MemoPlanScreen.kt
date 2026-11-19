package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.memorization.MemoPlanMath
import com.rafiqaldhikr.app.data.local.db.entities.MemoPlanItemEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate
import kotlinx.coroutines.launch

/** One plan: today's items to learn, the review for today, and what is already learned. */
@Composable
fun MemoPlanScreen(planId: Long, onBack: () -> Unit, onOpenMemo: () -> Unit) {
    val container = rememberContainer()
    val repo = container.memoPlanRepository
    val plan by remember(planId) { repo.observePlan(planId) }.collectAsState(initial = null)
    val items by remember(planId) { repo.observeItems(planId) }.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toEpochDay()
    val p = plan

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = p?.title ?: "خطة الحفظ", onBackClick = onBack)
        if (p == null) return@Column
        val undone = items.filter { it.doneEpochDay < 0 }
        val done = items.filter { it.doneEpochDay >= 0 }
        val dayIdx = MemoPlanMath.dayIndex(p.startEpochDay, today)
        val quota = MemoPlanMath.quotaToday(undone.size, p.totalDays, dayIdx)
        val todays = undone.take(quota)
        val learnedToday = done.count { it.doneEpochDay == today }
        val reviewItems = if (p.reviewMode == MemoPlanMath.REVIEW_MANUAL) {
            val positions = MemoPlanMath.manualReview(done.filter { it.doneEpochDay < today }.map { it.position }, p.reviewCount)
            done.filter { it.position in positions }
        } else emptyList()
        var confirmDelete by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RafiqCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("حفظت ${done.size} من ${items.size}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "اليوم ${(dayIdx + 1).coerceAtMost(p.totalDays)} من ${p.totalDays}" +
                                if (dayIdx >= p.totalDays && undone.isNotEmpty()) " — تجاوزت المدة، والباقي كله اليوم" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (learnedToday > 0) Text("حفظت اليوم: $learnedToday", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            if (undone.isEmpty()) {
                item {
                    RafiqCard { Text("أتممت الخطة، بارك الله فيك. تابع المراجعة.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge) }
                }
            } else {
                item { Text("احفظ اليوم (${todays.size})", style = MaterialTheme.typography.titleMedium) }
                items(todays, key = { "t_${it.id}" }) { it ->
                    LearnCard(it) { scope.launch { repo.markDone(p, it, today) } }
                }
            }
            if (p.reviewMode == MemoPlanMath.REVIEW_AUTO) {
                item {
                    RafiqCard(onClick = onOpenMemo) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("المراجعة تلقائية", style = MaterialTheme.typography.titleSmall)
                            Text("ما تحفظه يدخل في «حفظ الأذكار والأحاديث». اضغط لمراجعة المستحق اليوم.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else if (reviewItems.isNotEmpty()) {
                item { Text("راجع اليوم (${reviewItems.size})", style = MaterialTheme.typography.titleMedium) }
                items(reviewItems, key = { "r_${it.id}" }) { ReviewCard(it) }
            }
            if (done.isNotEmpty()) {
                item { Text("ما حفظته", style = MaterialTheme.typography.titleMedium) }
                items(done.sortedByDescending { it.position }, key = { "d_${it.id}" }) { it ->
                    RafiqCard {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(top = 12.dp))
                            TextButton(onClick = { scope.launch { repo.undo(it) } }) { Text("تراجع") }
                        }
                    }
                }
            }
            item {
                if (confirmDelete) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { scope.launch { repo.deletePlan(p.id); onBack() } }) { Text("نعم، احذف الخطة") }
                        OutlinedButton(onClick = { confirmDelete = false }) { Text("إلغاء") }
                    }
                } else {
                    TextButton(onClick = { confirmDelete = true }) { Text("حذف الخطة") }
                }
            }
        }
    }
}

@Composable
private fun LearnCard(item: MemoPlanItemEntity, onDone: () -> Unit) {
    RafiqCard {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (item.title.isNotBlank()) Text(item.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(item.text, style = MaterialTheme.typography.bodyLarge)
            if (item.source.isNotBlank()) Text(item.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Button(onClick = onDone) { Text("حفظته") }
        }
    }
}

@Composable
private fun ReviewCard(item: MemoPlanItemEntity) {
    var shown by remember(item.id) { mutableStateOf(false) }
    RafiqCard(onClick = { shown = !shown }) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall)
            if (shown) Text(item.text, style = MaterialTheme.typography.bodyLarge)
            else Text("اضغط لإظهار النص بعد أن تستحضره", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
