package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("author") val name : String,
    @SerializedName("download_url") val picture : String
)
