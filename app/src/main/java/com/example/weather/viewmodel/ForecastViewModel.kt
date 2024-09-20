package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.City
import com.example.weather.repository.CityRepository
import com.example.weather.repository.ForecastRepository
import com.example.weather.repository.result.ForecastResult
import com.example.weather.viewstate.CityForecastViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _cityForecastState =
        MutableStateFlow<CityForecastViewState>(CityForecastViewState.Loading)
    val cityForecastState: Flow<CityForecastViewState> get() = _cityForecastState

    private val _trackedCitiesMutableLiveData = MutableLiveData<List<City>>(emptyList())
    val trackedCitiesLiveData: LiveData<List<City>> get() = _trackedCitiesMutableLiveData

    private val _currentCityState = MutableStateFlow<City?>(null)
    val currentCityState: Flow<City?> get() = _currentCityState

    fun setCurrentCity(city: City) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "New current city is: $city")
            if (!city.isCurrentCity) {
                cityRepository.setCityAsCurrent(city)
            }
            _currentCityState.value = city
        }
    }

    fun onNewCurrentCity(city: City) {
        Log.d(TAG, "onNewCurrentCity() called")
        _cityForecastState.value = CityForecastViewState.Loading
        viewModelScope.launch {
            when (val dailyForecastResult = forecastRepository.loadForecast(city)) {
                is ForecastResult.Content -> {
                    Log.d(
                        TAG,
                        "onNewCurrentCity(); dailyForecastResult is ForecastResult.Content"
                    )
                    _cityForecastState.value =
                        CityForecastViewState.Content(
                            dailyForecastResult.dailyForecasts,
                            dailyForecastResult.hourlyForecasts
                        )
                }

                is ForecastResult.Error -> {
                    Log.d(TAG, "onNewCurrentCity(); ForecastResult.Error")
                    _cityForecastState.value = CityForecastViewState.Error(
                        dailyForecastResult.throwable
                    )

                    Log.d(TAG, dailyForecastResult.throwable.toString())
                }
            }

        }
    }

    private fun loadForecastForTrackedCities(cities: List<City>) {
        val jobs = mutableListOf<Job>()
        viewModelScope.launch {
            for (city in cities) {
                jobs.add(launch(Dispatchers.IO) {
                    forecastRepository.loadForecast(city)
                })
            }

            jobs.joinAll()
            _currentCityState.value = cities.find { it.isCurrentCity } ?: cities.first()
        }
    }

    fun loadListOfTrackedCities() {
        viewModelScope.launch(Dispatchers.IO) {
            _trackedCitiesMutableLiveData.postValue(cityRepository.loadAll())
        }
    }

    fun loadForecastForTrackedCities() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedCities = cityRepository.loadAll()
            if (savedCities.isNotEmpty()) {
                loadForecastForTrackedCities(savedCities)
            } else {
                _cityForecastState.value = CityForecastViewState.NoCitiesAvailable
            }
        }
    }

    fun deleteTrackedCity(city: City) {
        viewModelScope.launch {
            cityRepository.delete(city)
            if (city.isCurrentCity) {
                setCurrentCity(cityRepository.loadAll().first())
            }
            _trackedCitiesMutableLiveData.postValue(cityRepository.loadAll())
        }
    }

    companion object {
        private const val TAG = "ForecastViewModel"
    }
}