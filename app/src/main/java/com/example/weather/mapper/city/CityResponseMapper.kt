package com.example.weather.mapper.city

import com.example.weather.mapper.ApiMapper
import com.example.weather.model.City
import com.example.weather.response.city.CityResponse

object CityResponseMapper: ApiMapper<CityResponse, City> {
    override fun mapToDomain(apiResponse: CityResponse): City {
        return City(
            apiResponse.id ?: throw IllegalArgumentException("City cannot have null id value!"),
            apiResponse.name ?: throw IllegalArgumentException("City cannot have null name value!"),
            apiResponse.country.orEmpty(),
            apiResponse.latitude ?: throw IllegalArgumentException("City cannot have null latitude value!"),
            apiResponse.longitude ?: throw IllegalArgumentException("City cannot have null longitude value!"),
            apiResponse.admin1.orEmpty(),
            apiResponse.admin2.orEmpty(),
            apiResponse.admin3.orEmpty(),
            apiResponse.admin4.orEmpty(),
            apiResponse.timezone ?: "auto"
        )
    }
}