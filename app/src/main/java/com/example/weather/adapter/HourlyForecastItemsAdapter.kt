package com.example.weather.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.HourlyForecastItem
import com.example.weather.R
import com.example.weather.databinding.HourlyForecastItemBinding
import com.example.weather.databinding.HourlyForecastItemHeaderBinding
import com.example.weather.mapper.DateAndTimeMapper
import com.example.weather.model.HourlyForecast

class HourlyForecastItemsAdapter() :
    ListAdapter<HourlyForecastItem, RecyclerView.ViewHolder>(Differ) {

    class HeaderViewHolder(private val binding: HourlyForecastItemHeaderBinding) :
        ViewHolder<HourlyForecastItem.Header>(binding) {

        override fun bindTo(item: HourlyForecastItem.Header) {
            val dayOfWeek = DateAndTimeMapper.getDayOfTheWeek(item.date)
            val label = buildString {
                for (ch in dayOfWeek) {
                    append(ch)
                    if (ch != dayOfWeek.last()) {
                        append('\n')
                    }
                }
            }
            binding.hourlyForecastItemHeaderLabel.text = label
        }
    }

    class DataViewViewHolder(private val binding: HourlyForecastItemBinding) :
        ViewHolder<HourlyForecastItem.Data>(binding) {

        override fun bindTo(item: HourlyForecastItem.Data) {
            binding.hourlyForecastItemTimeLabel.text = item.forecast.time
            binding.hourlyForecastItemImageViewWeatherIcon.setImageDrawable(
                mapWeatherCodeToWeatherIcon(
                    item.forecast.timeOfDay,
                    item.forecast.weatherCode,
                    binding.root.context
                )
            )
            binding.hourlyForecastItemWeatherCodeLabel.text =
                mapWeatherCodeToWeatherStatus(
                    item.forecast.weatherCode,
                    binding.root.context
                )
            binding.hourlyForecastItemTemperatureLabel.text = binding.root.resources.getString(
                R.string.temperature_label_text,
                Math.round(item.forecast.temperature)
            )

            if (item.hourState == HourlyForecastItem.HourState.PRESENT) {
                binding.root.background = AppCompatResources.getDrawable(
                    binding.root.context,
                    R.drawable.hourly_forecast_item_data_background_present
                )
            } else {
                binding.root.background =
                    AppCompatResources.getDrawable(
                        binding.root.context,
                        android.R.color.transparent
                    )
            }
        }

        protected fun mapWeatherCodeToWeatherIcon(
            timeOfDay: HourlyForecast.TimeOfDay,
            weatherCode: Int,
            context: Context
        ): Drawable? {
            return when (weatherCode) {
                //Clear sky
                0 -> {
                    if (timeOfDay == HourlyForecast.TimeOfDay.DAY) {
                        AppCompatResources.getDrawable(context, R.drawable.sunny_day)
                    } else {
                        AppCompatResources.getDrawable(context, R.drawable.moon_and_clear_sky)
                    }
                }
                //Mainly clear, partly cloudy, and overcast
                1 -> {
                    if (timeOfDay == HourlyForecast.TimeOfDay.DAY) {
                        AppCompatResources.getDrawable(context, R.drawable.sun_and_blue_cloud)
                    } else {
                        AppCompatResources.getDrawable(context, R.drawable.moon_and_blue_cloud)
                    }
                }

                2 -> {
                    if (timeOfDay == HourlyForecast.TimeOfDay.DAY) {
                        AppCompatResources.getDrawable(context, R.drawable.blue_clouds_and_sun)
                    } else {
                        AppCompatResources.getDrawable(context, R.drawable.clouds_and_moon)
                    }
                }

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
                80 -> AppCompatResources.getDrawable(
                    context,
                    R.drawable.downpour_rain_and_blue_cloud
                )

                81 -> AppCompatResources.getDrawable(
                    context,
                    R.drawable.downpour_rain_and_blue_cloud
                )

                82 -> AppCompatResources.getDrawable(
                    context,
                    R.drawable.downpour_rain_and_blue_cloud
                )
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

        protected fun mapWeatherCodeToWeatherStatus(weatherCode: Int, context: Context): String {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_DATA -> {
                val binding = HourlyForecastItemBinding.inflate(inflater, parent, false)
                DataViewViewHolder(binding)
            }

            ITEM_TYPE_HEADER -> {
                val binding = HourlyForecastItemHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewViewHolder -> holder.bindTo(getItem(position) as HourlyForecastItem.Data)
            is HeaderViewHolder -> holder.bindTo(getItem(position) as HourlyForecastItem.Header)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HourlyForecastItem.Data -> ITEM_TYPE_DATA
            is HourlyForecastItem.Header -> ITEM_TYPE_HEADER
        }
    }

    private object Differ : DiffUtil.ItemCallback<HourlyForecastItem>() {
        override fun areItemsTheSame(
            oldItem: HourlyForecastItem,
            newItem: HourlyForecastItem
        ): Boolean {
            return when {
                oldItem is HourlyForecastItem.Data && newItem is HourlyForecastItem.Data ->
                    (oldItem.forecast.date == newItem.forecast.date
                        && oldItem.forecast.time == newItem.forecast.time)

                oldItem is HourlyForecastItem.Header && newItem is HourlyForecastItem.Header -> oldItem.date == newItem.date
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: HourlyForecastItem,
            newItem: HourlyForecastItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val ITEM_TYPE_HEADER = 0
        private const val ITEM_TYPE_DATA = 1
    }

}