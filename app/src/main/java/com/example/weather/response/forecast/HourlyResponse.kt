package com.example.weather.response.forecast


import com.google.gson.annotations.SerializedName

data class HourlyResponse(
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>,
    @SerializedName("time")
    val dateAndTime: List<String>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)