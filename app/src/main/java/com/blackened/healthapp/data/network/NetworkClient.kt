package com.blackened.healthapp.data.network

import android.os.Handler
import android.os.Looper
import com.blackened.healthapp.data.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.jvm.java

object NetworkClient {

    private const val BASE_URL = "https://blackenedhealthapp.ru/api/v1/"

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val healthApi: HealthApiService by lazy {
        retrofit.create(HealthApiService::class.java)
    }

    val familyApi: FamilyApiService by lazy {
        retrofit.create(FamilyApiService::class.java)
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val token = SessionManager.token
                val newRequest = if (!token.isNullOrBlank()) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }

                chain.proceed(newRequest)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonParser.asConverterFactory(contentType))
            .build()
    }


}