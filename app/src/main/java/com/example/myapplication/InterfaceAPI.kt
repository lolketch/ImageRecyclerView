package com.example.myapplication

import com.example.myapplication.model.ApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface InterfaceAPI {
    @GET("v2/list?limit=100")
    fun getImageJson():Call<List<ApiResponse>>
}