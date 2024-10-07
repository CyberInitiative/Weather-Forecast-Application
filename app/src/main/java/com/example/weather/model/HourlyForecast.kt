package com.example.weather.model

data class HourlyForecast(
    val date: String,
    val time: String,
    val weatherCode: Int,
    var temperature: Double,
    val timeOfDay: TimeOfDay
) {
    enum class TimeOfDay{
        DAY,
        NIGHT
    }
}