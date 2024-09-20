package com.example.weather.model

import java.util.UUID

sealed class Forecast {
    data class HourlyForecast(
        val date: String,
        val time: String,
        var weatherCode: Int,
        var temperature: Double,
    ) : Forecast()

    data class DailyForecast(
        val date: String,
        var weatherCode: Int,
        var temperatureMax: Double,
        var temperatureMin: Double,
        var hourlyForecasts: List<HourlyForecast>?
    ) : Forecast()
}