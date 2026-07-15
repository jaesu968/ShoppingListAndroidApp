package com.example.shoppinglist.data.api

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Kotlin singleton object to connect Retrofit appropriately
object RetrofitClient {
    // Base URL for the API
    private const val BASE_URL = "http://10.0.2.2:8000/api/"
    // Retrofit instance
    // Use JSON as a converter factory so the unmodeled picked filed doesn't crash parsing
    private val json = Json { ignoreUnknownKeys = true }
    // logging interceptor for debugging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // client
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    // Api Service interface
    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)
}