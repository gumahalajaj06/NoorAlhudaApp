package com.example.features.duas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.DuaCategory
import com.example.core.data.DuaItem
import com.example.core.data.DuasCatalog
import com.example.core.database.AppDatabase
import com.example.core.database.FavoriteZikrEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DuasUiState(
    val categories: List<DuaCategory> = DuasCatalog.categories,
    val selectedCategory: DuaCategory = DuasCatalog.categories.first(),
    val items: List<DuaItem> = DuasCatalog.duas.filter { it.categoryId == "quran" },
    val favoriteIds: Set<String> = emptySet()
)

class DuasViewModel(private val db: AppDatabase) : ViewModel() {
    private val favoritesDao = db.favoritesDao()

    private val _uiState = MutableStateFlow(DuasUiState())
    val uiState: StateFlow<DuasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesDao.observeFavoriteAzkar().collect { favs ->
                _uiState.update { it.copy(favoriteIds = favs.map { f -> f.id }.toSet()) }
            }
        }
    }

    fun selectCategory(category: DuaCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                items = DuasCatalog.duas.filter { dua -> dua.categoryId == category.id }
            )
        }
    }

    fun toggleFavorite(dua: DuaItem) {
        val isFav = _uiState.value.favoriteIds.contains(dua.id)
        viewModelScope.launch {
            if (isFav) {
                favoritesDao.deleteFavoriteZikr(dua.id)
            } else {
                favoritesDao.insertFavoriteZikr(
                    FavoriteZikrEntity(
                        id = dua.id,
                        categoryTitle = dua.categoryTitle,
                        text = dua.text,
                        reference = dua.reference,
                        grade = dua.grade,
                        isDua = true
                    )
                )
            }
        }
    }
}
