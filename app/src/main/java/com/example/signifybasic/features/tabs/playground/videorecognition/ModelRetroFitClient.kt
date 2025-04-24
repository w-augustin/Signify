package com.example.signifybasic.features.tabs.playground.videorecognition

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Request
import okhttp3.Interceptor


object ModelRetrofitClient {
    private var retrofit: Retrofit? = null
    var BASE_URL: String = "http://10.20.0.77:5000"

    fun getInstance(): ModelApiService {
        if (retrofit == null || retrofit?.baseUrl().toString() != BASE_URL) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ModelApiService::class.java)
    }

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

}