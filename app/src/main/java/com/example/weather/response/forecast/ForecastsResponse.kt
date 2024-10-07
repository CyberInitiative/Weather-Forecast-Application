package com.example.weather.response.forecast

import com.google.gson.annotations.SerializedName

data class ForecastsResponse(
    @SerializedName("daily")
    val dailyResponse: DailyResponse,
    @SerializedName("daily_units")
    val dailyUnitsResponse: DailyUnitsResponse,
    @SerializedName("elevation")
    val elevation: Int,
    @SerializedName("generationtime_ms")
    val generationtimeMs: Double,
    @SerializedName("hourly")
    val hourlyResponse: HourlyResponse,
    @SerializedName("hourly_units")
    val hourlyUnitsResponse: HourlyUnitsResponse,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int
)