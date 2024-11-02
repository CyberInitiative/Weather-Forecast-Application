package com.example.weather.mapper.forecast

import com.example.weather.mapper.ApiMapper
import com.example.weather.utils.DateAndTimeUtils
import com.example.weather.model.HourlyForecast
import com.example.weather.response.forecast.HourlyResponse

object HourlyResponseMapper : ApiMapper<HourlyResponse, Map<String, List<HourlyForecast>>> {
    override fun mapToDomain(apiResponse: HourlyResponse): Map<String, List<HourlyForecast>> {
        val dateToHourlyForecastMap = mutableMapOf<String, MutableList<HourlyForecast>>()

        val temperatureList = apiResponse.temperature2m ?: throw IllegalArgumentException("The temperature2m values list in Hourly Forecast cannot be null!")
        val dateAndTimeList = apiResponse.dateAndTime ?: throw IllegalArgumentException("The dateAndTime values list in Hourly Forecast cannot be null!")
        val weatherCodes = apiResponse.weatherCode ?: throw IllegalArgumentException("The weatherCode values list in Hourly Forecast cannot be null!")
        val timeOfDayCodes = apiResponse.isDay ?: throw IllegalArgumentException("The weatherCode values list in Hourly Forecast cannot be null!")

        for (index in dateAndTimeList.indices) {
            val (date, _) = DateAndTimeUtils.splitDateAndTime(dateAndTimeList[index])

            val hourlyForecast =
                mapSingleHourlyForecastItem(
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

    private fun getTimeOfDay(code: Int): HourlyForecast.TimeOfDay {
        return if (code == 1) {
            HourlyForecast.TimeOfDay.DAY
        } else {
            HourlyForecast.TimeOfDay.NIGHT
        }
    }

    private fun mapSingleHourlyForecastItem(
        dateAndTime: String,
        temperature: Double,
        weatherCode: Int,
        timeOfDay: HourlyForecast.TimeOfDay
    ): HourlyForecast {

        val (date, time) = DateAndTimeUtils.splitDateAndTime(dateAndTime)

        return HourlyForecast(
            date,
            time,
            weatherCode,
            temperature,
            timeOfDay
        )
    }
}