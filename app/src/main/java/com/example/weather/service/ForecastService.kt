package com.example.weather.service

import com.example.weather.response.forecast.ForecastResponseArrayList
import com.example.weather.response.forecast.ForecastsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastService {

    @GET("forecast")
    suspend fun loadForecasts(
        @Query("latitude") latitudes: List<Double>,
        @Query("longitude") longitudes: List<Double>,
        @Query("hourly") hourlyParams: List<String> = hourlyForecastParameters,
        @Query("daily") dailyParams: List<String> = dailyForecastParameters,
        @Query("timezone") timezones: List<String>,
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("temperature_unit") temperatureUnit: String = "celsius"
    ): Response<ForecastResponseArrayList>

    @GET("forecast")
    suspend fun loadForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourlyParams: List<String> = hourlyForecastParameters,
        @Query("daily") dailyParams: List<String> = dailyForecastParameters,
        @Query("timezone") timezone: String? = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("temperature_unit") temperatureUnit: String = "celsius"
    ): Response<ForecastsResponse>
    companion object {
        val hourlyForecastParameters: List<String> = listOf(
            "temperature_2m",
            "weather_code",
            "is_day"
        )

        val dailyForecastParameters: List<String> = listOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "sunset",
            "sunrise"
        )
    }
}