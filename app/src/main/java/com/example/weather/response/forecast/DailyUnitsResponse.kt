package com.example.weather.response.forecast


import com.google.gson.annotations.SerializedName

data class DailyUnitsResponse(
    @SerializedName("time")
    val date: String,
    @SerializedName("weather_code")
    val weatherCode: String
)