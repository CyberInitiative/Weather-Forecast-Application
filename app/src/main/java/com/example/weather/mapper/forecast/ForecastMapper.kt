package com.example.weather.mapper.forecast

import com.example.weather.mapper.ApiMapper
import com.example.weather.model.DailyForecast
import com.example.weather.response.forecast.ForecastsResponse

object ForecastMapper : ApiMapper<ForecastsResponse, List<DailyForecast>> {
    override fun mapToDomain(apiResponse: ForecastsResponse): List<DailyForecast> {
        val dailyForecastList = mutableListOf<DailyForecast>()

        val hourlyResponse = apiResponse.hourlyResponse
            ?: throw IllegalArgumentException("The hourlyResponse in ForecastResponse cannot be null!")
        val hourlyUnitsResponse = apiResponse.hourlyUnitsResponse ?: throw IllegalArgumentException(
            "The hourlyUnitsResponse in ForecastResponse cannot be null!"
        )
        val dailyResponse = apiResponse.dailyResponse
            ?: throw IllegalArgumentException("The dailyResponse in ForecastResponse cannot be null!")

        val mappedHourlyResponse = HourlyResponseMapper.mapToDomain(hourlyResponse)
        val mappedDailyResponse = DailyResponseMapper.mapToDomain(dailyResponse)

        for (item in mappedDailyResponse) {
            val hourlyForecastList = when {
                mappedHourlyResponse.containsKey(item.date) -> {
                    mappedHourlyResponse[item.date]
                }

                else -> throw IllegalStateException("There are no hourly forecasts for current date!")
            }

            dailyForecastList.add(
                DailyForecast(
                    item.date,
                    item.weatherCode,
                    item.temperatureMax,
                    item.temperatureMin,
                    hourlyForecastList!!,
                    hourlyUnitsResponse.temperature2m
                        ?: throw IllegalStateException("The temperature2m in HourlyUnitsResponse is null!")
                )
            )
        }

        return dailyForecastList
    }

}