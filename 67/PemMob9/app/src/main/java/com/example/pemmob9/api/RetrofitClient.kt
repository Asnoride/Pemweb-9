package com.example.pemmob9.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Thread-safe singleton for Retrofit 2.9.0 configured for Android Emulator local IP.
 * Uses Double-Checked Locking pattern with @Volatile to guarantee thread-safety.
 */
object RetrofitClient {

    // Primary local emulator IP URL (http://10.0.2.2:8000/)
    const val BASE_URL = "http://10.0.2.2:8000/"

    @Volatile
    private var apiServiceInstance: ApiService? = null

    /**
     * Returns the thread-safe singleton instance of ApiService.
     */
    fun getApiService(): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: createApiService().also { apiServiceInstance = it }
        }
    }

    /**
     * Creates a new ApiService instance with OkHttp Logging Interceptor & GSON Converter.
     */
    private fun createApiService(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
