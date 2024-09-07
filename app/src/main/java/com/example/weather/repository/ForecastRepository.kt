package com.example.weather.repository

import android.util.Log
import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.Forecast
import com.example.weather.service.ForecastResponse
import com.example.weather.service.ForecastService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ForecastRepository() {

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ForecastService::class.java)

    private val _dateToHourlyForecastMap: MutableMap<String, MutableList<Forecast.HourlyForecast>> =
        mutableMapOf()
//    val dateToHourlyForecastMap: Map<String, List<Forecast.HourlyForecast>> get() = _dateToHourlyForecastMap

    private val _dailyForecastList: MutableList<Forecast.DailyForecast> = mutableListOf()
    val dailyForecastList: List<Forecast.DailyForecast> get() = _dailyForecastList

    suspend fun loadForecast(
        latitude: Double,
        longitude: Double,
        hourlyParams: List<String>,
        dailyParams: List<String>,
        timezone: String = "auto",
        forecastDays: Int = 7
    ) {
        val forecastResponse = api.getForecast(
            latitude,
            longitude,
            hourlyParams,
            dailyParams,
            timezone,
            forecastDays
        )

        if (forecastResponse != null) {
            val dateToHourlyForecastMap =
                ForecastMapper.buildDateToHourlyForecastMap(forecastResponse)
            populateDateToHourlyForecastMapFromMappedResponse(dateToHourlyForecastMap)

            val dailyForecastList = ForecastMapper.buildDailyForecastItemList(
                forecastResponse,
                _dateToHourlyForecastMap
            )
            populateDailyForecastListFromMappedResponse(dailyForecastList)
        }
    }

    fun getHourlyForecastForTwentyFourHours(): List<Forecast.HourlyForecast>{
        val resultForecastList = mutableListOf<Forecast.HourlyForecast>()
        val currentDate = DateAndTimeMapper.getCurrentDate()
        _dailyForecastList
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

        _dailyForecastList
            .firstOrNull { it.date == DateAndTimeMapper.getNextDayDate(currentDate) }
            ?.let {
                val droppedList = it.hourlyForecastList.toMutableList().dropLast(resultForecastList.size)
                resultForecastList.addAll(droppedList)
            }

        return resultForecastList
    }

    fun getRelevantHourlyForecast(): List<Forecast.HourlyForecast> {
        val hourlyForecastList = mutableListOf<Forecast.HourlyForecast>()
        val currentDate = DateAndTimeMapper.getCurrentDate()
        _dailyForecastList
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
                hourlyForecastList.addAll(mutableList)
            }
        return hourlyForecastList
    }

    private fun populateDateToHourlyForecastMapFromMappedResponse(dateToHourlyForecastMap: Map<String, List<Forecast.HourlyForecast>>) {
        _dateToHourlyForecastMap.clear()
        for ((key, value) in dateToHourlyForecastMap) {
            _dateToHourlyForecastMap[key] = value.toMutableList()
        }
    }

    private fun populateDailyForecastListFromMappedResponse(dailyForecastList: List<Forecast.DailyForecast>) {
        _dailyForecastList.clear()
        for (forecast in dailyForecastList) {
            _dailyForecastList.add(forecast)
        }
    }

    companion object {
        private const val TAG = "ForecastRepository"
        private const val BASE_URL = "https://api.open-meteo.com/v1/"
    }
}