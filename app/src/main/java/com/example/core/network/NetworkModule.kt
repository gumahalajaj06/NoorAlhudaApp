package com.example.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val quranCloudApi: QuranCloudApi by lazy {
        buildRetrofit("https://api.alquran.cloud/v1/").create(QuranCloudApi::class.java)
    }

    val quranProjectApi: QuranProjectApi by lazy {
        buildRetrofit("https://quranapi.pages.dev/").create(QuranProjectApi::class.java)
    }

    val aladhanApi: AladhanApi by lazy {
        buildRetrofit("https://api.aladhan.com/v1/").create(AladhanApi::class.java)
    }

    val mp3QuranApi: Mp3QuranApi by lazy {
        buildRetrofit("https://www.mp3quran.net/").create(Mp3QuranApi::class.java)
    }

    val azkarRemoteApi: AzkarRemoteApi by lazy {
        buildRetrofit("https://raw.githubusercontent.com/").create(AzkarRemoteApi::class.java)
    }
}
