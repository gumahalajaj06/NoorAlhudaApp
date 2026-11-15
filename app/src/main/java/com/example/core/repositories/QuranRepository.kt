package com.example.core.repositories

import android.content.Context
import com.example.core.data.SurahCatalog
import com.example.core.database.AppDatabase
import com.example.core.database.BookmarkEntity
import com.example.core.database.CachedAyahEntity
import com.example.core.database.FavoriteAyahEntity
import com.example.core.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

data class AyahSearchHit(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val text: String
)

class QuranRepository(
    private val context: Context,
    private val db: AppDatabase
) {
    private val quranDao = db.quranDao()
    private val bookmarksDao = db.bookmarksDao()
    private val favoritesDao = db.favoritesDao()

    fun observeSurahAyahs(surahNumber: Int): Flow<List<CachedAyahEntity>> =
        quranDao.observeSurahAyahs(surahNumber)

    fun observeBookmarks(): Flow<List<BookmarkEntity>> = bookmarksDao.observeBookmarks()
    fun observeFavoriteAyahs(): Flow<List<FavoriteAyahEntity>> = favoritesDao.observeFavoriteAyahs()

    fun observeMushafPageBookmarked(page: Int): Flow<Boolean> =
        bookmarksDao.observeExists("MUSHAF_$page")

    suspend fun ensureSurahLoaded(surahNumber: Int): Result<List<CachedAyahEntity>> = withContext(Dispatchers.IO) {
        val cached = quranDao.getSurahAyahsOnce(surahNumber)
        val expectedCount = SurahCatalog.getByNumber(surahNumber).ayahCount
        if (cached.size >= expectedCount && cached.isNotEmpty()) {
            return@withContext Result.success(cached)
        }

        // Primary: Al-Quran Cloud (Uthmani + Tafsir Muyassar)
        try {
            val response = NetworkModule.quranCloudApi.getSurahWithTafsir(surahNumber)
            val uthmaniData = response.data.getOrNull(0)
            val tafsirData = response.data.getOrNull(1)
            if (uthmaniData != null && uthmaniData.ayahs.isNotEmpty()) {
                val entities = uthmaniData.ayahs.mapIndexed { idx, ayah ->
                    val tafsirText = tafsirData?.ayahs?.getOrNull(idx)?.text
                        ?: "التفسير الميسّر غير متوفر لهذه الآية حالياً."
                    CachedAyahEntity(
                        surahNumber = surahNumber,
                        ayahNumber = ayah.numberInSurah,
                        globalNumber = ayah.number,
                        textUthmani = ayah.text,
                        tafsirMuyassar = tafsirText,
                        juz = ayah.juz,
                        page = ayah.page,
                        hasSajda = false
                    )
                }
                quranDao.insertAyahs(entities)
                return@withContext Result.success(entities)
            }
        } catch (_: Exception) {
            // Fall through to secondary source
        }

        // Fallback: The-Quran-Project/Quran-API
        try {
            val fallback = NetworkModule.quranProjectApi.getSurah(surahNumber)
            if (fallback.arabic1.isNotEmpty()) {
                val surahInfo = SurahCatalog.getByNumber(surahNumber)
                val entities = fallback.arabic1.mapIndexed { idx, text ->
                    val ayahNo = idx + 1
                    CachedAyahEntity(
                        surahNumber = surahNumber,
                        ayahNumber = ayahNo,
                        globalNumber = SurahCatalog.getGlobalAyahNumber(surahNumber, ayahNo),
                        textUthmani = text,
                        tafsirMuyassar = "يرجى الاتصال بالإنترنت لتحميل التفسير الميسّر لهذه السورة.",
                        juz = 1,
                        page = surahInfo.startPage,
                        hasSajda = false
                    )
                }
                quranDao.insertAyahs(entities)
                return@withContext Result.success(entities)
            }
        } catch (e: Exception) {
            if (cached.isNotEmpty()) return@withContext Result.success(cached)
            return@withContext Result.failure(e)
        }

        if (cached.isNotEmpty()) Result.success(cached)
        else Result.failure(IllegalStateException("تعذر تحميل السورة، تحقق من الاتصال بالإنترنت."))
    }

    suspend fun searchAyahs(query: String): List<AyahSearchHit> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        // Try remote full-Quran search first, merge with local Room cache
        val remoteHits = try {
            val res = NetworkModule.quranCloudApi.searchQuran(trimmed)
            res.data?.matches?.take(40)?.mapNotNull { m ->
                val sNum = m.surah?.number ?: return@mapNotNull null
                val sName = SurahCatalog.getByNumber(sNum).nameArabic
                AyahSearchHit(
                    surahNumber = sNum,
                    surahName = sName,
                    ayahNumber = m.numberInSurah,
                    text = m.text
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        if (remoteHits.isNotEmpty()) return@withContext remoteHits

        quranDao.searchCachedAyahs(trimmed).map {
            AyahSearchHit(
                surahNumber = it.surahNumber,
                surahName = SurahCatalog.getByNumber(it.surahNumber).nameArabic,
                ayahNumber = it.ayahNumber,
                text = it.textUthmani
            )
        }
    }

    suspend fun toggleFavoriteAyah(surahNumber: Int, ayahNumber: Int, text: String, currentlyFavorite: Boolean) {
        val key = "${surahNumber}_$ayahNumber"
        if (currentlyFavorite) {
            favoritesDao.deleteFavoriteAyah(key)
        } else {
            favoritesDao.insertFavoriteAyah(
                FavoriteAyahEntity(
                    key = key,
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = SurahCatalog.getByNumber(surahNumber).nameArabic,
                    textUthmani = text
                )
            )
        }
    }

    suspend fun toggleMushafBookmark(page: Int, currentlyBookmarked: Boolean) {
        val id = "MUSHAF_$page"
        if (currentlyBookmarked) {
            bookmarksDao.deleteBookmark(id)
        } else {
            val surah = SurahCatalog.getSurahForPage(page)
            bookmarksDao.upsertBookmark(
                BookmarkEntity(
                    id = id,
                    type = "MUSHAF",
                    surahNumber = surah.number,
                    ayahNumber = 1,
                    pageNumber = page,
                    title = "صفحة $page — سورة ${surah.nameArabic}",
                    subtitle = "المصحف المدني"
                )
            )
        }
    }

    suspend fun bookmarkAyah(surahNumber: Int, ayahNumber: Int) {
        val surah = SurahCatalog.getByNumber(surahNumber)
        bookmarksDao.upsertBookmark(
            BookmarkEntity(
                id = "AYAH_${surahNumber}_$ayahNumber",
                type = "AYAH",
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                pageNumber = surah.startPage,
                title = "سورة ${surah.nameArabic} — آية $ayahNumber",
                subtitle = "علامة قراءة محفوظة"
            )
        )
    }

    suspend fun deleteBookmark(id: String) = bookmarksDao.deleteBookmark(id)

    // --- Mushaf 604 Pages Offline Storage ---
    private fun mushafDir(): File = File(context.filesDir, "mushaf_pages").apply { mkdirs() }

    fun getLocalMushafPageFile(page: Int): File {
        val padded = page.coerceIn(1, 604).toString().padStart(3, '0')
        return File(mushafDir(), "$padded.png")
    }

    fun getMushafPageUrls(page: Int): List<String> {
        val padded = page.coerceIn(1, 604).toString().padStart(3, '0')
        return listOf(
            "https://raw.githubusercontent.com/GovarJabbar/Quran-PNG/master/$padded.png",
            "https://quran.islam-db.com/data/pages/quranpages_1024/images/page$padded.png"
        )
    }

    fun getDownloadedPagesCount(): Int {
        return mushafDir().listFiles()?.count { it.extension == "png" && it.length() > 1024 } ?: 0
    }

    suspend fun downloadMushafPage(page: Int): Boolean = withContext(Dispatchers.IO) {
        val target = getLocalMushafPageFile(page)
        if (target.exists() && target.length() > 1024) return@withContext true

        for (url in getMushafPageUrls(page)) {
            try {
                val request = Request.Builder().url(url).build()
                NetworkModule.okHttpClient.newCall(request).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bytes = resp.body?.bytes()
                        if (bytes != null && bytes.size > 1024) {
                            target.writeBytes(bytes)
                            return@withContext true
                        }
                    }
                }
            } catch (_: Exception) {
                // try fallback URL
            }
        }
        false
    }

    suspend fun clearMushafCache() = withContext(Dispatchers.IO) {
        mushafDir().listFiles()?.forEach { it.delete() }
    }
}
