package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun rememberContainer(): AppContainer = (LocalContext.current.applicationContext as RafiqApplication).container

/** The active profile's id, or null while loading or when there is none. */
@Composable
fun activeProfileIdState(container: AppContainer): State<Long?> {
    val profile = container.profileRepository.activeProfileFlow.collectAsState(initial = null)
    return androidx.compose.runtime.derivedStateOf { profile.value?.id }
}

/** Collects a per-profile list flow, empty until a profile is known. */
@Composable
fun <T> profileList(profileId: Long?, source: (Long) -> Flow<List<T>>): List<T> {
    val flow = androidx.compose.runtime.remember(profileId) { if (profileId == null) emptyFlow() else source(profileId) }
    val items by flow.collectAsState(initial = emptyList())
    return items
}
