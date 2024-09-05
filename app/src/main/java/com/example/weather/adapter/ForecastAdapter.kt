package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.DailyForecastItemBinding
import com.example.weather.databinding.HourlyForecastItemBinding
import com.example.weather.model.Forecast

class ForecastAdapter :
    ListAdapter<Forecast, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOURLY_FORECAST -> {
                val binding = HourlyForecastItemBinding.inflate(inflater, parent, false)
                HourlyForecastViewHolder(binding)
            }

            DAILY_FORECAST -> {
                val binding = DailyForecastItemBinding.inflate(inflater, parent, false)
                DailyForecastViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid item type")
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

    class HourlyForecastViewHolder(private val binding: HourlyForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Forecast.HourlyForecast) {
            binding.hourlyForecastItemDateLabel.text = item.date
            binding.hourlyForecastItemTimeLabel.text = item.time
            binding.hourlyForecastItemWeatherCodeLabel.text = item.weather
            binding.hourlyForecastItemTemperatureLabel.text = item.temperature.toString()
        }
    }

    class DailyForecastViewHolder(private val binding: DailyForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                Toast.makeText(
                    binding.root.context,
                    binding.dailyForecastItemDateLabel.text,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        fun bind(item: Forecast.DailyForecast) {
            binding.dailyForecastItemDateLabel.text = item.date
            binding.dailyForecastItemWeatherCodeLabel.text = item.weather
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