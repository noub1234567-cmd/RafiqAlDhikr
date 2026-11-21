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
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MemoPlanScreen(planId: Long, onBack: () -> Unit, onOpenMemo: () -> Unit) {
    val container = rememberContainer()
    val repo = container.memoPlanRepository
    val plan by remember(planId) { repo.observePlan(planId) }.collectAsState(initial = null)
    val items by remember(planId) { repo.observeItems(planId) }.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val today = LocalDate.now().toEpochDay()
    val p = plan

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = p?.title ?: "خطة الحفظ", onBackClick = onBack)
        if (p == null) return@Column
        val undone = items.filter { it.doneEpochDay < 0 }
        val done = items.filter { it.doneEpochDay >= 0 }
        val ramp = p.ramp == 1
        val paused = p.pausedSessions >= 0
        val dayState = MemoPlanMath.state(p.startEpochDay, p.totalDays, p.studyDaysMask, today, paused)
        val elapsed = MemoPlanMath.sessionsElapsed(p.startEpochDay, p.studyDaysMask, today)
        val quota = MemoPlanMath.quotaToday(undone.size, p.totalDays, ramp, p.startEpochDay, p.studyDaysMask, today, paused)
        val todays = undone.take(quota)
        val endDay = MemoPlanMath.endDate(p.startEpochDay, p.totalDays, p.studyDaysMask)
        val todayWords = todays.sumOf { MemoPlanMath.words(it.text) }
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
                            when (dayState) {
                                MemoPlanMath.PAUSED -> "الخطة متوقفة مؤقتاً، وتبقّى ${p.pausedSessions} أيام حفظ تُستأنف من يوم الاستئناف"
                                MemoPlanMath.NOT_STARTED -> "تبدأ الخطة في ${dayLabel(p.startEpochDay)}"
                                MemoPlanMath.REST_DAY -> "اليوم يوم راحة، وتنتهي الخطة في ${dayLabel(endDay)}"
                                MemoPlanMath.OVER -> if (undone.isNotEmpty()) "تجاوزت المدة، والباقي كله اليوم" else "انتهت المدة"
                                else -> "يوم الحفظ ${elapsed + 1} من ${p.totalDays} — تنتهي في ${dayLabel(endDay)}"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (todays.isNotEmpty()) Text(
                            "الوقت التقريبي لحفظ اليوم: ${MemoPlanMath.newMinutes(todayWords)} دقيقة",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (learnedToday > 0) Text("حفظت اليوم: $learnedToday", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                var recalcOpen by remember { mutableStateOf(false) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { recalcOpen = true }, enabled = undone.isNotEmpty()) { Text("أعد الحساب") }
                    if (paused) Button(onClick = { scope.launch { repo.resume(p, today) } }) { Text("استأنف الخطة") }
                    else OutlinedButton(onClick = { scope.launch { repo.pause(p, today) } }, enabled = undone.isNotEmpty()) { Text("إيقاف مؤقت") }
                }
                if (recalcOpen) {
                    var daysText by remember { mutableStateOf((p.totalDays - elapsed).coerceAtLeast(1).toString()) }
                    var newMask by remember { mutableStateOf(p.studyDaysMask) }
                    val newDays = daysText.toIntOrNull()?.takeIf { it > 0 }
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { recalcOpen = false },
                        title = { Text("إعادة حساب الخطة") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("الباقي ${undone.size} نصاً، يُوزَّع من اليوم على أيام حفظ جديدة بورد متساوٍ.", style = MaterialTheme.typography.bodyMedium)
                                androidx.compose.material3.OutlinedTextField(
                                    value = daysText, onValueChange = { daysText = it.filter(Char::isDigit).take(4) },
                                    label = { Text("عدد أيام الحفظ من اليوم") }, singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                                StudyDayChips(newMask) { newMask = it }
                                if (newDays != null) {
                                    val end = MemoPlanMath.endDate(today, newDays, newMask)
                                    Text("الورد الجديد: حتى ${MemoPlanMath.perDayFor(undone.size, newDays)} يومياً، وتنتهي في ${dayLabel(end)}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        confirmButton = {
                            Button(enabled = newDays != null, onClick = {
                                scope.launch { repo.reschedule(p.id, today, newDays ?: 1, newMask) }
                                recalcOpen = false
                            }) { Text("أعد الحساب") }
                        },
                        dismissButton = { TextButton(onClick = { recalcOpen = false }) { Text("إلغاء") } }
                    )
                }
            }
            item {
                var minutes by remember(p.id) { mutableStateOf(com.rafiqaldhikr.app.core.memorization.MemoPlanReminders.minutes(context, p.id)) }
                var picking by remember { mutableStateOf(false) }
                RafiqCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (minutes >= 0) "تذكير يومي: ${com.rafiqaldhikr.app.core.memorization.MemoPlanReminders.label(minutes)}" else "لا يوجد تذكير يومي",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { picking = true }) { Text(if (minutes >= 0) "غيّر الوقت" else "حدد وقت التذكير") }
                            if (minutes >= 0) TextButton(onClick = {
                                com.rafiqaldhikr.app.core.memorization.MemoPlanReminders.clear(context, p.id)
                                container.reminderScheduler.cancelMemoPlanReminder(p.id)
                                minutes = -1
                            }) { Text("إلغاء التذكير") }
                        }
                    }
                }
                if (picking) {
                    val st = androidx.compose.material3.rememberTimePickerState(
                        initialHour = if (minutes >= 0) minutes / 60 else 7,
                        initialMinute = if (minutes >= 0) minutes % 60 else 0,
                        is24Hour = false
                    )
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { picking = false },
                        title = { Text("وقت التذكير اليومي") },
                        text = { androidx.compose.material3.TimePicker(state = st) },
                        confirmButton = {
                            Button(onClick = {
                                com.rafiqaldhikr.app.core.memorization.MemoPlanReminders.set(context, p.id, st.hour, st.minute)
                                container.reminderScheduler.scheduleMemoPlanReminder(p.id, p.profileId, p.title)
                                minutes = st.hour * 60 + st.minute
                                picking = false
                            }) { Text("حفظ") }
                        },
                        dismissButton = { TextButton(onClick = { picking = false }) { Text("إلغاء") } }
                    )
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
                        Button(onClick = { scope.launch {
                            repo.deletePlan(p.id)
                            com.rafiqaldhikr.app.core.memorization.MemoPlanReminders.clear(context, p.id)
                            container.reminderScheduler.cancelMemoPlanReminder(p.id)
                            onBack()
                        } }) { Text("نعم، احذف الخطة") }
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
            val firstHalf = com.rafiqaldhikr.app.core.dua.DuaLibrary.verseFirstHalf(item.title, item.text)
            if (shown) Text(item.text, style = MaterialTheme.typography.bodyLarge)
            else if (firstHalf != null) {
                Text(firstHalf, style = MaterialTheme.typography.bodyLarge)
                Text("أكمل الشطر الثاني ثم اضغط لإظهار البيت", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else Text("اضغط لإظهار النص بعد أن تستحضره", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
