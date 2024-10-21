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
import com.example.weather.result.ResponseResult
import com.example.weather.viewstate.ForecastsViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
    private val cityRepository: CityRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _forecastState =
        MutableStateFlow<ForecastsViewState>(ForecastsViewState.Loading)
    val forecastState: Flow<ForecastsViewState> get() = _forecastState

    private val _timeOfDayState: MutableStateFlow<HourlyForecast.TimeOfDay> =
        MutableStateFlow(HourlyForecast.TimeOfDay.DAY)
    val timeOfDayState: Flow<HourlyForecast.TimeOfDay> get() = _timeOfDayState

    private val _temperatureUnitState: MutableStateFlow<String> = MutableStateFlow("°C")
    val temperatureUnitState: Flow<String> get() = _temperatureUnitState

    private val _updateFrequencyState: MutableStateFlow<Int> = MutableStateFlow(1)
    val updateFrequency: Flow<Int> get() = _updateFrequencyState

    private val _forecastLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    val forecastLoadingState: LiveData<Boolean> get() = _forecastLoadingState

    var currentCityPosition: Int = 0

    init {
        viewModelScope.launch {
            cityRepository.loadAll()
//            Log.d(TAG, "cities size: ${cities.size}")
//            _trackedCitiesMutableLiveData.postValue(cities)
//            loadForecastForTrackedCities(cities)

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
        }
    }

    fun getForecastLoadingState(): Boolean?{
        return _forecastLoadingState.value
    }

    fun loadForecastForCity(city: City): Job {
        return viewModelScope.launch {
            val responseResult = forecastRepository.loadForecast(city)
            Log.d(TAG, "response result is: ${responseResult}")
            city.forecastResponse = responseResult
        }
    }

    fun loadForecastForCities(cities: List<City>) {
        val jobs = mutableListOf<Job>()
        viewModelScope.launch {
            for (city in cities) {
                jobs.add(loadForecastForCity(city))
            }
            jobs.joinAll()
            _forecastLoadingState.value = true
        }
    }

    suspend fun loadCities(): List<City> {
        return cityRepository.loadAll()
    }

//    private fun loadForecastForTrackedCities(cities: List<City>) {
//        val jobs = mutableListOf<Job>()
//        viewModelScope.launch {
//            for (city in cities) {
//                jobs.add(launch {
//                    forecastRepository.loadForecast(city)
//                })
//            }
//
//            jobs.joinAll()
//            _currentCityState.value = cities.find { it.isCurrentCity } ?: cities.first()
//        }
//    }

    fun loadListOfTrackedCities() {
//        viewModelScope.launch {
//            _trackedCitiesMutableLiveData.postValue(cityRepository.loadAll())
//        }
    }

