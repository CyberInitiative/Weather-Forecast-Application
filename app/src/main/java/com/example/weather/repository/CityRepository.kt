package com.example.weather.repository

import android.util.Log
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

    private val _cityList: MutableList<City> = mutableListOf()
    val cityList: List<City> get() = _cityList

    suspend fun loadCities(
        cityName: String, numOfSuggestedResults: Int = 10, language: String = "en"
    ) {
        val citiesSearchResponse = api.getLocations(
            cityName, numOfSuggestedResults, language
        )
        if(citiesSearchResponse != null){
            Log.d(TAG, citiesSearchResponse.toString())
            _cityList.clear()
            _cityList.addAll(CityLocationMapper.buildCityLocationList(citiesSearchResponse))
            Log.d(TAG, cityList.joinToString(", "))
        }
    }

    suspend fun loadCities(): List<City> {
        return cityDao.loadAllCities()
    }

    suspend fun saveCity(city: City){
        cityDao.insert(city)
    }

    companion object {
        private const val TAG = "CityRepository"
        private const val BASE_URL = "https://geocoding-api.open-meteo.com/v1/"
    }
}