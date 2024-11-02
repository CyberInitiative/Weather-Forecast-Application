package com.example.weather.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.example.weather.R

data class DailyForecast(
    val date: String,
    val weatherCode: Int,
    var temperatureMax: Double,
    var temperatureMin: Double,
    val hourlyForecasts: List<HourlyForecast>,
    var temperatureUnit: String
): Forecast(){

    fun mapWeatherCodeToWeatherIcon(weatherCode: Int, context: Context): Drawable? {
        return when (weatherCode) {
            //Clear sky
            0 -> AppCompatResources.getDrawable(context, R.drawable.sunny_day)
            //Mainly clear, partly cloudy, and overcast
            1 -> AppCompatResources.getDrawable(context, R.drawable.sun_and_blue_cloud)
            2 -> AppCompatResources.getDrawable(context, R.drawable.blue_clouds_and_sun)
            3 -> AppCompatResources.getDrawable(context, R.drawable.cloudy_weather)
            //Fog and depositing rime fog
            45 -> AppCompatResources.getDrawable(context, R.drawable.fog_weather)
            48 -> AppCompatResources.getDrawable(context, R.drawable.fog_weather)
            //Drizzle: Light, moderate, and dense intensity
            51 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            53 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            55 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            //Freezing Drizzle: Light and dense intensity
            56 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            57 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            //Rain: Slight, moderate and heavy intensity
            61 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            63 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day_and_blue_cloud)
            65 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day_and_blue_cloud)
            //Freezing Rain: Light and heavy intensity
            66 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day)
            67 -> AppCompatResources.getDrawable(context, R.drawable.rainy_day_and_blue_cloud)
            //Snow fall: Slight, moderate, and heavy intensity
            71 -> AppCompatResources.getDrawable(context, R.drawable.winter_snowfall_16473)
            73 -> AppCompatResources.getDrawable(context, R.drawable.snowy_weather)
            75 -> AppCompatResources.getDrawable(context, R.drawable.snowy_weather)
            //Snow grains
            77 -> AppCompatResources.getDrawable(context, R.drawable.winter_snowfall_16473)
            //Rain showers: Slight, moderate, and violent
            80 -> AppCompatResources.getDrawable(context, R.drawable.downpour_rain_and_blue_cloud)
            81 -> AppCompatResources.getDrawable(context, R.drawable.downpour_rain_and_blue_cloud)
            82 -> AppCompatResources.getDrawable(context, R.drawable.downpour_rain_and_blue_cloud)
            //Snow showers slight and heavy
            85 -> AppCompatResources.getDrawable(context, R.drawable.snowy_weather)
            86 -> AppCompatResources.getDrawable(context, R.drawable.snowy_weather)
            //Thunderstorm: Slight or moderate (only available in Central Europe)
            95 -> AppCompatResources.getDrawable(context, R.drawable.blue_cloud_and_lightning)
            //Thunderstorm with slight and heavy hail (only available in Central Europe)
            96 -> AppCompatResources.getDrawable(context, R.drawable.hail_and_blue_cloud)
            99 -> AppCompatResources.getDrawable(context, R.drawable.hail_and_blue_cloud)

            else -> AppCompatResources.getDrawable(context, R.drawable.ic_launcher_background)
        }
    }

}