package com.example.weather.mapper

import com.example.weather.R
import com.example.weather.model.Forecast
import com.example.weather.service.ForecastResponse
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
        val dateList = daily.date
        val weatherCodeList = daily.weatherCode

        for (index in dateList.indices) {
            val hourlyForecastList = when {
                mapDateToHourlyForecast.containsKey(dateList[index]) -> {
                    mapDateToHourlyForecast[dateList[index]]
                }

                else -> throw IllegalStateException("There are no hourly forecasts for current date!")
            }
            val dailyForecastItem =
                buildDailyForecastItem(
                    dateList[index],
                    weatherCodeList[index],
                    hourlyForecastList!!
                )
            dailyForecastList.add(dailyForecastItem)
        }

        return dailyForecastList
    }

    private fun buildDailyForecastItem(
        date: String,
        weatherCode: Int,
        hourlyForecastList: List<Forecast.HourlyForecast>
    ): Forecast.DailyForecast {
        return Forecast.DailyForecast(date, weatherCode, hourlyForecastList)
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