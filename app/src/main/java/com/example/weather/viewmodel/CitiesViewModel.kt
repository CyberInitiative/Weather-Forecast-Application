package com.example.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.City
import com.example.weather.repository.CityRepository
import com.example.weather.result.CitySearchResult
import com.example.weather.viewstate.CitySearchViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CitiesViewModel(private val cityRepository: CityRepository) : ViewModel() {

    private val _citySuggestionsState =
        MutableStateFlow<CitySearchViewState>(CitySearchViewState.Initial)
    val citySuggestionsState: Flow<CitySearchViewState> get() = _citySuggestionsState

    private val _trackedCitiesState = MutableLiveData<List<City>>(emptyList())
    val trackedCitiesState: LiveData<List<City>> get() = _trackedCitiesState

    init {
        viewModelScope.launch {
            _trackedCitiesState.value = cityRepository.loadAll()
        }
    }

    fun getTrackedCities(): List<City>? {
        return _trackedCitiesState.value
    }

    fun getTrackedCityByPosition(position: Int): City? {
        return _trackedCitiesState.value?.get(position)
    }

    fun saveCity(city: City) {
        viewModelScope.launch(Dispatchers.IO) {
            cityRepository.save(city)
        }
//            if (cityRepository.getCurrentCity() == null) {
//                city.isCurrentCity = true
//            }
        val trackedCitiesValue = _trackedCitiesState.value
        if (trackedCitiesValue != null) {
            val newValue = trackedCitiesValue.toMutableList().also { it.add(city) }
            _trackedCitiesState.value = newValue
        } else {
            _trackedCitiesState.value = listOf(city)
        }
    }

    fun deleteCity(city: City) {
        viewModelScope.launch {
            cityRepository.delete(city)
        }
        val trackedCitiesValue = _trackedCitiesState.value
        if (trackedCitiesValue != null) {
            val newValue = trackedCitiesValue.toMutableList().also { it.remove(city) }
            _trackedCitiesState.value = newValue
        }
    }

    fun searchCity(
        cityName: String, numOfSuggestedResults: Int = 20, language: String = "en"
    ) {
        viewModelScope.launch {
            _citySuggestionsState.value = CitySearchViewState.Loading
            when (val citySearchResult =
                cityRepository.searchCity(cityName, numOfSuggestedResults, language)) {
                is CitySearchResult.Content -> {
                    _citySuggestionsState.value =
                        CitySearchViewState.Content(citySearchResult.cities)
                }

                is CitySearchResult.Error -> {
                    _citySuggestionsState.value =
                        CitySearchViewState.Error(citySearchResult.throwable)
                }

                is CitySearchResult.ResponseError -> {
                    _citySuggestionsState.value =
                        CitySearchViewState.Error(IllegalStateException(citySearchResult.errorBody))
                }
            }
        }
    }

    fun cleanCitiesSuggestions() {
        _citySuggestionsState.value = CitySearchViewState.Initial
    }

    companion object {
        const val TAG = "CitySearchViewModel"
    }
}