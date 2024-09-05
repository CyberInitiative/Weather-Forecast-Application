package com.example.weather.mapper

import com.example.weather.model.Forecast
import com.example.weather.service.ForecastResponse

object ForecastMapper {
    fun buildHourlyForecastItemList(forecastResponse: ForecastResponse): List<Forecast.HourlyForecast> {
        val hourlyForecastList = mutableListOf<Forecast.HourlyForecast>()

        val hourly = forecastResponse.hourlyResponse
        val temperatureList = hourly.temperature2m
        val timeList = hourly.dateAndTime
        val weatherCodeList = hourly.weatherCode

        for (index in timeList.indices) {
            val hourlyForecastItem =
                buildHourlyForecastItem(
                    temperatureList[index],
                    timeList[index],
                    weatherCodeList[index]
                )
            hourlyForecastList.add(hourlyForecastItem)
        }

        return hourlyForecastList
    }

    fun buildDailyForecastItemList(forecastResponse: ForecastResponse): List<Forecast.DailyForecast> {
        val dailyForecastList = mutableListOf<Forecast.DailyForecast>()

        val daily = forecastResponse.dailyResponse
        val dateList = daily.date
        val weatherCodeList = daily.weatherCode

        for (index in dateList.indices) {
            val dailyForecastItem =
                buildDailyForecastItem(dateList[index], weatherCodeList[index])
            dailyForecastList.add(dailyForecastItem)
        }

        return dailyForecastList
    }

    private fun buildDailyForecastItem(
        date: String,
        weatherCode: Int
    ): Forecast.DailyForecast {
        val weather = mapCodeToWeather(weatherCode)
        return Forecast.DailyForecast(date, weather)
    }

    private fun buildHourlyForecastItem(
        temperature: Double,
        dateAndTime: String,
        weatherCode: Int
    ): Forecast.HourlyForecast {
        val weather = mapCodeToWeather(weatherCode)
        val dateAndTimeSplit = DateAndTimeMapper.splitDateAndTime(dateAndTime)
        return Forecast.HourlyForecast(
            dateAndTimeSplit.first,
            dateAndTimeSplit.second,
            weather,
            temperature
        )
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
            80 -> "Slight\nrain showers"
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