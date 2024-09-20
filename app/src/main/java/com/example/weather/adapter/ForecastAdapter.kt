package com.example.weather.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.DailyForecastItemBinding
import com.example.weather.databinding.HourlyForecastItemBinding
import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.model.Forecast

class ForecastAdapter() : ListAdapter<Forecast, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOURLY_FORECAST -> {
                val binding = HourlyForecastItemBinding.inflate(inflater, parent, false)
                HourlyForecastViewHolder(
                    binding,
                    ::mapWeatherCodeToWeatherIcon,
                    ::mapWeatherCodeToWeatherStatus
                )
            }

            DAILY_FORECAST -> {
                val binding = DailyForecastItemBinding.inflate(inflater, parent, false)
                DailyForecastViewHolder(
                    binding,
                    ::mapWeatherCodeToWeatherIcon,
                    ::mapWeatherCodeToWeatherStatus
                )
            }

            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HourlyForecastViewHolder -> holder.bind(getItem(position) as Forecast.HourlyForecast)
            is DailyForecastViewHolder -> holder.bind(getItem(position) as Forecast.DailyForecast)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Forecast.HourlyForecast -> HOURLY_FORECAST
            is Forecast.DailyForecast -> DAILY_FORECAST
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    class HourlyForecastViewHolder(
        private val binding: HourlyForecastItemBinding,
        private val weatherIconMapperFun: (Int, Context) -> Drawable?,
        private val weatherStatusMapperFun: (Int, Context) -> String
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Forecast.HourlyForecast) {
            binding.hourlyForecastItemTimeLabel.text = item.time
            binding.hourlyForecastItemImageViewWeatherIcon.setImageDrawable(
                weatherIconMapperFun(
                    item.weatherCode,
                    binding.root.context
                )
            )
            binding.hourlyForecastItemWeatherCodeLabel.text =
                weatherStatusMapperFun(item.weatherCode, binding.root.context)
            binding.hourlyForecastItemTemperatureLabel.text = binding.root.resources.getString(
                R.string.temperature_label_text,
                Math.round(item.temperature)
            )
        }

    }

    class DailyForecastViewHolder(
        private val binding: DailyForecastItemBinding,
        private val weatherIconMapperFun: (Int, Context) -> Drawable?,
        private val weatherStatusMapperFun: (Int, Context) -> String
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Forecast.DailyForecast) {
            if (DateAndTimeMapper.getNextDayDate() == item.date) {
                binding.dailyForecastItemDateLabel.text =
                    binding.root.resources.getString(R.string.tomorrow_day_label)
            } else {
                binding.dailyForecastItemDateLabel.text =
                    DateAndTimeMapper.getDayOfTheWeek(item.date)
            }
            binding.dailyForecastItemMaxTemperatureLabel.text = binding.root.resources.getString(
                R.string.daily_forecast_item_max_temperature_label_text,
                Math.round(item.temperatureMax)
            )
            binding.dailyForecastItemMinTemperatureLabel.text =
                binding.root.resources.getString(
                    R.string.daily_forecast_item_min_temperature_label_text,
                    Math.round(item.temperatureMin)
                )

            binding.dailyForecastItemImageViewWeatherIcon.setImageDrawable(
                weatherIconMapperFun(
                    item.weatherCode,
                    binding.root.context
                )
            )
            binding.dailyForecastItemWeatherCodeLabel.text =
                weatherStatusMapperFun(item.weatherCode, binding.root.context)
        }

    }

    private fun mapWeatherCodeToWeatherIcon(weatherCode: Int, context: Context): Drawable? {
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

    companion object {
        const val HOURLY_FORECAST = 0
        const val DAILY_FORECAST = 1
    }

    private object DiffCallback : DiffUtil.ItemCallback<Forecast>() {
        override fun areItemsTheSame(oldItem: Forecast, newItem: Forecast): Boolean {
            return when {
                oldItem is Forecast.HourlyForecast && newItem is Forecast.HourlyForecast -> oldItem.date == newItem.date
                oldItem is Forecast.DailyForecast && newItem is Forecast.DailyForecast -> oldItem.date == newItem.date
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Forecast, newItem: Forecast): Boolean {
            return oldItem == newItem
        }
    }

}