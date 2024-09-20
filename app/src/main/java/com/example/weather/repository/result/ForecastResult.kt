package com.example.weather.repository.result

import com.example.weather.model.Forecast

sealed class ForecastResult {
    data class Content(val dailyForecasts: List<Forecast.DailyForecast>, val hourlyForecasts: List<Forecast.HourlyForecast>): ForecastResult()
    data class Error(val throwable: Throwable): ForecastResult()
}