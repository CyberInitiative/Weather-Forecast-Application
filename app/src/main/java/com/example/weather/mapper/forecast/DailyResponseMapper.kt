package com.example.weather.mapper.forecast

import com.example.weather.mapper.ApiMapper
import com.example.weather.response.forecast.DailyResponse

object DailyResponseMapper :
    ApiMapper<DailyResponse, List<MappedDailyResponse>> {

    override fun mapToDomain(apiResponse: DailyResponse): List<MappedDailyResponse> {
        val mappedDailyResponseList = mutableListOf<MappedDailyResponse>()

        val dates = apiResponse.date
            ?: throw IllegalArgumentException("The date values list in Daily Forecast cannot be null!")
        val weatherCodes = apiResponse.weatherCode
            ?: throw IllegalArgumentException("The weatherCode values list in Daily Forecast cannot be null!")
        val temperatureMaxValues = apiResponse.temperature2mMax
            ?: throw IllegalArgumentException("The temperature2mMax values list in Daily Forecast cannot be null!")
        val temperatureMinValues = apiResponse.temperature2mMin
            ?: throw IllegalArgumentException("The temperature2mMin values list in Daily Forecast cannot be null!")

        for (index in dates.indices) {
            val mappedDailyResponse =
                buildDailyForecastItem(
                    dates[index],
                    weatherCodes[index],
                    temperatureMaxValues[index],
                    temperatureMinValues[index],
                )
            mappedDailyResponseList.add(mappedDailyResponse)
        }

        return mappedDailyResponseList
    }

    private fun buildDailyForecastItem(
        date: String,
        weatherCode: Int,
        temperatureMax: Double,
        temperatureMin: Double,
    ): MappedDailyResponse {
        return MappedDailyResponse(
            date,
            weatherCode,
            temperatureMax,
            temperatureMin
        )
    }
}

data class MappedDailyResponse(
    val date: String,
    val weatherCode: Int,
    val temperatureMax: Double,
    val temperatureMin: Double,
)