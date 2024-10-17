package com.example.weather.viewstate

sealed class ForecastsViewState {
    object Loading: ForecastsViewState()
    object NoCitiesAvailable: ForecastsViewState()
    class Success(val itemsCount: Int) : ForecastsViewState()
    data class Error(val throwable: Throwable): ForecastsViewState()
}