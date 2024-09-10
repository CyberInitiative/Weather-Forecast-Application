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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _hourlyForecastMutableLiveData = MutableLiveData<List<Forecast.HourlyForecast>>(emptyList())
    val hourlyForecastLiveData: LiveData<List<Forecast.HourlyForecast>> get() = _hourlyForecastMutableLiveData

    private val _dailyForecastMutableLiveData = MutableLiveData<List<Forecast.DailyForecast>>(emptyList())
    val dailyForecastLiveData: LiveData<List<Forecast.DailyForecast>> get() = _dailyForecastMutableLiveData

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

    fun loadForecastAndSet(city: City){
        viewModelScope.launch {
            forecastRepository.loadForecast(city)
            _currentCityMutableLiveData.value = city
        }
    }

    fun loadCities(cityName: String, numOfSuggestedResults: Int = 10, language: String = "en") {
        viewModelScope.launch {
            cityRepository.loadCities(cityName, numOfSuggestedResults, language)
            _citySuggestionsMutableLiveData.postValue(cityRepository.cityList)
        }
    }

    fun loadCities(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            _trackedCitiesMutableLiveData.postValue(cityRepository.loadCities())
        }
    }

    fun saveCity(city: City) {
        viewModelScope.launch(Dispatchers.IO) {
            cityRepository.saveCity(city)
        }
    }

    fun setCurrentCity(city: City) {
        _currentCityMutableLiveData.value = city
    }

    fun onNewCurrentCity(city: City) {
        val dailyForecastList = forecastRepository.cityToForecast[city]
        dailyForecastList?.let {
            _dailyForecastMutableLiveData.postValue(it)
            _hourlyForecastMutableLiveData.postValue(
                forecastRepository.getHourlyForecastForTwentyFourHours(city)
            )
        }
    }

    private fun loadForecastForTrackedCities(cities: List<City>) {
        val jobs = mutableListOf<Job>()
        viewModelScope.launch {
            for (city in cities) {
                Log.d(TAG, "CITY NAME: ${city.name}, CITY TIMEZONE: ${city.timezone}")
                jobs.add(launch(Dispatchers.IO) {
                    forecastRepository.loadForecast(city)
                })
            }

            jobs.joinAll()
            _currentCityMutableLiveData.value = cities.first()
        }
    }

    fun loadForecastForTrackedCities() {
        if (trackedCitiesLiveData.value != null && trackedCitiesLiveData.value!!.isNotEmpty()) {
            loadForecastForTrackedCities(trackedCitiesLiveData.value!!)
        } else {
            viewModelScope.launch {
                loadCities().join()
                loadForecastForTrackedCities(trackedCitiesLiveData.value!!)
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