package com.example.weather.repository

import com.example.weather.service.ForecastResponse
import com.example.weather.service.ForecastService
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ForecastRepository() {

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ForecastService::class.java)

    fun getForecast(
        latitude: Double,
        longitude: Double,
        hourlyParams: List<String>,
        dailyParams: List<String>,
        timezone: String = "auto",
        forecastDays: Int = 7
    ): Call<ForecastResponse> {
        return api.getForecast(
            latitude,
            longitude,
            hourlyParams,
            dailyParams,
            timezone,
            forecastDays
        )
    }

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/v1/"
    }
}