//    fun loadForecastForTrackedCities() {
//        viewModelScope.launch {
//            val savedCities = cityRepository.loadAll()
//            if (savedCities.isNotEmpty()) {
//                loadForecastForTrackedCities(savedCities)
//            } else {
//                _forecastState.value = ForecastViewState.NoCitiesAvailable
//            }


    /*
    private fun handleLoadForecastForTrackedCitiesResponseResult(
        cities: List<City>,
        response: ResponseResult<Map<Pair<Double, Double>, List<DailyForecast>>>
    ): ForecastsViewState {
        return when (response) {
            is ResponseResult.Success -> {
                Log.d(TAG, "response.data size: ${response.data.size}")
                Log.d(TAG, "SUCCESS")
                for (item in response.data) {
                    val latitudeAndLongitude = item.key
                    Log.d(TAG, "handleLoadForecastForTrackedCitiesResponseResult; $latitudeAndLongitude")

                    val city =
                        cities.firstOrNull { it.latitude == latitudeAndLongitude.first && it.longitude == latitudeAndLongitude.second }
                    city?.let {
                        it.dailyForecasts = item.value
                        Log.d(TAG, "city is: $city")
                    } ?: Log.d(TAG, "city is null")

                }

                ForecastsViewState.Success(cities.size)
            }

            is ResponseResult.Error -> {
                Log.d(
                    TAG, "handleLoadForecastForTrackedCitiesResponseResult; ResponseResult.Error"
                )
                ForecastsViewState.Error(IllegalStateException(response.message))
            }

            is ResponseResult.Exception -> {
                Log.d(
                    TAG, "handleLoadForecastForTrackedCitiesResponseResult; ResponseResult.Exception" +
                            "\n${response.exception.message}"
                )
                ForecastsViewState.Error(response.exception)
            }
        }
    }

    fun loadForecastForTrackedCities() {
        Log.d(TAG, "fun loadForecastForTrackedCities()")
        viewModelScope.launch {
            val trackedCities = cityRepository.loadAll()
            if (trackedCities.isNotEmpty()) {
                Log.d(TAG, "fun loadForecastForTrackedCities(); trackedCities.isNotEmpty()")
                val responseResult = forecastRepository.loadForecasts(trackedCities)
                _forecastState.value =
                    handleLoadForecastForTrackedCitiesResponseResult(trackedCities, responseResult)
            } else {
                _forecastState.value = ForecastsViewState.NoCitiesAvailable
            }
        }
    }
     */


    fun calculateTemperatureInUnit(list: List<DailyForecast>, temperatureUnit: String): Boolean {
        if (list.first().temperatureUnit == temperatureUnit) {
            return false
        } else {
            for (item in list) {
                item.temperatureMin =
                    calculateTemperatureInUnit(item.temperatureMin, temperatureUnit)
                item.temperatureMax =
                    calculateTemperatureInUnit(item.temperatureMax, temperatureUnit)
                item.temperatureUnit = temperatureUnit
                for (hourlyItem in item.hourlyForecasts) {
                    hourlyItem.temperature =
                        calculateTemperatureInUnit(hourlyItem.temperature, temperatureUnit)
                }
            }
            return true
        }
    }

    private fun calculateTemperatureInUnit(temperature: Double, temperatureUnit: String): Double {
        return if (temperatureUnit == "°C") {
            (temperature - 32) * (5.0 / 9.0)
        } else {
            temperature * (9.0 / 5.0) + 32
        }
    }

    fun setTimeOfDay(city: City){
        city.timeOfDay?.let {
            _timeOfDayState.value = it
        }
    }

    fun calculateTimeOfDay(city: City): HourlyForecast.TimeOfDay?{
        Log.d(TAG, "city ${city.name} called calculateTimeOfDay()")
        if(city.forecastResponse != null && city.forecastResponse is ResponseResult.Success){
            val timezoneDateAndTime =
                DateAndTimeMapper.getCurrentDateAndTimeInTimezone(city.timezone ?: "Auto").split(" ")
            val currentDate = timezoneDateAndTime[0]
            val currentHour = DateAndTimeMapper.getCurrentHourInTimezone(city.timezone ?: "Auto")

//            Log.d(TAG, "currentDate: $currentDate")
//            Log.d(TAG, "currentHour: $currentHour")

            val timeOfTheDay = (city.forecastResponse as ResponseResult.Success<List<DailyForecast>>).data.firstOrNull{
                it.date == currentDate
            }?.hourlyForecasts?.firstOrNull { it.time.split(":")[0] == currentHour }?.timeOfDay!!

//            if(_timeOfDayState.value != timeOfTheDay){
//                _timeOfDayState.value = timeOfTheDay
//            }
            return timeOfTheDay
        }
        return null
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
                DateAndTimeMapper.getCurrentDateAndTimeInTimezone(timezone).split(" ")
            val currentDate = timezoneDateAndTime[0]
            val currentTime = timezoneDateAndTime[1]

            hourlyForecastItems.addAll(dailyForecast.hourlyForecasts.map {
                val timeComparisonRes = DateAndTimeMapper.compareHours(it.time, currentTime)
                val dateComparisonRes =
                    DateAndTimeMapper.compareDates(dailyForecast.date, currentDate)
                val hourState =
                    if (dateComparisonRes == 0 && timeComparisonRes == 0) {
                        HourlyForecastItem.HourState.PRESENT
                    } else if ((dateComparisonRes == 0 && timeComparisonRes == 1) || dateComparisonRes == 1) {
                        HourlyForecastItem.HourState.PAST
                    } else {
                        HourlyForecastItem.HourState.FUTURE
                    }

                HourlyForecastItem.Data(
                    it,
//                    HourlyForecastItem.HourState.FUTURE
                    hourState
                )
            })
        }

        return hourlyForecastItems
    }

    fun saveTemperatureUnit(temperatureUnit: String) {
        Log.d(
            TAG,
            "fun saveTemperatureUnit(temperatureUnit: String); temperatureUnit = $temperatureUnit"
        )
        viewModelScope.launch {
            settingsDataStore.saveTemperatureUnit(temperatureUnit)
        }
    }

    fun saveUpdateFrequency(updateFrequency: Int) {
        viewModelScope.launch {
            Log.d(
                TAG,
                "fun saveUpdateFrequency(updateFrequency: Int); updateFrequency = $updateFrequency"
            )
            settingsDataStore.saveUpdateFrequency(updateFrequency)
        }
    }

    fun getTemperatureUnitStateValue(): String {
        return _temperatureUnitState.value
    }

    fun getUpdateFrequencyStateValue(): Int {
        return _updateFrequencyState.value
    }

    companion object {
        private const val TAG = "ForecastViewModel"
    }
}