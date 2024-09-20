package com.example.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.City
import com.example.weather.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CitySearchViewModel(private val cityRepository: CityRepository) : ViewModel() {

    private val _citySuggestions = MutableLiveData<List<City>>(emptyList())
    val citySuggestions: LiveData<List<City>> get() = _citySuggestions

    fun saveCity(city: City){
        viewModelScope.launch(Dispatchers.IO) {
            if (cityRepository.getCurrentCity() == null) {
                city.isCurrentCity = true
            }
            cityRepository.save(city)
        }
    }

    fun searchCity(
        cityName: String, numOfSuggestedResults: Int = 20, language: String = "en"
    ) {
        viewModelScope.launch {
            _citySuggestions.value = cityRepository.search(
                cityName, numOfSuggestedResults, language
            )
        }
    }

    fun cleanCitiesSuggestions(){
        _citySuggestions.value = emptyList()
    }
}