package com.example.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.repository.CityRepository
import com.example.weather.result.ResponseResult
import com.example.weather.viewstate.CitySearchViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CitySearchViewModel(private val cityRepository: CityRepository) : ViewModel() {

    private val _citySuggestionsState =
        MutableStateFlow<CitySearchViewState>(CitySearchViewState.Initial)
    val citySuggestionsState: Flow<CitySearchViewState> get() = _citySuggestionsState

    fun searchCity(
        cityName: String, numOfSuggestedResults: Int = 20, language: String = "en"
    ) {
        viewModelScope.launch {
            _citySuggestionsState.value = CitySearchViewState.Loading
            when (val citySearchResult =
                cityRepository.searchCity(cityName, numOfSuggestedResults, language)) {
                is ResponseResult.Success -> {
                    _citySuggestionsState.value = CitySearchViewState.Content(citySearchResult.data)
                }

                is ResponseResult.Exception -> {
                    _citySuggestionsState.value = CitySearchViewState.Error(citySearchResult.exception)
                }

                is ResponseResult.Error -> {
                    _citySuggestionsState.value =
                        CitySearchViewState.Error(IllegalStateException("Error code: ${citySearchResult.code}; Error message: ${citySearchResult.message}"))
                }
            }
        }
    }

    fun cleanCitiesSuggestions() {
        _citySuggestionsState.value = CitySearchViewState.Initial
    }
}