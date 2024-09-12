package com.example.weather.mapper

import com.example.weather.model.Forecast
import com.example.weather.response.forecast.ForecastResponse
import java.lang.IllegalStateException

object ForecastMapper {
    fun buildDateToHourlyForecastMap(forecastResponse: ForecastResponse): Map<String, List<Forecast.HourlyForecast>> {
        val mapDateToHourlyForecast = mutableMapOf<String, MutableList<Forecast.HourlyForecast>>()

        val hourlyResponse = forecastResponse.hourlyResponse
        val temperatureList = hourlyResponse.temperature2m
        val timeList = hourlyResponse.dateAndTime
        val weatherCodeList = hourlyResponse.weatherCode

        for (index in timeList.indices) {
            val hourlyForecast =
                buildHourlyForecastItem(
                    temperatureList[index],
                    timeList[index],
                    weatherCodeList[index]
                )
            if (!mapDateToHourlyForecast.containsKey(hourlyForecast.date)) {
                mapDateToHourlyForecast[hourlyForecast.date] = mutableListOf(hourlyForecast)
            } else {
                val hourlyForecastList = mapDateToHourlyForecast[hourlyForecast.date]
                hourlyForecastList!!.add(hourlyForecast)
            }
        }

        return mapDateToHourlyForecast
    }

    fun buildDailyForecastItemList(
        forecastResponse: ForecastResponse,
        mapDateToHourlyForecast: Map<String, List<Forecast.HourlyForecast>>
    ): List<Forecast.DailyForecast> {
        val dailyForecastList = mutableListOf<Forecast.DailyForecast>()

        val daily = forecastResponse.dailyResponse
        val dates = daily.date
        val weatherCodes = daily.weatherCode
        val temperatureMaxValues = daily.temperature2mMax
        val temperatureMinValues = daily.temperature2mMin

        for (index in dates.indices) {
            val hourlyForecastList = when {
                mapDateToHourlyForecast.containsKey(dates[index]) -> {
                    mapDateToHourlyForecast[dates[index]]
                }

                else -> throw IllegalStateException("There are no hourly forecasts for current date!")
            }
            val dailyForecastItem =
                buildDailyForecastItem(
                    dates[index],
                    weatherCodes[index],
                    temperatureMaxValues[index],
                    temperatureMinValues[index],
                    hourlyForecastList!!
                )
            dailyForecastList.add(dailyForecastItem)
        }

        return dailyForecastList
    }

    private fun buildDailyForecastItem(
        date: String,
        weatherCode: Int,
        temperatureMax: Double,
        temperatureMin: Double,
        hourlyForecastList: List<Forecast.HourlyForecast>
    ): Forecast.DailyForecast {
        return Forecast.DailyForecast(
            date,
            weatherCode,
            temperatureMax,
            temperatureMin,
            hourlyForecastList
        )
    }

    private fun buildHourlyForecastItem(
        temperature: Double,
        dateAndTime: String,
        weatherCode: Int
    ): Forecast.HourlyForecast {
        val dateAndTimeSplit = DateAndTimeMapper.splitDateAndTime(dateAndTime)
        return Forecast.HourlyForecast(
            dateAndTimeSplit.first,
            dateAndTimeSplit.second,
            weatherCode,
            temperature
        )
    }

}