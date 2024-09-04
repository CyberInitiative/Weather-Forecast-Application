package com.example.weather.service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastService {

    @GET("forecast")
    fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourlyParams: List<String>,
        @Query("forecast_days") forecastDays: Int
    ): Call<ForecastResponse>
}