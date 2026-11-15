package com.example.core.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranCloudApi {
    @GET("surah/{surahNumber}/editions/quran-uthmani,ar.muyassar")
    suspend fun getSurahWithTafsir(
        @Path("surahNumber") surahNumber: Int
    ): QuranCloudMultiEditionResponse

    @GET("search/{keyword}/all/quran-simple-clean")
    suspend fun searchQuran(
        @Path("keyword") keyword: String
    ): QuranCloudSearchResponse
}

interface QuranProjectApi {
    @GET("api/{surahNo}.json")
    suspend fun getSurah(
        @Path("surahNo") surahNo: Int
    ): QuranProjectSurahResponse
}

interface AladhanApi {
    @GET("timings/{date}")
    suspend fun getTimingsByCoordinates(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int,
        @Query("adjustment") adjustment: Int
    ): AladhanTimingsResponse

    @GET("calendar/{year}/{month}")
    suspend fun getMonthlyCalendar(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int
    ): AladhanCalendarResponse
}

interface Mp3QuranApi {
    @GET("api/v3/reciters")
    suspend fun getReciters(
        @Query("language") language: String = "ar"
    ): Mp3QuranRecitersResponse
}

interface AzkarRemoteApi {
    @GET("YousefAsalya/Islamic-Pro-azkar-API/main/data/ar.json")
    suspend fun fetchArabicAzkar(): List<RemoteAzkarCategoryDto>
}
