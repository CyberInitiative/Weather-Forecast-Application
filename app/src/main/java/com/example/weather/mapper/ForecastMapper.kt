package com.example.weather.mapper

import com.example.weather.service.ForecastResponse
import com.example.weather.model.HourlyForecast

object ForecastMapper {
    fun buildForecastItemList(forecastResponse: ForecastResponse): List<HourlyForecast> {
        val hourlyForecastList = mutableListOf<HourlyForecast>()

        val hourly = forecastResponse.hourlyResponse
        val temperatureList = hourly.temperature2m
        val timeList = hourly.time
        val weatherCodeList = hourly.weatherCode

        for (index in temperatureList.indices) {
            val forecastItem =
                buildForecastItem(temperatureList[index], timeList[index], weatherCodeList[index])
            hourlyForecastList.add(forecastItem)
        }

        return hourlyForecastList
    }

    private fun buildForecastItem(
        temperature: Double,
        time: String,
        weatherCode: Int
    ): HourlyForecast {
        val weather = mapCodeToWeather(weatherCode)
        return HourlyForecast(temperature, time, weather)
    }

    private fun mapCodeToWeather(weatherCode: Int): String {
        return when (weatherCode) {
            //Clear sky
            0 -> "Clear sky"
            //Mainly clear, partly cloudy, and overcast
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            //Fog and depositing rime fog
            45 -> "Fog"
            48 -> "Depositing rime fog"
            //Drizzle: Light, moderate, and dense intensity
            51 -> "Light drizzle"
            53 -> "Moderate drizzle"
            55 -> "Dense intensity drizzle"
            //Freezing Drizzle: Light and dense intensity
            56 -> "Light freezing drizzle"
            57 -> "Dense intensity freezing drizzle"
            //Rain: Slight, moderate and heavy intensity
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy intensity rain"
            //Freezing Rain: Light and heavy intensity
            66 -> "Light freezing rain"
            67 -> "Heavy intensity freezing rain"
            //Snow fall: Slight, moderate, and heavy intensity
            71 -> "Slight snow fall"
            73 -> "Moderate snow fall"
            75 -> "Heavy intensity snow fall"
            //Snow grains
            77 -> "Snow grains"
            //Rain showers: Slight, moderate, and violent
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Violent rain showers"
            //Snow showers slight and heavy
            85 -> "Slight snow showers"
            86 -> "Heavy snow showers"
            //Thunderstorm: Slight or moderate (only available in Central Europe)
            95 -> "Thunderstorm"
            //Thunderstorm with slight and heavy hail (only available in Central Europe)
            96 -> "Thunderstorm with slight hail"
            99 -> "Thunderstorm with heavy hail"

            else -> "Error"
        }
    }
}