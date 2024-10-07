package com.example.weather.repository

import android.util.Log
import com.example.weather.dao.CityDao
import com.example.weather.entity.CityEntity
import com.example.weather.mapper.CityLocationMapper
import com.example.weather.model.City
import com.example.weather.result.CitySearchResult
import com.example.weather.service.GeocodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(private val cityDao: CityDao, private val api: GeocodingService) {
    private val _loadedCities: MutableList<City> = mutableListOf()

    suspend fun searchCity(
        cityName: String, numOfSuggestedResults: Int = 20, language: String = "en"
    ): CitySearchResult {
        return withContext(Dispatchers.IO) {
            try {
                val citiesSearchResponse = api.getLocations(
                    cityName, numOfSuggestedResults, language
                )

                if(citiesSearchResponse.isSuccessful) {
                    val data = citiesSearchResponse.body()
                    if(data != null){
                        val mappedResponse = CityLocationMapper.buildCityLocationList(data)
                        CitySearchResult.Content(mappedResponse)
                    } else {
                        CitySearchResult.Error(NullPointerException("search(); Response body is null!"))
                    }

                } else {
                    CitySearchResult.ResponseError(
                        citiesSearchResponse.errorBody()?.toString() ?: "search(); Response error!"
                    )
                }
            } catch (ex: Exception){
                CitySearchResult.Error(ex)
            }
        }
    }

    suspend fun loadAll(): List<City> {
        if (_loadedCities.isEmpty()) {
            withContext(Dispatchers.IO) {
                val result = cityDao.fetchAll().map { it.mapToDomain() }
                Log.d(TAG, "loadAll(); Result: ${_loadedCities.joinToString("\n")}")
                _loadedCities.addAll(result)
            }
        }
        Log.d(TAG, "loadAll(); Return result: ${_loadedCities.joinToString("\n")}")

        return _loadedCities
    }

    suspend fun save(city: City): Long {
        _loadedCities.add(city)
        return cityDao.insert(city.mapToEntity())
    }

    suspend fun delete(city: City) {
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

    companion object {
        private const val TAG = "CityRepository"
    }
}