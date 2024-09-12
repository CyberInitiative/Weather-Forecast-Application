package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.City
import com.example.weather.model.Forecast
import com.example.weather.repository.CityRepository
import com.example.weather.repository.ForecastRepository
import com.example.weather.repository.result.ForecastResult
import com.example.weather.viewstate.CityForecastViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _dailyForecastMutableLiveData = MutableLiveData<CityForecastViewState>()
    val dailyForecastLiveData: LiveData<CityForecastViewState> get() = _dailyForecastMutableLiveData

    private val _citySuggestionsMutableLiveData = MutableLiveData<List<City>>(emptyList())
    val citySuggestionsLiveData: LiveData<List<City>> get() = _citySuggestionsMutableLiveData

    private val _trackedCitiesMutableLiveData = MutableLiveData<List<City>>(emptyList())
    val trackedCitiesLiveData: LiveData<List<City>> get() = _trackedCitiesMutableLiveData

    private val _currentCityMutableLiveData = MutableLiveData<City>()
    val currentCityLiveData: LiveData<City> get() = _currentCityMutableLiveData

    fun loadForecast(
        city: City,
    ) {
        viewModelScope.launch {
            forecastRepository.loadForecast(city)
        }
    }

    fun loadForecastAndSet(city: City) {
        viewModelScope.launch {
            forecastRepository.loadForecast(city)
            _currentCityMutableLiveData.value = city
        }
    }

    fun searchForCities(
        cityName: String,
        numOfSuggestedResults: Int = 10,
        language: String = "en"
    ) {
        viewModelScope.launch {
            cityRepository.searchForCities(cityName, numOfSuggestedResults, language)
            _citySuggestionsMutableLiveData.postValue(cityRepository.searchedCities)
        }
    }

    fun saveCity(city: City) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cityRepository.getCurrentCity() == null) {
                city.isCurrentCity = true
            }
            cityRepository.saveCity(city)
            cityRepository.updateSavedCities()
            searchForCities()
        }
    }

    fun setCurrentCity(city: City) {
        viewModelScope.launch(Dispatchers.IO) {
            cityRepository.setCityAsCurrent(city)
            _currentCityMutableLiveData.postValue(city)
        }
    }

    fun getForecastForNextTwentyFourHours(): List<Forecast.HourlyForecast> {
        return currentCityLiveData.value?.let {
            forecastRepository.getHourlyForecastForTwentyFourHours(it)
        } ?: emptyList()
    }

    fun onNewCurrentCity(city: City) {
        _dailyForecastMutableLiveData.value = CityForecastViewState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val dailyForecastResult = forecastRepository.loadForecast(city)
            when (dailyForecastResult) {
                is ForecastResult.Content -> {
                    _dailyForecastMutableLiveData.postValue(
                        CityForecastViewState.Content(
                            dailyForecastResult.forecast
                        )
                    )
                }

                is ForecastResult.Error -> {
                    _dailyForecastMutableLiveData.postValue(
                        CityForecastViewState.Error(
                            dailyForecastResult.throwable
                        )
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
            _currentCityMutableLiveData.value = cities.find { it.isCurrentCity } ?: cities.first()
        }
    }

    fun searchForCities(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            _trackedCitiesMutableLiveData.postValue(cityRepository.loadSavedCities())
        }
    }

    fun loadForecastForTrackedCities() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedCities = cityRepository.loadSavedCities()
            if (savedCities.isNotEmpty()) {
                loadForecastForTrackedCities(savedCities)
            } else {
                _dailyForecastMutableLiveData.postValue(CityForecastViewState.NoCitiesAvailable)
            }
        }
    }

    fun clearCitySuggestionsLiveData() {
        _citySuggestionsMutableLiveData.value = emptyList()
    }

    companion object {
        private const val TAG = "ForecastViewModel"
    }
}