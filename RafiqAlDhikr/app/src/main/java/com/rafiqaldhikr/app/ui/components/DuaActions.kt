package com.rafiqaldhikr.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.core.share.ShareCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** What gets copied: the text, then its source on a new line when there is one. */
fun duaClipboardText(text: String, source: String): String =
    if (source.isBlank()) text else "$text\n$source"

/**
 * The two buttons shown under every dhikr or du'a in the app: copy it, or add it to the
 * personal notebook ("أدعيتي") of the active profile. [saveTitle] and [saveCategory] describe the new entry.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuaActionsRow(
    text: String,
    source: String = "",
    saveTitle: String,
    saveCategory: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val tools = (context.applicationContext as RafiqApplication).container.personalToolsRepository
    var favorite by remember(text) { mutableStateOf(false) }
    LaunchedEffect(text) {
        val pid = (context.applicationContext as RafiqApplication).container.profileRepository.activeProfileFlow.first()?.id
        favorite = pid != null && tools.isFavorite(pid, text)
    }

    FlowRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        IconButton(onClick = {
            scope.launch {
                val pid = (context.applicationContext as RafiqApplication).container.profileRepository.activeProfileFlow.first()?.id
                if (pid == null) {
                    Toast.makeText(context, "لا يوجد ملف شخصي نشط", Toast.LENGTH_SHORT).show()
                } else {
                    favorite = tools.toggleFavorite(pid, saveTitle, text, source)
                    Toast.makeText(context, if (favorite) "أُضيف إلى المفضلة" else "أُزيل من المفضلة", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Icon(
                if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "المفضلة",
                tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = {
            clipboard.setText(AnnotatedString(duaClipboardText(text, source)))
            Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
        }) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("نسخ", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = {
            scope.launch {
                val container = (context.applicationContext as RafiqApplication).container
                val profileId = container.profileRepository.activeProfileFlow.first()?.id
                val message = if (profileId == null) {
                    "لا يوجد ملف شخصي نشط"
                } else if (container.duaRepository.addIfAbsent(profileId, saveTitle, duaClipboardText(text, source), saveCategory)) {
                    "أُضيف إلى أدعيتي"
                } else {
                    "موجود بالفعل في أدعيتي"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("إلى أدعيتي", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = {
            scope.launch {
                val chooser = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    ShareCard.buildChooser(context, text, source)
                }
                runCatching { context.startActivity(chooser) }
            }
        }) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("صورة", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = {
            scope.launch {
                val pid = (context.applicationContext as RafiqApplication).container.profileRepository.activeProfileFlow.first()?.id
                val message = if (pid == null) {
                    "لا يوجد ملف شخصي نشط"
                } else if (tools.addMemo(pid, saveTitle, text, source, java.time.LocalDate.now().toEpochDay())) {
                    "أُضيف إلى حفظ الأدعية"
                } else {
                    "موجود بالفعل في حفظ الأدعية"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("حفظ", style = MaterialTheme.typography.labelLarge)
        }
    }
}
