package com.example.weather.response.forecast


import com.google.gson.annotations.SerializedName

data class DailyResponse(
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double>?,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double>?,
    @SerializedName("sunrise")
    val sunrise: List<String>?,
    @SerializedName("sunset")
    val sunset: List<String>?,
    @SerializedName("time")
    val date: List<String>?,
    @SerializedName("weather_code")
    val weatherCode: List<Int>?
)