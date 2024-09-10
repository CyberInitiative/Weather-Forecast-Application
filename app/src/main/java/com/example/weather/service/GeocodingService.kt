package com.example.weather.service

import com.example.weather.response.city.CitiesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {

    @GET("search?format=json")
    suspend fun getLocations(
        @Query("name") cityName: String,
        @Query("count") numOfSuggestedResults: Int = 10,
        @Query("language") language: String = "en"
    ): CitiesSearchResponse
}