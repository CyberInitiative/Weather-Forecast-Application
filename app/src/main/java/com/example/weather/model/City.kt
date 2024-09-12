package com.example.weather.model

import androidx.room.Entity

@Entity(tableName = "cities", primaryKeys = ["name", "latitude", "longitude"])
data class City(
    val name: String,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val admin1: String?,
    val admin2: String?,
    val admin3: String?,
    val admin4: String?,
    val timezone: String? = "auto",
    var isCurrentCity: Boolean = false
)