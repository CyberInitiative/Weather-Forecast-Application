package com.example.weather.service

import com.example.weather.response.forecast.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastService {

    @GET("forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourlyParams: List<String> = listOf("temperature_2m", "weather_code"),
        @Query("daily") daily: List<String> = listOf("weather_code"),
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7
    ): ForecastResponse
}