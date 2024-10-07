package com.example.weather.model

data class DailyForecast(
    val date: String,
    val weatherCode: Int,
    var temperatureMax: Double,
    var temperatureMin: Double,
    var hourlyForecasts: List<HourlyForecast>,
    var temperatureUnit: String
)