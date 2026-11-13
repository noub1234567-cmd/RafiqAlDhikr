package com.rafiqaldhikr.app.ui.screens.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DuaUiState(
    val activeProfile: UserProfileEntity? = null,
    val duas: List<DuaEntryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "الكل",
    val favoritesOnly: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class DuaViewModel(private val container: AppContainer) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val categoryFlow = MutableStateFlow("الكل")
    private val favoritesOnlyFlow = MutableStateFlow(false)

    val uiState: StateFlow<DuaUiState> = container.profileRepository.activeProfileFlow
        .filterNotNull()
        .flatMapLatest { profile ->
            combine(
                container.duaRepository.observeDuas(profile.id),
                queryFlow,
                categoryFlow,
                favoritesOnlyFlow
            ) { allDuas, query, cat, favOnly ->
                val filtered = allDuas.filter { dua ->
                    val matchesQuery = query.isBlank() ||
                        dua.title.contains(query, ignoreCase = true) ||
                        dua.body.contains(query, ignoreCase = true) ||
                        dua.category.contains(query, ignoreCase = true)
                    val matchesCat = cat == "الكل" || dua.category == cat
                    val matchesFav = !favOnly || dua.isFavorite
                    matchesQuery && matchesCat && matchesFav
                }
                DuaUiState(
                    activeProfile = profile,
                    duas = filtered,
                    searchQuery = query,
                    selectedCategory = cat,
                    favoritesOnly = favOnly
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DuaUiState())

    fun setSearch(q: String) { queryFlow.value = q }
    fun setCategory(c: String) { categoryFlow.value = c }
    fun toggleFavoritesFilter() { favoritesOnlyFlow.value = !favoritesOnlyFlow.value }

    fun saveDua(dua: DuaEntryEntity) {
        viewModelScope.launch { container.duaRepository.saveDua(dua) }
    }

    fun toggleFavorite(dua: DuaEntryEntity) {
        viewModelScope.launch { container.duaRepository.toggleFavorite(dua) }
    }

    fun deleteDua(dua: DuaEntryEntity) {
        viewModelScope.launch { container.duaRepository.deleteDua(dua) }
    }
}
