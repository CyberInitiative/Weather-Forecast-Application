package com.example.weather

import com.example.weather.model.HourlyForecast

sealed class HourlyForecastItem {
    class Data(val forecast: HourlyForecast, var hourState: HourState): HourlyForecastItem()
    class Header(val date: String): HourlyForecastItem()

    enum class HourState{
        PRESENT,
        FUTURE,
        PAST
    }
}