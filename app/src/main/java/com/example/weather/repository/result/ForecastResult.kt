package com.example.weather.repository.result

import com.example.weather.model.Forecast

sealed class ForecastResult {
    data class Content(val forecast: List<Forecast.DailyForecast>): ForecastResult()
    data class Error(val throwable: Throwable): ForecastResult()
}