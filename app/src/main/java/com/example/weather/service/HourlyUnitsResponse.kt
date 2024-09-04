package com.example.weather.service


import com.google.gson.annotations.SerializedName

data class HourlyUnitsResponse(
    @SerializedName("temperature_2m")
    val temperature2m: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("weather_code")
    val weatherCode: String
)