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
                    api.getForecast(city.latitude, city.longitude, timezone = city.timezone)

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

    companion object {
        private const val TAG = "ForecastRepository"
    }
}