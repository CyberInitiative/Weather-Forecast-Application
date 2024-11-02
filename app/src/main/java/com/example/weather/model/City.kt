package com.example.weather.model

import com.example.weather.entity.CityEntity
import com.example.weather.result.ResponseResult

data class City(
    val id: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String,
    val admin2: String,
    val admin3: String,
    val admin4: String,
    val timezone: String = "auto",
    var isCurrentCity: Boolean = false,
    var isHomeCity: Boolean = false
) {

    var responseResult: ResponseResult<List<DailyForecast>>? = null

    fun mapToEntity(): CityEntity {
        return CityEntity(
            this.id,
            this.name,
            this.country,
            this.latitude,
            this.longitude,
            this.admin1,
            this.admin2,
            this.admin3,
            this.admin4,
            this.timezone,
            this.isHomeCity,
        )
    }

    fun getFullCityLocation(): String {
        val locationBuilder = buildString {
            append(this@City.name)
            listOf(
                this@City.admin1,
                this@City.admin2,
                this@City.admin3,
                this@City.admin4,
            ).forEach {
                if(it.isEmpty()){
                    return@forEach
                } else {
                    append(", $it")
                }
            }
            if (this@City.country.isNotEmpty()) {
                append(", ${this@City.country}")
            }
        }
        return locationBuilder
    }

}