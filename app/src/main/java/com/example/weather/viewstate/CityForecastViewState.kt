package com.example.weather.viewstate

import com.example.weather.model.Forecast

sealed class CityForecastViewState {
    object Loading: CityForecastViewState()
    object NoCitiesAvailable: CityForecastViewState()
    data class Content(val forecast: List<Forecast.DailyForecast>): CityForecastViewState()
    data class Error(val throwable: Throwable): CityForecastViewState()
}