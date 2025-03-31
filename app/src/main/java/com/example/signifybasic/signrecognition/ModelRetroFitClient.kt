package com.example.signifybasic.signrecognition

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ModelRetrofitClient {
    // VM instance -> external IP
    private const val BASE_URL = "http://34.70.203.161:5000"

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // Set connection timeout
        .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
        .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
        .build()

    val apiService: ModelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ModelApiService::class.java)
    }
}