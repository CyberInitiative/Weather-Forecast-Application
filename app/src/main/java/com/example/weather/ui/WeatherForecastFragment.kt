package com.example.weather.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.iterator
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.model.City
import com.example.weather.viewmodel.ForecastViewModel
import com.example.weather.viewstate.CityForecastViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WeatherForecastFragment : Fragment() {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentWeatherForecastBinding

    private lateinit var hourlyForecastAdapter: ForecastAdapter
    private lateinit var dailyForecastAdapter: ForecastAdapter

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.settings_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.settingsMenuManageCitiesItem -> {
                    findNavController().navigate(R.id.action_weatherForecastFragment_to_citiesManagerFragment)
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(menuProvider)
        setUpRecyclerViews()
        listenForSavedCity()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeCurrentCityState()
                listenToCityForecastState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //We need option menu only for current fragment
        //so we remove it from action bar when we leave it;
        requireActivity().removeMenuProvider(menuProvider)
    }

    private fun observeCurrentCityState() {
        forecastViewModel.currentCityState.onEach { city ->
            Log.d(TAG, "currentCityLiveData observer triggered. Current city is: $city")
            city?.let {
                forecastViewModel.onNewCurrentCity(city)
                binding.weatherForecastFragmentCityNameLabel.text = city.name
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun listenToCityForecastState() {
        forecastViewModel.cityForecastState.onEach { state ->
            Log.d(TAG, "forecastViewModel.cityForecastState onEach triggered")
            when (state) {
                is CityForecastViewState.Loading -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.Loading"
                    )
                    makeAllViewsInvisibleExceptGiven(binding.progressBar)
                }

                is CityForecastViewState.NoCitiesAvailable -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.NoCitiesAvailable"
                    )
                    makeAllViewsInvisibleExceptGiven(binding.weatherForecastFragmentNoCitiesLabel)
                }

                is CityForecastViewState.Content -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.Content"
                    )
                    val dailyForecasts = state.dailyForecasts
                    val hourlyForecasts = state.hourlyForecasts
                    dailyForecastAdapter.submitList(dailyForecasts.drop(1)) {
                        binding.weatherForecastFragmentDailyForecastRecyclerView.scrollToPosition(0)
                    }
                    hourlyForecastAdapter.submitList(hourlyForecasts)
                    {
                        binding.weatherForecastFragmentHourlyForecastRecyclerView.scrollToPosition(0)
                    }

                    binding.weatherForecastFragmentCurrentDayMaxAndMinTemperature.text =
                        resources.getString(
                            R.string.min_and_max_temperature,
                            Math.round(dailyForecasts[0].temperatureMax),
                            Math.round(dailyForecasts[0].temperatureMin)
                        )
                    binding.weatherForecastFragmentCurrentDayWeatherStatus.text =
                        dailyForecastAdapter.mapWeatherCodeToWeatherStatus(
                            dailyForecasts[0].weatherCode, requireContext()
                        )

                    makeAllViewsVisibleExceptGiven(
                        binding.progressBar,
                        binding.weatherForecastFragmentNoCitiesLabel
                    )
                }

                is CityForecastViewState.Error -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.Error"
                    )
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun listenForSavedCity() {
        setFragmentResultListener(CitiesSearcherFragment.SAVED_CITY_REQUEST_KEY) { _, bundle ->

            val savedCity: City? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(CitiesSearcherFragment.SAVED_CITY_KEY, City::class.java)
            } else {
                bundle.getParcelable(CitiesSearcherFragment.SAVED_CITY_KEY)
            }

            savedCity?.let {
                forecastViewModel.setCurrentCity(it)
            }
        }
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

    private fun View.animateViewVisibility(visibilityCode: Int) {
        this.apply {
            alpha = 0f
            visibility = visibilityCode
            animate().alpha(1f).setDuration(500).setListener(null)
        }
    }

    private fun setUpRecyclerViews() {
        hourlyForecastAdapter = ForecastAdapter()
        dailyForecastAdapter = ForecastAdapter()

        setUpForecastRecyclerView(
            binding.weatherForecastFragmentHourlyForecastRecyclerView, hourlyForecastAdapter
        )

        setUpForecastRecyclerView(
            binding.weatherForecastFragmentDailyForecastRecyclerView, dailyForecastAdapter
        )
    }

    private fun setUpForecastRecyclerView(
        recyclerView: RecyclerView, forecastAdapter: ForecastAdapter
    ) {
        recyclerView.apply {
            adapter = forecastAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    companion object {
        private const val TAG = "WeatherForecastFragment"
    }
}