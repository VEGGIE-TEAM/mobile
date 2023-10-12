package com.example.deteksisayur

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DatabasePOST {
    @Headers("Content-Type: application/json")
    @POST("/api/data")
    fun sendDeteksiResult(@Body data: Data): Call<Void>
}
