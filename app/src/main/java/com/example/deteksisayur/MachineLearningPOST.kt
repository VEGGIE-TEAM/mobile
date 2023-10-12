package com.example.deteksisayur

import retrofit2.Call
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface MachineLearningPOST {

     @POST("/predict")
     fun predict(@Body image: RequestBody): Call<DataML>

}