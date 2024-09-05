package com.example.weather.service


import com.google.gson.annotations.SerializedName

data class HourlyUnitsResponse(
    @SerializedName("temperature_2m")
    val temperature2m: String,
    @SerializedName("time")
    val dateAndTime: String,
    @SerializedName("weather_code")
    val weatherCode: String
)