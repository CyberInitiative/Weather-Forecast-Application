package com.example.weather.mapper

import com.example.weather.model.City
import com.example.weather.response.city.CityResponse
import com.example.weather.response.city.CitiesSearchResponse

object CityLocationMapper {
    fun buildCityLocationList(citiesSearchResponse: CitiesSearchResponse): List<City> {
        val cityList = mutableListOf<City>()
        if (citiesSearchResponse.cityResponses != null && citiesSearchResponse.cityResponses.isNotEmpty()) {
            for (cityResponse in citiesSearchResponse.cityResponses) {
                cityList.add(buildCityLocation(cityResponse))
            }
            return cityList
        }
        return emptyList()
    }

    private fun buildCityLocation(cityResponse: CityResponse): City {
        return City(
            cityResponse.id,
            cityResponse.name,
            cityResponse.country,
            cityResponse.latitude,
            cityResponse.longitude,
            cityResponse.admin1,
            cityResponse.admin2,
            cityResponse.admin3,
            cityResponse.admin4,
            cityResponse.timezone ?: "auto"
        )
    }
}