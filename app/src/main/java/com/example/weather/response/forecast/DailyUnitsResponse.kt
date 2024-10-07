package com.example.weather.response.forecast


import com.google.gson.annotations.SerializedName

data class DailyUnitsResponse(
    @SerializedName("temperature_2m_max")
    val temperature2mMax: String,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: String,
    @SerializedName("sunrise")
    val sunrise: String,
    @SerializedName("sunset")
    val sunset: String,
    @SerializedName("time")
    val date: String,
    @SerializedName("weather_code")
    val weatherCode: String
)