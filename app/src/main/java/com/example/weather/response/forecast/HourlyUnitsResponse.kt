package com.example.weather.response.forecast


import com.google.gson.annotations.SerializedName

data class HourlyUnitsResponse(
    @SerializedName("is_day")
    val isDay: String,
    @SerializedName("temperature_2m")
    val temperature2m: String,
    @SerializedName("time")
    val dateAndTime: String,
    @SerializedName("weather_code")
    val weatherCode: String
)