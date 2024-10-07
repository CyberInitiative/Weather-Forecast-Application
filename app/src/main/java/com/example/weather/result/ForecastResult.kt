package com.example.weather.result

import com.example.weather.model.DailyForecast

sealed class ForecastResult {
    data class Content(val dailyForecasts: List<DailyForecast>): ForecastResult()
    data class ResponseError(val errorBody: String): ForecastResult()
    data class Error(val throwable: Throwable): ForecastResult()
}