package com.example.weather.model

sealed class Forecast {
    data class HourlyForecast(
        val date: String,
        val time: String,
        val weather: String,
        val temperature: Double
    ) : Forecast()

    data class DailyForecast(val date: String, val weather: String) : Forecast()
}