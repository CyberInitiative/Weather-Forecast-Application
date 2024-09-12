package com.example.weather.model

sealed class Forecast {
    data class HourlyForecast(
        val date: String,
        val time: String,
        val weatherCode: Int,
        val temperature: Double
    ) : Forecast()

    data class DailyForecast(
        val date: String,
        val weatherCode: Int,
        val temperatureMax: Double,
        val temperatureMin: Double,
        val hourlyForecastList: List<HourlyForecast>
    ) : Forecast()
}