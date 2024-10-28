package com.example.weather.viewstate

import com.example.weather.model.DailyForecast
import com.example.weather.model.HourlyForecast

sealed class ForecastViewState {
    object Loading: ForecastViewState()
//    object NoCitiesAvailable: ForecastViewState()
    data class Content(val dailyForecasts: List<DailyForecast>, val hourlyForecasts: List<HourlyForecast>): ForecastViewState()
    data class Error(val throwable: Throwable): ForecastViewState()
}