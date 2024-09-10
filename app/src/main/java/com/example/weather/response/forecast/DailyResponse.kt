package com.example.weather.response.forecast


import com.google.gson.annotations.SerializedName

data class DailyResponse(
    @SerializedName("time")
    val date: List<String>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)