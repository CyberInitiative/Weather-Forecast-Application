package com.example.weather.mapper.city

import com.example.weather.mapper.ApiMapper
import com.example.weather.model.City
import com.example.weather.response.city.CitiesSearchResponse

object CitySearchResponseMapper: ApiMapper<CitiesSearchResponse, List<City>> {

    override fun mapToDomain(apiResponse: CitiesSearchResponse): List<City> {
        val cityList = mutableListOf<City>()
        if (!apiResponse.cityResponses.isNullOrEmpty()) {
            for (cityResponse in apiResponse.cityResponses) {
                cityList.add(CityResponseMapper.mapToDomain(cityResponse))
            }
            return cityList
        }
        return emptyList()
    }
}