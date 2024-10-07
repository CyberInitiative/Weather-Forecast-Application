package com.example.weather.viewstate

import com.example.weather.model.City

sealed class CitySearchViewState {
    object Initial: CitySearchViewState()
    object Loading: CitySearchViewState()
    data class Content(val cities: List<City>): CitySearchViewState()
    data class Error(val throwable: Throwable): CitySearchViewState()
}