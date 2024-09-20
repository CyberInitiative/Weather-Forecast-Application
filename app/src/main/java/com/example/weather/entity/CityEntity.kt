package com.example.weather.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weather.model.City

@Entity(tableName = "city")
data class CityEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val admin1: String?,
    val admin2: String?,
    val admin3: String?,
    val admin4: String?,
    val timezone: String? = "auto",
    var isCurrentCity: Boolean = false,
) {
    fun mapToDomain(): City {
        return City(
            this.id,
            this.name,
            this.country,
            this.latitude,
            this.longitude,
            this.admin1,
            this.admin2,
            this.admin3,
            this.admin4,
            this.timezone ?: "auto",
            this.isCurrentCity
        )
    }
}