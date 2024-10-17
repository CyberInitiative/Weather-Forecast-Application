package com.example.weather.viewstate

import com.example.weather.model.DailyForecast

sealed class ForecastViewState {
    object Loading: ForecastViewState()
    data class Content(val dailyForecasts: List<DailyForecast>): ForecastViewState()
    data class Error(val throwable: Throwable): ForecastViewState()
}