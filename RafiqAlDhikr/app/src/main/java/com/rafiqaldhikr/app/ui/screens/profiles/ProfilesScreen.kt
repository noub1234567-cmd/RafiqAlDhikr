package com.rafiqaldhikr.app.ui.screens.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

private val avatarColors = listOf(0xFF1B5E4A, 0xFF9E7022, 0xFF336669, 0xFF5D4E8C, 0xFF8C4A32)

@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<UserProfileEntity?>(null) }
    var deletingProfile by remember { mutableStateOf<UserProfileEntity?>(null) }

    Scaffold(
        topBar = {
            RafiqTopBar(title = "إدارة الملفات الشخصية", onBackClick = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "ملف شخصي جديد")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)) {
                    Text(
                        text = "يمكن لأكثر من مستخدم استخدام التطبيق على نفس الهاتف؛ ولكل ملف شخصي أذكاره وأهدافه وتذكيراته وسجلاته المستقلة تماماً دون الحاجة لحساب أو اتصال بالإنترنت.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(state.profiles, key = { it.id }) { profile ->
                val isActive = profile.id == state.activeProfileId
                RafiqCard(
                    containerColor = if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(profile.avatarColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium)
                            if (profile.note.isNotBlank()) {
                                Text(
                                    profile.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isActive) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "نشط",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isActive) {
                            Button(onClick = { viewModel.switchProfile(profile.id) }) {
                                Text("تفعيل هذا الملف")
                            }
                        } else {
                            Text("الملف النشط حالياً", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Row {
                            IconButton(onClick = { editingProfile = profile }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                            }
                            if (state.profiles.size > 1) {
                                IconButton(onClick = { deletingProfile = profile }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate || editingProfile != null) {
        ProfileEditorDialog(
            existing = editingProfile,
            onDismiss = {
                showCreate = false
                editingProfile = null
            },
            onSave = { name, color, note ->
                val target = editingProfile
                if (target == null) {
                    viewModel.createProfile(name, color, note)
                } else {
                    viewModel.updateProfile(target.copy(name = name, avatarColorHex = color, note = note))
                }
                showCreate = false
                editingProfile = null
            }
        )
    }

    deletingProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { deletingProfile = null },
            title = { Text("تأكيد حذف الملف الشخصي") },
            text = { Text("سيتم حذف الملف الشخصي «${profile.name}» وجميع سجلاته وأهدافه المحلية بشكل نهائي. هل أنت متأكد؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteProfile(profile)
                    deletingProfile = null
                }) { Text("نعم، حذف") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingProfile = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun ProfileEditorDialog(
    existing: UserProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var selectedColor by remember { mutableLongStateOf(existing?.avatarColorHex ?: avatarColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إنشاء ملف شخصي" else "تعديل الملف الشخصي") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("وصف مختصر (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("لون الشعار", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    avatarColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .then(
                                    if (selectedColor == c) {
                                        Modifier.border(2.5.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedColor = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), selectedColor, note.trim()) }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
