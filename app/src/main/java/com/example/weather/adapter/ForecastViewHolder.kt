package com.example.weather.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ForecastItemBinding
import com.example.weather.model.HourlyForecast

class ForecastViewHolder(private val binding: ForecastItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: HourlyForecast) {
        binding.forecastItemDateLabel.text = item.time
        binding.forecastItemWeatherCodeLabel.text = item.weather
        binding.forecastItemTemperatureLabel.text = item.temperature.toString()
    }
}