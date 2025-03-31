package com.example.signifybasic.signrecognition

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ModelApiService {

    @Multipart
    @POST("predict")
    fun predict(
        @Part video: MultipartBody.Part
    ): Call<ResponseBody>
}
