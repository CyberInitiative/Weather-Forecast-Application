package com.example.weather.service


import com.google.gson.annotations.SerializedName

data class HourlyResponse(
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>,
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)