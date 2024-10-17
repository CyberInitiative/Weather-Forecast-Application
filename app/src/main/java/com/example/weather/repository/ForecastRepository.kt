package com.example.weather.repository

import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.City
import com.example.weather.model.DailyForecast
import com.example.weather.response.forecast.ForecastsResponse
import com.example.weather.result.ResponseResult
import com.example.weather.service.ForecastService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ForecastRepository(
    private val api: ForecastService
) {

    suspend fun loadForecast(city: City): ResponseResult<List<DailyForecast>> {
        return withContext(Dispatchers.IO) {
            try {
                val forecastsResponse: Response<ForecastsResponse> =
                    api.loadForecast(city.latitude, city.longitude, timezone = city.timezone)

                if (forecastsResponse.isSuccessful) {

                    val data = forecastsResponse.body()

                    if (data != null) {
                        val mappedResponse = ForecastMapper.buildForecast(data)
                        ResponseResult.Success(mappedResponse)
                    } else {
                        ResponseResult.Exception(NullPointerException("Response body is null!"))
                    }

                } else {
                    ResponseResult.Error(
                        forecastsResponse.code(),
                        forecastsResponse.errorBody()
                            ?.toString()
                            ?: "Response error!"
                    )
                }

            } catch (ex: Exception) {
                ResponseResult.Exception(ex)
            }
        }

    }

    //TODO Need for loadForecasts() method.
    /*
    /**
     * @return Triple. The first value is a list of latitude values.
     * The second value is a list of longitude values. The third value is a list of timezone values.
     */
    private fun extractLatitudesAndLongitudesAndTimezones(cities: List<City>): Triple<List<Double>, List<Double>, List<String>> {
        val latitudeVals = mutableListOf<Double>()
        val longitudeVals = mutableListOf<Double>()
        val timezoneVals = mutableListOf<String>()

        for (city in cities) {
            latitudeVals.add(city.latitude)
            longitudeVals.add(city.longitude)
            timezoneVals.add(city.timezone ?: "Auto")
        }

        return Triple(latitudeVals, longitudeVals, timezoneVals)
    }
     */


    //TODO Remove or rework.
    // Rounded values of latitude and longitude make hard to map them with values in city instances.
    /*
    suspend fun loadForecasts(cities: List<City>): ResponseResult<Map<Pair<Double, Double>, List<DailyForecast>>> {
        Log.d(TAG, "suspend fun loadForecasts(cities: List<City>): ResponseResult<Map<Pair<Double, Double>, List<DailyForecast>>>")
        return withContext(Dispatchers.IO) {
            try {
                val values = extractLatitudesAndLongitudesAndTimezones(cities)
                val forecastsResponse: Response<ForecastResponseArrayList> =
                    api.loadForecasts(values.first, values.second, timezones = values.third)

                if (forecastsResponse.isSuccessful) {
                    val data = forecastsResponse.body()

                    if (data != null) {
                        val mappedResponse = ForecastMapper.buildForecasts(data)
                        ResponseResult.Success(mappedResponse)
                    } else {
                        ResponseResult.Exception(NullPointerException("Response body is null!"))
                    }

                } else {
                    ResponseResult.Error(
                        forecastsResponse.code(),
                        forecastsResponse.errorBody()
                            ?.toString()
                            ?: "Response error!"
                    )
                }

            } catch (ex: Exception) {
                ResponseResult.Exception(ex)
            }
        }
    }
     */

    //TODO move to viewModel or delete.
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

    //TODO move to viewModel or delete.
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