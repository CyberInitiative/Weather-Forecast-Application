package com.example.weather.model

import android.os.Parcelable
import com.example.weather.entity.CityEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class City(
    val id: Int,
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
) : Parcelable {

    @IgnoredOnParcel
    var dailyForecasts: List<Forecast.DailyForecast>? = null

    constructor(
        id: Int,
        name: String,
        country: String?,
        latitude: Double,
        longitude: Double,
        admin1: String?,
        admin2: String?,
        admin3: String?,
        admin4: String?,
        timezone: String? = "auto",
        isCurrentCity: Boolean = false,
        dailyForecasts: List<Forecast.DailyForecast>
    ) : this(
        id,
        name,
        country,
        latitude,
        longitude,
        admin1,
        admin2,
        admin3,
        admin4,
        timezone,
        isCurrentCity
    ) {
        this.dailyForecasts = dailyForecasts
    }

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
            this.timezone ?: "auto",
            this.isCurrentCity,
        )
    }
}