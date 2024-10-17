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
import com.example.weather.WeatherColorAnimator
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.adapter.HourlyForecastItemsAdapter
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.model.City
import com.example.weather.model.DailyForecast
import com.example.weather.model.HourlyForecast
import com.example.weather.result.ResponseResult
import com.example.weather.viewmodel.CitiesViewModel
import com.example.weather.viewmodel.ForecastViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
//                delay(100)
                city = citiesViewModel.getTrackedCityByPosition(it)
                if (city != null) {
//                    Log.d(TAG, "${city!!.name} City's forecastResponse: ${city!!.forecastResponse}")
                    binding.weatherForecastFragmentCityNameLabel.text = city!!.name
                    if (city!!.forecastResponse != null) {
                        handleResponseResult(city!!.forecastResponse!!, city!!.timezone ?: "Auto")
                        val timeOfDay = forecastViewModel.calculateTimeOfDay(city!!)
                        if (timeOfDay != null) {
                            if (timeOfDay == HourlyForecast.TimeOfDay.DAY) {
                                binding.weatherForecastFragmentDailyForecastRecyclerView.setBackgroundResource(R.color.liberty)
                                binding.weatherForecastFragmentHourlyForecastRecyclerView.setBackgroundResource(R.color.liberty)
                            } else {
                                binding.weatherForecastFragmentDailyForecastRecyclerView.setBackgroundResource(R.color.mesmerize)
                                binding.weatherForecastFragmentHourlyForecastRecyclerView.setBackgroundResource(R.color.mesmerize)
                            }
                        }
                    }
                }
            }
        }
//        observeTimeOfDay()
    }

    override fun onResume() {
        super.onResume()
        if (city != null) {
            Log.d(TAG, "onResume() called; city is: ${city!!.name}")
            forecastViewModel.calculateTimeOfDay(city!!)
        }
    }

    private fun handleResponseResult(
        responseResult: ResponseResult<List<DailyForecast>>,
        timeZone: String
    ) {
        when (responseResult) {
            is ResponseResult.Error -> TODO()
            is ResponseResult.Exception -> TODO()
            is ResponseResult.Success -> {
                setUp(responseResult.data, timeZone)
            }
        }
    }

    private fun setUp(dailyForecasts: List<DailyForecast>, timeZone: String) {
        dailyForecastAdapter.submitList(dailyForecasts)

        val hourlyForecasts = forecastViewModel.setUpHourlyForecasts(
            dailyForecasts,
            timeZone
        )
        hourlyForecastAdapter.submitList(
            hourlyForecasts
        )

        binding.weatherForecastFragmentDailyForecastRecyclerView.scrollToPosition(0)
        scrollToCurrentHour()

//        makeAllViewsVisibleExceptGiven(binding.progressBar, binding.weatherForecastFragmentNoCitiesLabel)
    }

    private fun observeTimeOfDay() {
        val animationDuration = 250L
        forecastViewModel.timeOfDayState.onEach { state ->
//            delay(250)
            Log.d(TAG, "animation playing")
            if (state == HourlyForecast.TimeOfDay.DAY) {
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentHourlyForecastRecyclerView,
                    resources.getColor(R.color.liberty, context?.theme),
                    animationDuration
                )
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentDailyForecastRecyclerView,
                    resources.getColor(R.color.liberty, context?.theme),
                    animationDuration
                )
            } else {
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentHourlyForecastRecyclerView,
                    resources.getColor(R.color.mesmerize, context?.theme),
                    animationDuration
                )
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentDailyForecastRecyclerView,
                    resources.getColor(R.color.mesmerize, context?.theme),
                    animationDuration
                )
            }

        }.launchIn(viewLifecycleOwner.lifecycleScope)
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