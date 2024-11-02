package com.example.weather.response.city


import com.google.gson.annotations.SerializedName

data class CitiesSearchResponse(
    @SerializedName("generationtime_ms")
    val generationtimeMs: Double?,
    @SerializedName("results")
    val cityResponses: List<CityResponse>?
)