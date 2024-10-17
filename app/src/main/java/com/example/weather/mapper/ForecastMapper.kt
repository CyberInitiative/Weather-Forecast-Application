package com.example.weather.mapper

import android.util.Log
import com.example.weather.model.DailyForecast
import com.example.weather.model.HourlyForecast
import com.example.weather.response.forecast.ForecastResponseArrayList
import com.example.weather.response.forecast.ForecastsResponse

object ForecastMapper {
    private val TAG = "ForecastMapper"

    fun buildForecast(forecastsResponse: ForecastsResponse): List<DailyForecast> {
        val hourlyForecastMap = buildHourlyForecasts(forecastsResponse)
        return buildDailyForecastItemList(forecastsResponse, hourlyForecastMap)
    }

    fun buildForecasts(forecastResponseArrayList: ForecastResponseArrayList): Map<Pair<Double, Double>, List<DailyForecast>> {
        val result = mutableMapOf<Pair<Double, Double>, List<DailyForecast>>()
        for (item in forecastResponseArrayList){
            Log.d(TAG, "$item")
            val key = Pair(item.latitude, item.longitude)
            Log.d(TAG,"KEY: $key")
            val value = buildForecast(item)
            result[key] = value
        }
        return result
    }

    fun buildDailyForecastItemList(
        forecastsResponse: ForecastsResponse,
        mapDateToHourlyForecast: Map<String, List<HourlyForecast>>
    ): List<DailyForecast> {
        val dailyForecastList = mutableListOf<DailyForecast>()

        val daily = forecastsResponse.dailyResponse
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
                    hourlyForecastList!!,
                    forecastsResponse.hourlyUnitsResponse.temperature2m
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
        hourlyForecastList: List<HourlyForecast>,
        temperatureUnit: String
    ): DailyForecast {
        return DailyForecast(
            date,
            weatherCode,
            temperatureMax,
            temperatureMin,
            hourlyForecastList,
            temperatureUnit
        )
    }

    fun buildHourlyForecasts(forecastsResponse: ForecastsResponse): Map<String, List<HourlyForecast>> {

        fun getTimeOfDay(code: Int): HourlyForecast.TimeOfDay {
            return if (code == 1) {
                HourlyForecast.TimeOfDay.DAY
            } else {
                HourlyForecast.TimeOfDay.NIGHT
            }
        }

        val dateToHourlyForecastMap = mutableMapOf<String, MutableList<HourlyForecast>>()

        val hourlyResponse = forecastsResponse.hourlyResponse

        val temperatureList = hourlyResponse.temperature2m
        val dateAndTimeList = hourlyResponse.dateAndTime
        val weatherCodes = hourlyResponse.weatherCode
        val timeOfDayCodes = hourlyResponse.isDay

        for (index in dateAndTimeList.indices) {
            val (date, time) = DateAndTimeMapper.splitDateAndTime(dateAndTimeList[index])

            val hourlyForecast =
                buildHourlyForecastItem(
                    dateAndTimeList[index],
                    temperatureList[index],
                    weatherCodes[index],
                    getTimeOfDay(timeOfDayCodes[index])
                )

            if (!dateToHourlyForecastMap.containsKey(date)) {
                dateToHourlyForecastMap[date] = mutableListOf(hourlyForecast)
            } else {
                val hourlyForecastList = dateToHourlyForecastMap[date]
                hourlyForecastList!!.add(hourlyForecast)
            }
        }

        return dateToHourlyForecastMap
    }


    private fun buildHourlyForecastItem(
        dateAndTime: String,
        temperature: Double,
        weatherCode: Int,
        timeOfDay: HourlyForecast.TimeOfDay
    ): HourlyForecast {

        val (date, time) = DateAndTimeMapper.splitDateAndTime(dateAndTime)

        return HourlyForecast(
            date,
            time,
            weatherCode,
            temperature,
            timeOfDay
        )
    }
}