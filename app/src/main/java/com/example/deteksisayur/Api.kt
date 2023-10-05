package com.example.deteksisayur

import retrofit2.Call
import retrofit2.http.GET

interface Api {
    @GET("data")
    fun getData(): Call<List<Data>>
}