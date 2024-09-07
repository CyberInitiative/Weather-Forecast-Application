package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.mapper.ForecastMapper
import com.example.weather.model.Forecast
import com.example.weather.repository.ForecastRepository
import com.example.weather.service.ForecastResponse
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            repository.loadForecast(
                latitude,
                longitude,
                hourlyParams,
                dailyParams,
                timezone,
                forecastDays
            )
            _dailyForecastList.postValue(repository.dailyForecastList)
            _hourlyForecastList.postValue(repository.getHourlyForecastForTwentyFourHours())
        }
    }

    fun getRelevantHourlyForecast(): List<Forecast.HourlyForecast> {
        return repository.getHourlyForecastForTwentyFourHours()
    }

    companion object {
        private const val TAG = "ForecastViewModel"
    }
}