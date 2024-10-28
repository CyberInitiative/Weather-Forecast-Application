package com.example.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.repository.CityRepository
import com.example.weather.result.CitySearchResult
import com.example.weather.viewstate.CitySearchViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CitySearchViewModel(private val cityRepository: CityRepository) : ViewModel() {

    private val _citySuggestionsState =
        MutableStateFlow<CitySearchViewState>(CitySearchViewState.Initial)
    val citySuggestionsState: Flow<CitySearchViewState> get() = _citySuggestionsState

//    fun saveCity(city: City) {
//        viewModelScope.launch(Dispatchers.IO) {
//            if (cityRepository.getHomeCity() == null) {
//                city.isCurrentCity = true
//            }
//            cityRepository.save(city)
//        }
//    }

    fun searchCity(
        cityName: String, numOfSuggestedResults: Int = 20, language: String = "en"
    ) {
        viewModelScope.launch {
            _citySuggestionsState.value = CitySearchViewState.Loading
            when (val citySearchResult =
                cityRepository.searchCity(cityName, numOfSuggestedResults, language)) {
                is CitySearchResult.Content -> {
                    _citySuggestionsState.value = CitySearchViewState.Content(citySearchResult.cities)
                }

                is CitySearchResult.Error -> {
                    _citySuggestionsState.value = CitySearchViewState.Error(citySearchResult.throwable)
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
}