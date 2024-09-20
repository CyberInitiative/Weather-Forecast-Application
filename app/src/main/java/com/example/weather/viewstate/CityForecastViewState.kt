package com.example.weather.viewstate

import com.example.weather.model.Forecast

sealed class CityForecastViewState {
    object Loading: CityForecastViewState()
    object NoCitiesAvailable: CityForecastViewState()
    data class Content(val dailyForecasts: List<Forecast.DailyForecast>, val hourlyForecasts: List<Forecast.HourlyForecast>): CityForecastViewState()
    data class Error(val throwable: Throwable): CityForecastViewState()
}