package com.example.weather.service


import com.google.gson.annotations.SerializedName

data class DailyUnitsResponse(
    @SerializedName("time")
    val date: String,
    @SerializedName("weather_code")
    val weatherCode: String
)