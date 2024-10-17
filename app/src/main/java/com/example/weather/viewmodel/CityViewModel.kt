package com.example.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.City
import com.example.weather.repository.ForecastRepository
import com.example.weather.result.ResponseResult
import com.example.weather.viewstate.ForecastViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CityViewModel(private val city: City, private val forecastRepository: ForecastRepository) :
    ViewModel() {

    private val _forecastViewState: MutableStateFlow<ForecastViewState> =
        MutableStateFlow(ForecastViewState.Loading)
    val forecastViewState: StateFlow<ForecastViewState> get() = _forecastViewState

    fun loadForecast() {
        viewModelScope.launch {
            when (val response = forecastRepository.loadForecast(city)) {
                is ResponseResult.Error -> {
                    _forecastViewState.value =
                        ForecastViewState.Error(IllegalStateException(response.message))
                }

                is ResponseResult.Exception -> {
                    _forecastViewState.value =
                        ForecastViewState.Error(response.exception)
                }

                is ResponseResult.Success -> {
                    _forecastViewState.value = ForecastViewState.Content(response.data)
                }
            }
        }
    }

}