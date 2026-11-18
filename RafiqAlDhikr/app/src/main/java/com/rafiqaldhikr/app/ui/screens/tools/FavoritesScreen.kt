package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.ui.components.DuaActionsRow
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

/** Everything starred anywhere in the app. Un-starring here removes it from the list. */
@Composable
fun FavoritesScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val profileId by activeProfileIdState(container)
    val favorites = profileList(profileId) { container.personalToolsRepository.observeFavorites(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "مفضلتي", onBackClick = onBack)
        if (favorites.isEmpty()) {
            RafiqCard(modifier = Modifier.padding(16.dp)) {
                Text(
                    "لم تضف شيئاً بعد. اضغط على النجمة تحت أي ذكر أو دعاء في التطبيق ليظهر هنا.",
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
            items(favorites, key = { it.id }) { f ->
                RafiqCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (f.title.isNotBlank()) Text(f.title, style = MaterialTheme.typography.titleSmall)
                        Text(f.text, style = MaterialTheme.typography.bodyLarge)
                        if (f.source.isNotBlank()) {
                            Text(f.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        DuaActionsRow(text = f.text, source = f.source, saveTitle = f.title.ifBlank { "دعاء" }, saveCategory = "من المفضلة")
                    }
                }
            }
        }
    }
}
