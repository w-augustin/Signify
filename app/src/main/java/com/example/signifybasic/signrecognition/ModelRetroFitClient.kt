package com.example.signifybasic.signrecognition

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Request
import okhttp3.Interceptor


object ModelRetrofitClient {
    // VM instance -> external IP
    private const val BASE_URL = "http://34.70.203.161:5000"

    // Interceptor to add "Connection: close" header
    private val connectionCloseInterceptor = Interceptor { chain ->
        val request: Request = chain.request()
            .newBuilder()
            .header("Connection", "close")  // Add this header to close the connection after the request
            .build()
        chain.proceed(request)
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(connectionCloseInterceptor)   // Set read timeout
        .connectTimeout(60, TimeUnit.SECONDS)  // Set connection timeout
        .writeTimeout(60, TimeUnit.SECONDS)    // Set write timeout
        .readTimeout(60, TimeUnit.SECONDS)
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