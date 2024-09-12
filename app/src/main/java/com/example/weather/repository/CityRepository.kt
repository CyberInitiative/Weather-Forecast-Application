package com.example.weather.repository

import com.example.weather.dao.CityDao
import com.example.weather.mapper.CityLocationMapper
import com.example.weather.model.City
import com.example.weather.service.GeocodingService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CityRepository(private val cityDao: CityDao) {

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingService::class.java)

    private val _searchedCities: MutableList<City> = mutableListOf()
    val searchedCities: List<City> get() = _searchedCities

    private val _loadedSavedCities: MutableList<City> = mutableListOf()

    suspend fun searchForCities(
        cityName: String, numOfSuggestedResults: Int = 10, language: String = "en"
    ) {
        val citiesSearchResponse = api.getLocations(
            cityName, numOfSuggestedResults, language
        )
        if (citiesSearchResponse != null) {
            _searchedCities.clear()
            _searchedCities.addAll(CityLocationMapper.buildCityLocationList(citiesSearchResponse))
        }
    }

    suspend fun updateSavedCities() {
        _loadedSavedCities.clear()
        _loadedSavedCities.addAll(cityDao.loadAllCities())
    }

    suspend fun loadSavedCities(): List<City> {
        return if (_loadedSavedCities.isEmpty()) {
            val loadedCities = cityDao.loadAllCities()
            _loadedSavedCities.addAll(loadedCities)
            loadedCities
        } else {
            _loadedSavedCities
        }
    }

    suspend fun saveCity(city: City) {
        cityDao.insert(city)
    }

    fun getCurrentCity(): City? {
        return _loadedSavedCities.firstOrNull { it.isCurrentCity }
    }

    suspend fun setCityAsCurrent(city: City) {
        if (getCurrentCity() == null) {
            city.isCurrentCity = true
            cityDao.update(city)
        } else {
            getCurrentCity()?.let {
                it.isCurrentCity = false
                cityDao.update(it)

                city.isCurrentCity = true
                cityDao.update(city)
            }
        }
    }

    companion object {
        private const val TAG = "CityRepository"
        private const val BASE_URL = "https://geocoding-api.open-meteo.com/v1/"
    }
}