package com.example.features.azkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.AzkarCatalog
import com.example.core.data.ZikrCategory
import com.example.core.data.ZikrItem
import com.example.core.database.AppDatabase
import com.example.core.database.FavoriteZikrEntity
import com.example.core.network.NetworkModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AzkarUiState(
    val categories: List<ZikrCategory> = AzkarCatalog.categories,
    val selectedCategory: ZikrCategory = AzkarCatalog.categories.first(),
    val itemsInCategory: List<ZikrItem> = AzkarCatalog.builtInAzkar.filter { it.categoryId == "morning" },
    val remainingCounts: Map<String, Int> = emptyMap(),
    val favoriteIds: Set<String> = emptySet(),
    val isSyncingRemote: Boolean = false,
    val syncMessage: String? = null
)

class AzkarViewModel(private val db: AppDatabase) : ViewModel() {
    private val favoritesDao = db.favoritesDao()

    private val allAzkar = MutableStateFlow(AzkarCatalog.builtInAzkar)
    private val _uiState = MutableStateFlow(AzkarUiState())
    val uiState: StateFlow<AzkarUiState> = _uiState.asStateFlow()

    init {
        resetCountsForList(AzkarCatalog.builtInAzkar)

        viewModelScope.launch {
            favoritesDao.observeFavoriteAzkar().collect { favs ->
                _uiState.update { it.copy(favoriteIds = favs.map { f -> f.id }.toSet()) }
            }
        }
    }

    private fun resetCountsForList(items: List<ZikrItem>) {
        val counts = items.associate { it.id to it.repeatCount }
        _uiState.update { it.copy(remainingCounts = it.remainingCounts + counts) }
    }

    fun selectCategory(category: ZikrCategory) {
        val filtered = allAzkar.value.filter { it.categoryId == category.id }
        _uiState.update {
            it.copy(
                selectedCategory = category,
                itemsInCategory = filtered
            )
        }
    }

    fun decrementZikr(item: ZikrItem) {
        val current = _uiState.value.remainingCounts[item.id] ?: item.repeatCount
        if (current > 0) {
            _uiState.update { state ->
                state.copy(remainingCounts = state.remainingCounts + (item.id to (current - 1)))
            }
        }
    }

    fun resetZikrCount(item: ZikrItem) {
        _uiState.update { state ->
            state.copy(remainingCounts = state.remainingCounts + (item.id to item.repeatCount))
        }
    }

    fun resetCategoryCounts() {
        val updated = _uiState.value.remainingCounts.toMutableMap()
        _uiState.value.itemsInCategory.forEach { item ->
            updated[item.id] = item.repeatCount
        }
        _uiState.update { it.copy(remainingCounts = updated) }
    }

    fun toggleFavorite(item: ZikrItem) {
        val isFav = _uiState.value.favoriteIds.contains(item.id)
        viewModelScope.launch {
            if (isFav) {
                favoritesDao.deleteFavoriteZikr(item.id)
            } else {
                favoritesDao.insertFavoriteZikr(
                    FavoriteZikrEntity(
                        id = item.id,
                        categoryTitle = item.categoryTitle,
                        text = item.text,
                        reference = item.reference,
                        grade = item.grade,
                        isDua = false
                    )
                )
            }
        }
    }

    fun syncFromRemoteApi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingRemote = true, syncMessage = null) }
            try {
                val remoteCategories = NetworkModule.azkarRemoteApi.fetchArabicAzkar()
                if (remoteCategories.isNotEmpty()) {
                    // Merge verified remote categories that map cleanly into our 12 categories
                    val extraItems = mutableListOf<ZikrItem>()
                    remoteCategories.forEach { cat ->
                        val mappedId = mapRemoteCategoryToId(cat.category)
                        if (mappedId != null) {
                            val existingTexts = allAzkar.value
                                .filter { it.categoryId == mappedId }
                                .map { it.text.take(30) }
                                .toSet()
                            cat.array.take(15).forEach { entry ->
                                val cleanText = entry.text.trim()
                                if (cleanText.isNotEmpty() && cleanText.take(30) !in existingTexts) {
                                    extraItems.add(
                                        ZikrItem(
                                            id = "rem_${cat.id}_${entry.id}",
                                            categoryId = mappedId,
                                            categoryTitle = AzkarCatalog.categories.first { it.id == mappedId }.title,
                                            text = cleanText,
                                            repeatCount = entry.count.coerceAtLeast(1),
                                            reference = entry.reference?.takeIf { it.isNotBlank() } ?: "حصن المسلم — Islamic-Pro Azkar API",
                                            grade = "مأثور عن حصن المسلم"
                                        )
                                    )
                                }
                            }
                        }
                    }
                    if (extraItems.isNotEmpty()) {
                        val merged = AzkarCatalog.builtInAzkar + extraItems
                        allAzkar.value = merged
                        resetCountsForList(extraItems)
                        val curCat = _uiState.value.selectedCategory
                        _uiState.update {
                            it.copy(
                                itemsInCategory = merged.filter { item -> item.categoryId == curCat.id },
                                syncMessage = "تم تحديث الأذكار وإضافة ${extraItems.size} ذكراً من حصن المسلم"
                            )
                        }
                    } else {
                        _uiState.update { it.copy(syncMessage = "جميع الأذكار محدثة بالفعل") }
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(syncMessage = "تعذر التحديث من الخادم، يتم عرض الأذكار الموثقة المحفوظة محلياً.")
                }
            } finally {
                _uiState.update { it.copy(isSyncingRemote = false) }
            }
        }
    }

    private fun mapRemoteCategoryToId(name: String): String? {
        return when {
            name.contains("الصباح") -> "morning"
            name.contains("المساء") -> "evening"
            name.contains("النوم") -> "sleep"
            name.contains("الاستيقاظ") -> "wakeup"
            name.contains("بعد السلام") || name.contains("الصلاة") -> "after_prayer"
            name.contains("الأذان") -> "adhan"
            name.contains("الوضوء") -> "wudu"
            name.contains("المنزل") -> "home"
            name.contains("المسجد") -> "mosque"
            name.contains("السفر") -> "travel"
            name.contains("الطعام") -> "food"
            name.contains("الاستغفار") || name.contains("التسبيح") -> "istighfar"
            else -> null
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }
}
