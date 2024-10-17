package com.example.weather.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.HourlyForecastItem
import com.example.weather.adapter.ForecastAdapter.OnDailyForecastItemClick
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.model.City
import com.example.weather.model.DailyForecast
import com.example.weather.result.ResponseResult

class CityForecastAdapter(
    private var cities: List<City>,
    private val listener: OnDailyForecastItemClick
) : RecyclerView.Adapter<CityForecastAdapter.CityLayoutViewHolder>() {

    inner class CityLayoutViewHolder(private val binding: FragmentWeatherForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(city: City) {
            Log.d(TAG, "bindTo() called;")
            binding.weatherForecastFragmentCityNameLabel.text = city.name
            handleForecastResponseResult(city.forecastResponse)
        }

        private fun handleForecastResponseResult(responseResult: ResponseResult<List<DailyForecast>>?) {
            when (responseResult) {
                null -> {
                    Log.d(
                        VIEWHOLDER_TAG,
                        "handleForecastResponseResult() called; responseResult is null"
                    )
                }

                is ResponseResult.Error -> {
                    Log.d(
                        VIEWHOLDER_TAG,
                        "handleForecastResponseResult() called; responseResult is Error.\nError message: ${responseResult.message}."
                    )
                }

                is ResponseResult.Exception -> {
                    Log.d(
                        VIEWHOLDER_TAG,
                        "handleForecastResponseResult() called; responseResult is Exception.\nException message: ${responseResult.exception}"
                    )
                }

                is ResponseResult.Success -> {
                    binding.weatherForecastFragmentDailyForecastRecyclerView.apply {
                        adapter =
                            ForecastAdapter(listener).apply {
                                this.submitList(responseResult.data)
                            }

                        layoutManager = LinearLayoutManager(
                            itemView.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }

                    binding.weatherForecastFragmentHourlyForecastRecyclerView.apply {
                        adapter =
                            HourlyForecastItemsAdapter().apply {
                                this.submitList(buildHourlyForecastItems(responseResult.data))
                            }
                        layoutManager = LinearLayoutManager(
                            itemView.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }

                }
            }
        }

        private fun buildHourlyForecastItems(dailyForecasts: List<DailyForecast>): List<HourlyForecastItem> {
            val hourlyForecastItems: MutableList<HourlyForecastItem> = mutableListOf()

            for (index in dailyForecasts.indices) {
                val dailyForecast = dailyForecasts[index]
                if (index != 0) {
                    hourlyForecastItems.add(
                        HourlyForecastItem.Header(
                            dailyForecast.date
                        )
                    )
                }

                hourlyForecastItems.addAll(dailyForecast.hourlyForecasts.map {
                    HourlyForecastItem.Data(
                        it,
                        HourlyForecastItem.HourState.FUTURE
                    )
                })
            }
            return hourlyForecastItems
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityLayoutViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentWeatherForecastBinding.inflate(inflater, parent, false)
        return CityLayoutViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    override fun onBindViewHolder(holder: CityLayoutViewHolder, position: Int) {
        holder.bindTo(cities[position])
    }

    companion object {
        private const val TAG = "CityForecastAdapter"
        private const val VIEWHOLDER_TAG = "CityForecastAdapter.CityLayoutViewHolder"
    }

}