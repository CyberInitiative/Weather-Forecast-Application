package com.example.weather.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.HourlyForecastItem
import com.example.weather.R
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.adapter.HourlyForecastItemsAdapter
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.model.City
import com.example.weather.model.DailyForecast
import com.example.weather.model.HourlyForecast
import com.example.weather.result.ResponseResult
import com.example.weather.viewmodel.CitiesViewModel
import com.example.weather.viewmodel.ForecastViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WeatherForecastFragment : Fragment(), ForecastAdapter.OnDailyForecastItemClick {
    private val forecastViewModel: ForecastViewModel by activityViewModel()
    private val citiesViewModel: CitiesViewModel by activityViewModel()

    private lateinit var binding: FragmentWeatherForecastBinding

    private lateinit var hourlyForecastAdapter: HourlyForecastItemsAdapter
    private lateinit var dailyForecastAdapter: ForecastAdapter

    private var city: City? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerViews()

        val position = arguments?.getInt(POSITION_KEY)
        position?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                city = citiesViewModel.getTrackedCityByPosition(it)
                if (city != null) {
//                    Log.d(TAG, "${city!!.name} City's forecastResponse: ${city!!.forecastResponse}")
                    binding.weatherForecastFragmentCityNameLabel.text = city!!.name
                    if (city!!.forecastResponse != null) {
                        handleResponseResult(city!!.forecastResponse!!)
                    }
                }
            }
        }
    }

    private fun handleResponseResult(
        responseResult: ResponseResult<List<DailyForecast>>,
    ) {
        when (responseResult) {
            is ResponseResult.Error -> {
                Log.d(
                    TAG,
                    "handleResponseResult() called; responseResult is Error.\nError message: ${responseResult.message}."
                )
            }

            is ResponseResult.Exception -> {
                Log.d(
                    TAG,
                    "handleResponseResult() called; responseResult is Exception.\nException message: ${responseResult.exception}"
                )
            }

            is ResponseResult.Success -> {
                setRecyclerViewsColor()
                setUpAdapters(responseResult.data)
            }
        }
    }

    private fun setUpAdapters(dailyForecasts: List<DailyForecast>) {
        if(city != null) {
            dailyForecastAdapter.submitList(dailyForecasts)

            val hourlyForecasts = forecastViewModel.setUpHourlyForecasts(
                dailyForecasts,
                city!!.timezone ?: "Auto"
            )
            hourlyForecastAdapter.submitList(
                hourlyForecasts
            )

            binding.weatherForecastFragmentDailyForecastRecyclerView.scrollToPosition(0)
            scrollToCurrentHour()
        }
    }

    private fun setRecyclerViewsColor() {
        if(city != null) {

            if (city!!.timeOfDay == null) {
                val timeOfDay = forecastViewModel.calculateTimeOfDay(city!!)
                city!!.timeOfDay = timeOfDay
            }

            if (city!!.timeOfDay == HourlyForecast.TimeOfDay.DAY) {
                binding.weatherForecastFragmentDailyForecastRecyclerView.setBackgroundResource(
                    R.color.liberty
                )
                binding.weatherForecastFragmentHourlyForecastRecyclerView.setBackgroundResource(
                    R.color.liberty
                )
            } else {
                binding.weatherForecastFragmentDailyForecastRecyclerView.setBackgroundResource(
                    R.color.mesmerize
                )
                binding.weatherForecastFragmentHourlyForecastRecyclerView.setBackgroundResource(
                    R.color.mesmerize
                )
            }
        }

    }

    private fun View.animateViewVisibility(visibilityCode: Int) {
        this.apply {
            alpha = 0f
            visibility = visibilityCode
            animate().alpha(1f).setDuration(500).setListener(null)
        }
    }

    private fun setUpRecyclerViews() {
        hourlyForecastAdapter = HourlyForecastItemsAdapter()
        dailyForecastAdapter = ForecastAdapter(this)

        setUpRecyclerView(
            binding.weatherForecastFragmentHourlyForecastRecyclerView, hourlyForecastAdapter
        )

        setUpRecyclerView(
            binding.weatherForecastFragmentDailyForecastRecyclerView, dailyForecastAdapter
        )
    }

    private fun <T : RecyclerView.ViewHolder?> setUpRecyclerView(
        recyclerView: RecyclerView, recyclerViewAdapter: RecyclerView.Adapter<T>
    ) {
        recyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun scrollToCurrentHour() {
        val item = hourlyForecastAdapter.currentList.filterIsInstance<HourlyForecastItem.Data>()
            .firstOrNull { it.hourState == HourlyForecastItem.HourState.PRESENT }
        val index = if (item != null) {
            hourlyForecastAdapter.currentList.indexOf(item)
        } else {
            0
        }
        (binding.weatherForecastFragmentHourlyForecastRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            index,
            0
        )
    }

    private fun findHeader(item: DailyForecast) {
        val hourlyForecastItems = hourlyForecastAdapter.currentList
        val headerItem = hourlyForecastItems.filterIsInstance<HourlyForecastItem.Header>()
            .firstOrNull { it.date == item.date }
        val index = headerItem?.let {
            hourlyForecastItems.indexOf(headerItem)
        } ?: 0
        (binding
            .weatherForecastFragmentHourlyForecastRecyclerView
            .layoutManager as LinearLayoutManager)
            .scrollToPositionWithOffset(
                index,
                0
            )
        if (index == 0) {
            scrollToCurrentHour()
        }
    }

    override fun onItemClick(position: Int) {
        binding.weatherForecastFragmentHourlyForecastRecyclerView.stopScroll()
        val item = dailyForecastAdapter.currentList[position] as DailyForecast
        findHeader(item)
    }

    private fun makeAllViewsVisibleExceptGiven(vararg invisibleViews: View) {
        for (view in invisibleViews) {
            view.animateViewVisibility(View.INVISIBLE)
        }
        for (view in binding.root) {
            if (view !in invisibleViews) {
                view.animateViewVisibility(View.VISIBLE)
            }
        }
    }

    private fun makeAllViewsInvisibleExceptGiven(vararg visibleViews: View) {
        for (view in visibleViews) {
            view.animateViewVisibility(View.VISIBLE)
        }
        for (view in binding.root) {
            if (view !in visibleViews) {
                view.animateViewVisibility(View.INVISIBLE)
            }
        }
    }

    companion object {
        private const val TAG = "WeatherForecastFragment"

        private const val POSITION_KEY = "position"

        fun newInstance(position: Int): WeatherForecastFragment {
            val weatherForecastFragment = WeatherForecastFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION_KEY, position)
            weatherForecastFragment.arguments = bundle
            return weatherForecastFragment
        }
    }
}