package com.example.weather.repository

import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.City
import com.example.weather.model.Forecast
import com.example.weather.repository.result.ForecastResult
import com.example.weather.service.ForecastService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ForecastRepository() {

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ForecastService::class.java)

    private val _cityToForecast: MutableMap<City, List<Forecast.DailyForecast>> = mutableMapOf()

    suspend fun loadForecast(
        city: City,
    ) = try {
        // if we have downloaded forecast for this city, return it
        if (_cityToForecast[city] != null) {
            ForecastResult.Content(_cityToForecast[city]!!)
        } else {
            val forecastResponse = api.getForecast(
                city.latitude,
                city.longitude,
                timezone = city.timezone
            )
            val dateToHourlyForecastMap =
                ForecastMapper.buildDateToHourlyForecastMap(forecastResponse)
            val dailyForecastList = ForecastMapper.buildDailyForecastItemList(
                forecastResponse,
                dateToHourlyForecastMap
            )
            _cityToForecast[city] = dailyForecastList
            ForecastResult.Content(dailyForecastList)
        }
    } catch (throwable: Throwable) {
        ForecastResult.Error(throwable)
    }

    fun getHourlyForecastForTwentyFourHours(city: City): List<Forecast.HourlyForecast> {
        val resultForecastList = mutableListOf<Forecast.HourlyForecast>()
        _cityToForecast[city]?.let { dailyForecastList ->
            val timezoneDateAndTime = DateAndTimeMapper.getDateAndTimeInTimezone(city.timezone?: "Auto").split(" ")
            val currentDate = timezoneDateAndTime[0]
            dailyForecastList
                .firstOrNull { it.date == currentDate }
                ?.let {
                    val mutableList = it.hourlyForecastList.toMutableList()
                    val iterator = mutableList.listIterator()
                    while (iterator.hasNext()) {
                        val nextElement = iterator.next()
                        if (nextElement.date == currentDate) {
                            val hour = nextElement.time.split(":")[0]
                            val timezoneHour = timezoneDateAndTime[1].split(":")[0]
                            if (hour.toInt() < timezoneHour.toInt()) {
                                iterator.remove()
                            }
                        }
                    }
                    resultForecastList.addAll(mutableList)
                } ?: return resultForecastList

            dailyForecastList
                .firstOrNull { it.date == DateAndTimeMapper.getNextDayDate(currentDate) }
                ?.let {
                    val droppedList =
                        it.hourlyForecastList.toMutableList().dropLast(resultForecastList.size)
                    resultForecastList.addAll(droppedList)
                }
        }
        return resultForecastList
    }

    companion object {
        private const val TAG = "ForecastRepository"
        private const val BASE_URL = "https://api.open-meteo.com/v1/"
    }
}