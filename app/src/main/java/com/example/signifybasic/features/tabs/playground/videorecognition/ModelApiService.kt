package com.example.signifybasic.features.tabs.playground.videorecognition

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ModelApiService {

    @Multipart
    @POST("predict")
    fun predict(
        @Part video: MultipartBody.Part,
        @Part expectedSign: MultipartBody.Part,
        @Query("method") method: String = "holistic" // Default to "holistic"
    ): Call<ResponseBody>
}
