package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.HourlyForecastItem
import com.example.weather.SettingsDataStore
import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.model.City
import com.example.weather.model.DailyForecast
import com.example.weather.model.HourlyForecast
import com.example.weather.repository.CityRepository
import com.example.weather.repository.ForecastRepository
import com.example.weather.result.ForecastResult
import com.example.weather.viewstate.ForecastViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
    private val cityRepository: CityRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _forecastState =
        MutableStateFlow<ForecastViewState>(ForecastViewState.Loading)
    val forecastState: Flow<ForecastViewState> get() = _forecastState

    private val _trackedCitiesMutableLiveData = MutableLiveData<List<City>>(emptyList())
    val trackedCitiesLiveData: LiveData<List<City>> get() = _trackedCitiesMutableLiveData

    private val _currentCityState = MutableStateFlow<City?>(null)
    val currentCityState: Flow<City?> get() = _currentCityState

    private val _timeOfDayState: MutableStateFlow<HourlyForecast.TimeOfDay> =
        MutableStateFlow(HourlyForecast.TimeOfDay.DAY)
    val timeOfDayState: Flow<HourlyForecast.TimeOfDay> get() = _timeOfDayState

    private val _temperatureUnitState: MutableStateFlow<String> = MutableStateFlow("°C")
    val temperatureUnitState: Flow<String> get() = _temperatureUnitState

    private val _updateFrequencyState: MutableStateFlow<Int> = MutableStateFlow(1)
    val updateFrequency: Flow<Int> get() = _updateFrequencyState

    init {
        viewModelScope.launch {
//            loadListOfTrackedCities()
            
            launch {
                settingsDataStore.temperatureUnit.collect {
                    Log.d(TAG, "temperatureUnit value from dataStore: $it")
                    _temperatureUnitState.value = it
                }
            }
            launch {
                settingsDataStore.updateFrequency.collect {
                    Log.d(TAG, "updateFrequency value from dataStore: $it")
                    _updateFrequencyState.value = it
                }
            }

            loadForecastForTrackedCities()
        }
    }

    fun setCurrentCity(city: City) {
        viewModelScope.launch {
            Log.d(TAG, "New current city is: $city")
            if (!city.isCurrentCity) {
                cityRepository.setCityAsCurrent(city)
            }
            _currentCityState.value = city
        }
    }

    /**
     * @param direction the direction in which user swiped. Set to -1 if swiping is from right to left, and 1 if swiping is from left to right.
     */
    fun setNextToSwipedCityAsCurrentCity(direction: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val value = _trackedCitiesMutableLiveData.value
            if (value != null) {
                val currentIndex = value.indexOf(_currentCityState.value)
                if (currentIndex == -1) {
                    _currentCityState.value = value.first()
                } else {
                    //Swiped from right to left
                    if (direction == -1) {
                        if (currentIndex == value.lastIndex) {
                            _currentCityState.value = value.first()
                        } else {
                            _currentCityState.value = value[currentIndex + 1]
                        }
                    //Swiped from left to right
                    } else {
                        if (currentIndex == 0) {
                            _currentCityState.value = value.last()
                        } else {
                            _currentCityState.value = value[currentIndex - 1]
                        }
                    }
                }
            }
        }
    }

    fun onNewCurrentCity(city: City) {
        Log.d(TAG, "onNewCurrentCity() called")
        _forecastState.value = ForecastViewState.Loading
        viewModelScope.launch {
            when (val dailyForecastResult = forecastRepository.loadForecast(city)) {
                is ForecastResult.Content -> {
                    Log.d(
                        TAG,
                        "onNewCurrentCity(); dailyForecastResult is ForecastResult.Content"
                    )
                    calculateTemperatureInGivenUnit(
                        dailyForecastResult.dailyForecasts,
                        _temperatureUnitState.value
                    )
                    _forecastState.value =
                        ForecastViewState.Content(
                            dailyForecastResult.dailyForecasts,
                            getHourlyForecasts(dailyForecastResult.dailyForecasts)
                        )
                }

                is ForecastResult.Error -> {
                    Log.d(TAG, "onNewCurrentCity(); ForecastResult.Error")
                    _forecastState.value = ForecastViewState.Error(
                        dailyForecastResult.throwable
                    )

                    Log.d(TAG, dailyForecastResult.throwable.toString())
                }

                is ForecastResult.ResponseError -> {
                    Log.d(TAG, "onNewCurrentCity(); ForecastResult.ResponseError")
                    _forecastState.value = ForecastViewState.Error(
                        IllegalStateException(dailyForecastResult.errorBody)
                    )
                }
            }

        }
    }

    private fun loadForecastForTrackedCities(cities: List<City>) {
        val jobs = mutableListOf<Job>()
        viewModelScope.launch {
            for (city in cities) {
                jobs.add(launch {
                    forecastRepository.loadForecast(city)
                })
            }

            _currentCityState.value = cities.find { it.isCurrentCity } ?: cities.first()
        }
    }

    fun loadForecastForTrackedCities() {
        viewModelScope.launch {
            val trackedCities = cityRepository.loadAll()
            if (!trackedCities.isNullOrEmpty()) {
                _trackedCitiesMutableLiveData.postValue(trackedCities)
                loadForecastForTrackedCities(trackedCities)
            } else {
                _forecastState.value = ForecastViewState.NoCitiesAvailable
            }
        }
    }

    fun loadListOfTrackedCities() {
        viewModelScope.launch {
            _trackedCitiesMutableLiveData.postValue(cityRepository.loadAll())
        }
    }

    fun calculateTemperatureInGivenUnit(list: List<DailyForecast>, temperatureUnit: String): Boolean {
        if (list.first().temperatureUnit == temperatureUnit) {
            return false
        } else {
            for (item in list) {
                item.temperatureMin =
                    calculateTemperatureInGivenUnit(item.temperatureMin, temperatureUnit)
                item.temperatureMax =
                    calculateTemperatureInGivenUnit(item.temperatureMax, temperatureUnit)
                item.temperatureUnit = temperatureUnit
                for (hourlyItem in item.hourlyForecasts) {
                    hourlyItem.temperature =
                        calculateTemperatureInGivenUnit(hourlyItem.temperature, temperatureUnit)
                }
            }
            return true
        }
    }

    private fun calculateTemperatureInGivenUnit(temperature: Double, temperatureUnit: String): Double {
        return if (temperatureUnit == "°C") {
            (temperature - 32) * (5.0 / 9.0)
        } else {
            temperature * (9.0 / 5.0) + 32
        }
    }

    fun deleteTrackedCity(city: City) {
        viewModelScope.launch {
            cityRepository.delete(city)

            val trackedCities = _trackedCitiesMutableLiveData.value

            if(!trackedCities.isNullOrEmpty()) {
                if (city.isCurrentCity) {
                    setCurrentCity(cityRepository.loadAll().first())
                }
                _trackedCitiesMutableLiveData.postValue(trackedCities!!)
            }
        }
    }

    fun setUpHourlyForecasts(
        dailyForecasts: List<DailyForecast>,
        timezone: String = "Auto"
    ): List<HourlyForecastItem> {
        val hourlyForecastItems: MutableList<HourlyForecastItem> = mutableListOf()

        for (index in dailyForecasts.indices) {
            val dailyForecast = dailyForecasts[index]
            if (index != 0) {
                hourlyForecastItems.add(
                    HourlyForecastItem.Header(
                        dailyForecast.date
                    )
                )
            }

            val timezoneDateAndTime =
                DateAndTimeMapper.getDateAndTimeInTimezone(timezone).split(" ")
            val currentDate = timezoneDateAndTime[0]
            val currentTime = timezoneDateAndTime[1]

            hourlyForecastItems.addAll(dailyForecast.hourlyForecasts.map {
                val timeComparisonRes = DateAndTimeMapper.compareHours(it.time, currentTime)
                val dateComparisonRes =
                    DateAndTimeMapper.compareDates(dailyForecast.date, currentDate)
                val hourState =
                    if (dateComparisonRes == 0 && timeComparisonRes == 0) {
                        _timeOfDayState.value = it.timeOfDay
                        HourlyForecastItem.HourState.PRESENT
                    } else if ((dateComparisonRes == 0 && timeComparisonRes == 1) || dateComparisonRes == 1) {
                        HourlyForecastItem.HourState.PAST
                    } else {
                        HourlyForecastItem.HourState.FUTURE
                    }

                HourlyForecastItem.Data(
                    it,
                    hourState
                )
            })
        }

        return hourlyForecastItems
    }

    fun getCurrentCity(): City? {
        return _currentCityState.value
    }

    fun getHourlyForecasts(dailyForecasts: List<DailyForecast>): List<HourlyForecast> {
        val resultForecastList = mutableListOf<HourlyForecast>()

        for (dailyForecast in dailyForecasts) {
            resultForecastList.addAll(dailyForecast.hourlyForecasts!!)
        }

        return resultForecastList
    }

    fun saveTemperatureUnit(temperatureUnit: String) {
        viewModelScope.launch {
            settingsDataStore.saveTemperatureUnit(temperatureUnit)
        }
    }

    fun saveUpdateFrequency(updateFrequency: Int) {
        viewModelScope.launch {
            Log.d(TAG, "saveUpdateFrequency(updateFrequency: Int); value: $updateFrequency")
            settingsDataStore.saveUpdateFrequency(updateFrequency)
        }
    }

    companion object {
        private const val TAG = "ForecastViewModel"
    }
}