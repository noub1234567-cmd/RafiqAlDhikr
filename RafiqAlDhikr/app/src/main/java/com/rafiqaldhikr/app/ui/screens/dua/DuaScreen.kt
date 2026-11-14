package com.rafiqaldhikr.app.ui.screens.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.ui.components.EmptyStateCard
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

private val defaultCategories = listOf(
    "الكل", "أدعية عامة", "الوالدان والأهل", "الرزق والبركة", "العلم والهداية", "تفريج الكرب"
)

@Composable
fun DuaScreen(
    viewModel: DuaViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var editingDua by remember { mutableStateOf<DuaEntryEntity?>(null) }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "دفتر الأدعية الشخصي",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoritesFilter() }) {
                        Icon(
                            imageVector = if (state.favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (state.favoritesOnly) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة دعاء")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearch(it) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("ابحث في أدعيتك الخاصة...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(defaultCategories) { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.duas.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.VolunteerActivism,
                            title = "دفترك الخاص آمن ومحلي",
                            description = "أضف أدعيتك الشخصية ومناجاتك الخاصة؛ تبقى محفوظة محلياً داخل هذا الملف الشخصي فقط."
                        )
                    }
                } else {
                    items(state.duas, key = { it.id }) { dua ->
                        RafiqCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dua.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = dua.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = { viewModel.toggleFavorite(dua) }) {
                                        Icon(
                                            imageVector = if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "تفضيل",
                                            tint = if (dua.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { editingDua = dua }) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل")
                                    }
                                    IconButton(onClick = { viewModel.deleteDua(dua) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(dua.body, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }

    val dialogProfileId = state.activeProfile?.id
    if (dialogProfileId != null && (showAdd || editingDua != null)) {
        val profileId: Long = dialogProfileId
        DuaEditorDialog(
            profileId = profileId,
            existing = editingDua,
            onDismiss = {
                showAdd = false
                editingDua = null
            },
            onSave = {
                viewModel.saveDua(it)
                showAdd = false
                editingDua = null
            }
        )
    }
}

@Composable
private fun DuaEditorDialog(
    profileId: Long,
    existing: DuaEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (DuaEntryEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var body by remember { mutableStateOf(existing?.body ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "أدعية عامة") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إضافة دعاء خاص" else "تعديل الدعاء") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الدعاء") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("نص الدعاء") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && body.isNotBlank(),
                onClick = {
                    onSave(
                        (existing ?: DuaEntryEntity(
                            profileId = profileId,
                            title = title.trim(),
                            body = body.trim()
                        )).copy(
                            title = title.trim(),
                            body = body.trim(),
                            category = category.trim().ifBlank { "أدعية عامة" }
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
