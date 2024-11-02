package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.DailyForecastItem
import com.example.weather.R
import com.example.weather.databinding.DailyForecastItemBinding
import com.example.weather.utils.DateAndTimeUtils

class DailyForecastAdapter(private val listener: OnDailyForecastItemClick) :
    ListAdapter<DailyForecastItem, DailyForecastAdapter.DailyForecastViewHolder>(DiffCallback) {

    interface OnDailyForecastItemClick {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DailyForecastItemBinding.inflate(inflater, parent, false)
        val viewHolder = DailyForecastViewHolder(
            binding
        )
        binding.root.setOnClickListener {
            listener.onItemClick(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class DailyForecastViewHolder(
        private val binding: DailyForecastItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: DailyForecastItem) {

            binding.dailyForecastItemDayOfWeekLabel.text =
                DateAndTimeUtils.getDayOfTheWeek(item.data.date)

            binding.dailyForecastDateLabel.text =
                DateAndTimeUtils.convertDateToUserLocale(item.data.date)

            binding.dailyForecastItemMaxTemperatureLabel.text = binding.root.resources.getString(
                R.string.daily_forecast_item_max_temperature_label_text,
                Math.round(item.data.temperatureMax)
            )
            binding.dailyForecastItemMinTemperatureLabel.text =
                binding.root.resources.getString(
                    R.string.daily_forecast_item_min_temperature_label_text,
                    Math.round(item.data.temperatureMin)
                )

            binding.dailyForecastItemImageViewWeatherIcon.setImageDrawable(
                item.data.mapWeatherCodeToWeatherIcon(
                    item.data.weatherCode,
                    binding.root.context
                )
            )
            binding.dailyForecastItemWeatherCodeLabel.text =
                item.data.mapWeatherCodeToWeatherStatus(item.data.weatherCode, binding.root.context)

            if (item.isScrolled) {
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

    private object DiffCallback : DiffUtil.ItemCallback<DailyForecastItem>() {
        override fun areItemsTheSame(
            oldItem: DailyForecastItem,
            newItem: DailyForecastItem
        ): Boolean {
            return oldItem.data.date == newItem.data.date
        }

        override fun areContentsTheSame(
            oldItem: DailyForecastItem,
            newItem: DailyForecastItem
        ): Boolean {
            return oldItem.data == newItem.data
        }
    }

}