package com.example.weather.model

import android.content.Context
import com.example.weather.R

abstract class Forecast {
    fun mapWeatherCodeToWeatherStatus(weatherCode: Int, context: Context): String {
        return when (weatherCode) {
            //Clear sky
            0 -> context.getString(R.string.weather_status_clear_sky)
            //Mainly clear, partly cloudy, and overcast
            1 -> context.getString(R.string.weather_status_mainly_sky)
            2 -> context.getString(R.string.weather_status_partly_cloudy)
            3 -> context.getString(R.string.weather_status_overcast)
            //Fog and depositing rime fog
            45 -> context.getString(R.string.weather_status_fog)
            48 -> context.getString(R.string.weather_status_depositing_rime_fog)
            //Drizzle: Light, moderate, and dense intensity
            51 -> context.getString(R.string.weather_status_light_drizzle)
            53 -> context.getString(R.string.weather_status_moderate_drizzle)
            55 -> context.getString(R.string.weather_status_dense_intensity_drizzle)
            //Freezing Drizzle: Light and dense intensity
            56 -> context.getString(R.string.weather_status_light_freezing_drizzle)
            57 -> context.getString(R.string.weather_status_dense_intensity_freezing_drizzle)
            //Rain: Slight, moderate and heavy intensity
            61 -> context.getString(R.string.weather_status_slight_rain)
            63 -> context.getString(R.string.weather_status_moderate_rain)
            65 -> context.getString(R.string.weather_status_heavy_intensity_rain)
            //Freezing Rain: Light and heavy intensity
            66 -> context.getString(R.string.weather_status_light_freezing_rain)
            67 -> context.getString(R.string.weather_status_heavy_intensity_freezing_rain)
            //Snow fall: Slight, moderate, and heavy intensity
            71 -> context.getString(R.string.weather_status_slight_snow_fall)
            73 -> context.getString(R.string.weather_status_moderate_snow_fall)
            75 -> context.getString(R.string.weather_status_heavy_intensity_snow_fall)
            //Snow grains
            77 -> context.getString(R.string.weather_status_snow_grains)
            //Rain showers: Slight, moderate, and violent
            80 -> context.getString(R.string.weather_status_slight_rain_showers)
            81 -> context.getString(R.string.weather_status_moderate_rain_showers)
            82 -> context.getString(R.string.weather_status_violent_rain_showers)
            //Snow showers slight and heavy
            85 -> context.getString(R.string.weather_status_slight_snow_showers)
            86 -> context.getString(R.string.weather_status_heavy_snow_showers)
            //Thunderstorm: Slight or moderate (only available in Central Europe)
            95 -> context.getString(R.string.weather_status_thunderstorm)
            //Thunderstorm with slight and heavy hail (only available in Central Europe)
            96 -> context.getString(R.string.weather_status_thunderstorm_with_slight_hail)
            99 -> context.getString(R.string.weather_status_thunderstorm_with_heavy_hail)

            else -> "Error"
        }
    }
}