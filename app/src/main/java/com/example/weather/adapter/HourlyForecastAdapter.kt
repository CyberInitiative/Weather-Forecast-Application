package com.example.weather.adapter

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
import com.example.weather.utils.DateAndTimeUtils

class HourlyForecastAdapter() :
    ListAdapter<HourlyForecastItem, RecyclerView.ViewHolder>(Differ) {

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

    class HeaderViewHolder(private val binding: HourlyForecastItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: HourlyForecastItem.Header) {
            val dayOfWeek = DateAndTimeUtils.getDayOfTheWeek(item.date)
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
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: HourlyForecastItem.Data) {
            binding.hourlyForecastItemTimeLabel.text = item.forecast.time
            binding.hourlyForecastItemImageViewWeatherIcon.setImageDrawable(
                item.forecast.mapWeatherCodeToWeatherIcon(
                    item.forecast.timeOfDay,
                    item.forecast.weatherCode,
                    binding.root.context
                )
            )
            binding.hourlyForecastItemWeatherCodeLabel.text =
                item.forecast.mapWeatherCodeToWeatherStatus(
                    item.forecast.weatherCode,
                    binding.root.context
                )
            binding.hourlyForecastItemTemperatureLabel.text = binding.root.resources.getString(
                R.string.temperature_label_text,
                Math.round(item.forecast.temperature)
            )

            if (item.hourState == HourlyForecastItem.Data.HourState.PRESENT) {
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