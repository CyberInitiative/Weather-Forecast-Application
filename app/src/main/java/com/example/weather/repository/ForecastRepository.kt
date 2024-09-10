package com.example.weather.repository

import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.City
import com.example.weather.model.Forecast
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
    val cityToForecast: Map<City, List<Forecast.DailyForecast>> get() = _cityToForecast

    suspend fun loadForecast(
        city: City,
    ) {
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
    }

    fun getHourlyForecastForTwentyFourHours(city: City): List<Forecast.HourlyForecast> {
        val resultForecastList = mutableListOf<Forecast.HourlyForecast>()
        _cityToForecast[city]?.let { dailyForecastList ->
            val currentDate = DateAndTimeMapper.getCurrentDate()
            dailyForecastList
                .firstOrNull { it.date == currentDate }
                ?.let {
                    val mutableList = it.hourlyForecastList.toMutableList()
                    val iterator = mutableList.listIterator()
                    while (iterator.hasNext()) {
                        val nextElement = iterator.next()
                        if (nextElement.date == DateAndTimeMapper.getCurrentDate()) {
                            val hour = nextElement.time.split(":")[0]
                            if (hour.toInt() < DateAndTimeMapper.getCurrentHour()) {
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