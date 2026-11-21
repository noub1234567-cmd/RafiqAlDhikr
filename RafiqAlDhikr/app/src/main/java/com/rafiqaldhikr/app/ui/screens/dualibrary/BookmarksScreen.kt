package com.rafiqaldhikr.app.ui.screens.dualibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.ReadingPrefs
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.screens.tools.activeProfileIdState
import com.rafiqaldhikr.app.ui.screens.tools.profileList
import com.rafiqaldhikr.app.ui.screens.tools.rememberContainer
import kotlinx.coroutines.launch

/** The marked spots in library sections; "افتح" jumps to the spot inside its section. */
@Composable
fun BookmarksScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val container = rememberContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileId by activeProfileIdState(container)
    val marks = profileList(profileId) { container.personalToolsRepository.observeBookmarks(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "إشاراتي المرجعية", onBackClick = onBack)
        if (marks.isEmpty()) {
            RafiqCard(modifier = Modifier.padding(16.dp)) {
                Text(
                    "اضغط على أيقونة الإشارة في أعلى أي نص داخل أقسام الذكر والعلم ليظهر هنا، ثم افتحه لتعود إلى موضعه.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(marks, key = { it.id }) { m ->
                RafiqCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(m.sectionTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        if (m.title.isNotBlank()) Text(m.title, style = MaterialTheme.typography.titleSmall)
                        Text(m.text, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                ReadingPrefs.savePosition(context, m.sectionId, m.locator)
                                onOpen(m.sectionId)
                            }) { Text("افتح") }
                            TextButton(onClick = { scope.launch { container.personalToolsRepository.deleteBookmark(m.id) } }) { Text("أزل") }
                        }
                    }
                }
            }
        }
    }
}
