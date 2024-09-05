package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.Forecast
import com.example.weather.repository.ForecastRepository
import com.example.weather.service.ForecastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForecastViewModel(private val repository: ForecastRepository) : ViewModel() {
    private val _hourlyForecastList = MutableLiveData<List<Forecast.HourlyForecast>>(emptyList())
    val hourlyForecastList: LiveData<List<Forecast.HourlyForecast>> get() = _hourlyForecastList

    private val _dailyForecastList = MutableLiveData<List<Forecast.DailyForecast>>(emptyList())
    val dailyForecastList: LiveData<List<Forecast.DailyForecast>> get() = _dailyForecastList

    fun loadForecast(
        latitude: Double,
        longitude: Double,
        hourlyParams: List<String>,
        dailyParams: List<String>,
        timezone: String = "auto",
        forecastDays: Int = 7
    ) {
        val call = repository.getForecast(
            latitude,
            longitude,
            hourlyParams,
            dailyParams,
            timezone,
            forecastDays
        )
        call.enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                val forecastResponse = response.body()
                if (forecastResponse != null) {
                    val mappedHourlyResponse = ForecastMapper.buildHourlyForecastItemList(forecastResponse)
                    _hourlyForecastList.value = mappedHourlyResponse

                    val mappedDailyResponse = ForecastMapper.buildDailyForecastItemList(forecastResponse)
                    _dailyForecastList.value = mappedDailyResponse
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Log.d(TAG, t.message.toString())
            }

        })

    }

    companion object {
        private const val TAG = "ForecastViewModel"
    }
}