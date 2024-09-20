package com.example.weather.repository

import android.util.Log
import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.City
import com.example.weather.model.Forecast
import com.example.weather.repository.result.ForecastResult
import com.example.weather.service.ForecastService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class ForecastRepository(
    private val api: ForecastService
) {

    suspend fun loadDailyForecasts(city: City): List<Forecast.DailyForecast> {
        Log.d(TAG, "Method call: loadDailyForecasts(city: City): List<Forecast.DailyForecast>")
        if (city.dailyForecasts.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                val forecastResponse = api.getForecast(
                    city.latitude,
                    city.longitude,
                    timezone = city.timezone
                )
                city.dailyForecasts = ForecastMapper.buildForecast(forecastResponse)
            }
        }
        return city.dailyForecasts!!
    }

    suspend fun loadForecast(
        city: City,
    ) = try {

        val dailyForecast = loadDailyForecasts(city)
        val hourlyForecast = getHourlyForecastForTwentyFourHours(city)

        ForecastResult.Content(dailyForecast, hourlyForecast)

    } catch (throwable: Throwable) {
        ForecastResult.Error(throwable)
    }

    suspend fun getHourlyForecastForTwentyFourHours(city: City): List<Forecast.HourlyForecast> {
        val resultForecastList = mutableListOf<Forecast.HourlyForecast>()
        val dailyForecastsForCity = loadDailyForecasts(city)
        val timezoneDateAndTime =
            DateAndTimeMapper.getDateAndTimeInTimezone(city.timezone ?: "Auto").split(" ")
        val currentDate = timezoneDateAndTime[0]
        val currentHour = timezoneDateAndTime[1].split(":")[0].toInt()
        val hourlyForecastsForCurrentDay: MutableList<Forecast.HourlyForecast> =
            dailyForecastsForCity.first() { it.date == currentDate }.hourlyForecasts!!.toMutableList()
        resultForecastList.addAll(
            hourlyForecastsForCurrentDay.drop(currentHour).toMutableList()
        )

        if (resultForecastList.size != 24) {
            val hourlyForecastsForNextDay = dailyForecastsForCity.first {
                it.date == DateAndTimeMapper.getNextDayDate(currentDate)
            }.hourlyForecasts!!.toMutableList()
            resultForecastList.addAll(hourlyForecastsForNextDay.dropLast(resultForecastList.size))
        }

        return resultForecastList
    }

//    suspend fun getHourlyForecastForTwentyFourHours(city: City): List<Forecast.HourlyForecast> {
//        val resultForecastList = mutableListOf<Forecast.HourlyForecast>()
//        val hourlyForecastsForCity = loadHourlyForecasts(city)
//        val timezoneDateAndTime =
//            DateAndTimeMapper.getDateAndTimeInTimezone(city.timezone ?: "Auto").split(" ")
//        val currentDate = timezoneDateAndTime[0]
//        val currentHour = timezoneDateAndTime[1].split(":")[0].toInt()
//        val hourlyForecastsForCurrentDay: MutableList<Forecast.HourlyForecast> =
//            hourlyForecastsForCity.filter { it.date == currentDate }.toMutableList()
//        resultForecastList.addAll(hourlyForecastsForCurrentDay.drop(currentHour).toMutableList())
//
//        if (resultForecastList.size != 24) {
//            val hourlyForecastsForNextDay = hourlyForecastsForCity.filter {
//                it.date == DateAndTimeMapper.getNextDayDate(currentDate)
//            }.toMutableList()
//            resultForecastList.addAll(hourlyForecastsForNextDay.dropLast(resultForecastList.size))
//        }
//
//        return resultForecastList
//    }

    companion object {
        private const val TAG = "ForecastRepository"
    }
}