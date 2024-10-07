package com.example.weather.result

import com.example.weather.model.City

sealed class CitySearchResult {
    data class Content(val cities: List<City>): CitySearchResult()
    data class ResponseError(val errorBody: String): CitySearchResult()
    data class Error(val throwable: Throwable): CitySearchResult()
}