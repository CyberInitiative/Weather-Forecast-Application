package com.example.weather.repository

import android.util.Log
import com.example.weather.dao.CityDao
import com.example.weather.entity.CityEntity
import com.example.weather.mapper.CityLocationMapper
import com.example.weather.model.City
import com.example.weather.service.GeocodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(private val cityDao: CityDao, private val api: GeocodingService) {
    private val _loadedCities: MutableList<City> = mutableListOf()

    suspend fun search(
        cityName: String, numOfSuggestedResults: Int = 20, language: String = "en"
    ): List<City> {
        return withContext(Dispatchers.IO) {
            val citiesSearchResponse = api.getLocations(
                cityName, numOfSuggestedResults, language
            )
            if (citiesSearchResponse != null) {
                Log.d(TAG, "citiesSearchResponse != null")
                CityLocationMapper.buildCityLocationList(citiesSearchResponse)
            } else {
                emptyList()
            }
        }
    }

    suspend fun loadAll(): List<City> {
        if (_loadedCities.isEmpty()) {
            withContext(Dispatchers.IO) {
                val result = cityDao.fetchAll().map { it.mapToDomain() }
                _loadedCities.addAll(result)
            }
        }
        return _loadedCities
    }

    suspend fun save(city: City): Long {
        _loadedCities.add(city)
        return cityDao.insert(city.mapToEntity())
    }

    suspend fun delete(city: City){
        _loadedCities.remove(city)
        withContext(Dispatchers.IO) {
            cityDao.deleteByCityParameters(city.name, city.latitude, city.longitude)
        }
    }

    suspend fun setCityAsCurrent(city: City) {
        if (getCurrentCityAsEntity() == null) {
            city.isCurrentCity = true
            cityDao.updateCurrentCityStatus(
                city.isCurrentCity,
                city.name,
                city.latitude,
                city.longitude
            )
        } else {
            getCurrentCityAsEntity()?.let {
                it.isCurrentCity = false
                cityDao.update(it)

                city.isCurrentCity = true
                cityDao.updateCurrentCityStatus(
                    city.isCurrentCity,
                    city.name,
                    city.latitude,
                    city.longitude
                )
            }
        }
    }

    suspend fun getCurrentCity(): City? {
        return cityDao.fetchCurrentCity()?.mapToDomain()
    }

    suspend fun getCurrentCityAsEntity(): CityEntity? {
        return cityDao.fetchCurrentCity()
    }

//    suspend fun updateSavedCities() {
//        _loadedSavedCities.clear()
//        _loadedSavedCities.addAll(cityDao.fetchAllCities())
//    }

//    suspend fun loadSavedCities(): List<City> {
//        return if (_loadedCities.isEmpty()) {
//            val loadedCities = cityDao.fetchAllCities()
//            _loadedCities.addAll(loadedCities)
//            loadedCities
//        } else {
//            _loadedCities
//        }
//    }

//    suspend fun deleteCity(city: City){
//        cityDao.deleteCity(city)
//        _loadedSavedCities.remove(city)
//    }


//    suspend fun setCityAsCurrent(city: City) {
//        if (getCurrentCity() == null) {
//            city.isCurrentCity = true
//            cityDao.updateCity(city)
//        } else {
//            getCurrentCity()?.let {
//                it.isCurrentCity = false
//                cityDao.updateCity(it)
//
//                city.isCurrentCity = true
//                cityDao.updateCity(city)
//            }
//        }
//    }

    companion object {
        private const val TAG = "CityRepository"
    }
}