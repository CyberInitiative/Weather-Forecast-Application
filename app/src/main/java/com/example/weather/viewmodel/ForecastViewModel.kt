package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.HourlyForecast
import com.example.weather.repository.ForecastRepository
import com.example.weather.service.ForecastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForecastViewModel(private val repository: ForecastRepository) : ViewModel() {
    private val _hourlyForecastList = MutableLiveData<List<HourlyForecast>>(emptyList())
    val hourlyForecastList: LiveData<List<HourlyForecast>> get() = _hourlyForecastList

    fun loadForecast(
        latitude: Double,
        longitude: Double,
        hourlyParams: List<String>,
        forecastDats: Int
    ) {
        val call = repository.getForecast(latitude, longitude, hourlyParams, forecastDats)
        call.enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                val forecastResponse = response.body()
                if (forecastResponse != null) {
                    val mappedResponse = ForecastMapper.buildForecastItemList(forecastResponse)
                    _hourlyForecastList.value = mappedResponse
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