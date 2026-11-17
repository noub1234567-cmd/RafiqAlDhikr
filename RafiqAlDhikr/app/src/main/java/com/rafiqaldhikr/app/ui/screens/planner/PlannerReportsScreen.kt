package com.rafiqaldhikr.app.ui.screens.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.planner.PlannerType
import com.rafiqaldhikr.app.ui.components.ConfirmHost
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.rememberConfirm
import java.time.LocalDate

private fun dateText(epochDay: Long): String {
    val d = LocalDate.ofEpochDay(epochDay)
    return ArabicFormatters.toArabicDigits("${d.dayOfMonth}/${d.monthValue}/${d.year}")
}

@Composable
fun PlannerReportsScreen(viewModel: PlannerReportsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val confirm = rememberConfirm()
    ConfirmHost(confirm)

    Scaffold(topBar = { RafiqTopBar(title = "تقارير الجدول الأسبوعي", onBackClick = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.current?.let { cur ->
                item { ReportCard("هذا الأسبوع (جارٍ)", cur, onDelete = null) }
            }
            if (state.reports.isEmpty()) {
                item {
                    RafiqCard {
                        Text(
                            "لا توجد تقارير محفوظة بعد. يُحفظ تقرير تلقائياً عند انتهاء كل أسبوع، ولا يُحذف إلا بيدك.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(state.reports, key = { it.id }) { r ->
                ReportCard(
                    "${dateText(r.startEpochDay)} – ${dateText(r.endEpochDay)}",
                    r,
                    onDelete = { confirm.ask("سيُحذف هذا التقرير نهائياً. هل تريد المتابعة؟") { viewModel.delete(r.id) } }
                )
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, item: ReportItem, onDelete: (() -> Unit)?) {
    RafiqCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "المخطَّط: ${ArabicFormatters.toArabicDigits(item.planned)} · المنجَز: ${ArabicFormatters.toArabicDigits(item.done)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        if (item.lines.isEmpty()) {
            Text("لا توجد عبادات.", style = MaterialTheme.typography.bodyMedium)
        }
        item.lines.forEach { l ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(
                    "${l.title} (${PlannerType.fromKey(l.type).labelAr})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${ArabicFormatters.toArabicDigits(l.done)}/${ArabicFormatters.toArabicDigits(l.planned)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (onDelete != null) {
            TextButton(onClick = onDelete) { Text("حذف التقرير") }
        }
    }
}
