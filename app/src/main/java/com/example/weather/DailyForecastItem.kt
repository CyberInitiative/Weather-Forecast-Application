package com.example.weather

import com.example.weather.model.DailyForecast

data class DailyForecastItem(val data: DailyForecast, var isScrolled: Boolean = false)