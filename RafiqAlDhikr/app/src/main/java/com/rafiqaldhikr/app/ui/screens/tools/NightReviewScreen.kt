package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate
import kotlinx.coroutines.launch

/** محاسبة النفس: three short questions before sleep, kept privately on this device. */
@Composable
fun NightReviewScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val repo = container.personalToolsRepository
    val profileId by activeProfileIdState(container)
    val reviews = profileList(profileId) { repo.observeReviews(it) }
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toEpochDay()

    var shortfall by remember { mutableStateOf("") }
    var good by remember { mutableStateOf("") }
    var forgiveness by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(profileId) {
        val pid = profileId ?: return@LaunchedEffect
        repo.getReview(pid, today)?.let {
            shortfall = it.shortfall
            good = it.good
            forgiveness = it.forgiveness
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "محاسبة النفس", onBackClick = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RafiqCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("قبل أن تنام: ثلاثة أسئلة قصيرة", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "تُحفظ إجاباتك على جهازك فقط ولا تُرسل إلى أي مكان.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = shortfall, onValueChange = { shortfall = it; saved = false },
                            label = { Text("فيمَ قصّرتُ اليوم؟") }, modifier = Modifier.fillMaxWidth(), minLines = 2
                        )
                        OutlinedTextField(
                            value = good, onValueChange = { good = it; saved = false },
                            label = { Text("ماذا أحسنتُ اليوم؟") }, modifier = Modifier.fillMaxWidth(), minLines = 2
                        )
                        OutlinedTextField(
                            value = forgiveness, onValueChange = { forgiveness = it; saved = false },
                            label = { Text("ما الذي أستغفر الله منه؟") }, modifier = Modifier.fillMaxWidth(), minLines = 2
                        )
                        Button(
                            enabled = profileId != null,
                            onClick = {
                                val pid = profileId ?: return@Button
                                scope.launch {
                                    repo.saveReview(pid, today, shortfall, good, forgiveness)
                                    saved = true
                                }
                            }
                        ) { Text(if (saved) "تم الحفظ" else "حفظ") }
                    }
                }
            }
            if (reviews.isNotEmpty()) {
                item { Text("ما سبق", style = MaterialTheme.typography.titleMedium) }
            }
            items(reviews, key = { it.id }) { r ->
                RafiqCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(LocalDate.ofEpochDay(r.dateEpochDay).toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        if (r.shortfall.isNotBlank()) Answer("قصّرتُ في", r.shortfall)
                        if (r.good.isNotBlank()) Answer("أحسنتُ في", r.good)
                        if (r.forgiveness.isNotBlank()) Answer("أستغفر الله من", r.forgiveness)
                        TextButton(onClick = { scope.launch { repo.deleteReview(r.id) } }) { Text("حذف") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Answer(label: String, value: String) {
    Spacer(Modifier.height(4.dp))
    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyMedium)
}
