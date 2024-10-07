package com.example.weather.repository

import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.City
import com.example.weather.model.DailyForecast
import com.example.weather.response.forecast.ForecastsResponse
import com.example.weather.result.ForecastResult
import com.example.weather.service.ForecastService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ForecastRepository(
    private val api: ForecastService
) {

    private val _dailyForecasts: MutableMap<City, List<DailyForecast>> = mutableMapOf()

    suspend fun loadForecasts(city: City): ForecastResult {
        val forecasts = _dailyForecasts[city]
        return if (forecasts != null) {
            ForecastResult.Content(forecasts)
        } else {
            return withContext(Dispatchers.IO) {
                try {
                    val forecastsResponse: Response<ForecastsResponse> =
                        api.getForecast(city.latitude, city.longitude, timezone = city.timezone)

                    if (forecastsResponse.isSuccessful) {
                        val data = forecastsResponse.body()
                        if (data != null) {
                            val mappedResponse = ForecastMapper.buildForecast(data)
                            _dailyForecasts[city] = mappedResponse

                            ForecastResult.Content(mappedResponse)
                        } else {
                            ForecastResult.Error(NullPointerException("loadForecasts(city: City): ForecastResult; Response body is null!"))
                        }
                    } else {
                        ForecastResult.ResponseError(
                            forecastsResponse.errorBody()
                                ?.toString()
                                ?: "loadForecasts(city: City): ForecastResult; Response error!"
                        )
                    }

                } catch (ex: Exception) {
                    ForecastResult.Error(ex)
                }
            }
        }
    }

    //TODO move to viewModel or delete;
    /*
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
     */

    //TODO move to viewModel or delete;
    /*
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
     */

    companion object {
        private const val TAG = "ForecastRepository"
    }
}