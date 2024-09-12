package com.example.weather.ui

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
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.viewmodel.ForecastViewModel
import com.example.weather.viewstate.CityForecastViewState
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

        forecastViewModel.dailyForecastLiveData.distinctUntilChanged().observe(viewLifecycleOwner) {
            Log.d(TAG, "dailyForecastLiveData observer triggered")
            when (it) {
                is CityForecastViewState.Loading -> {
                    Log.d(
                        TAG,
                        "dailyForecastLiveData observer triggered. CityForecastViewState.Loading"
                    )
                    binding.progressBar.animateViewVisibility(View.VISIBLE)
                    for (view in binding.root) {
                        if (view != binding.progressBar) {
                            view.animateViewVisibility(View.INVISIBLE)
                        }
                    }
                }

                is CityForecastViewState.NoCitiesAvailable -> {
                    Log.d(
                        TAG,
                        "dailyForecastLiveData observer triggered. CityForecastViewState.NoCitiesAvailable"
                    )
                    binding.weatherForecastFragmentNoCitiesLabel.animateViewVisibility(View.VISIBLE)
                    for (view in binding.root) {
                        if (view != binding.weatherForecastFragmentNoCitiesLabel) {
                            view.animateViewVisibility(View.INVISIBLE)
                        }
                    }
                }

                is CityForecastViewState.Content -> {
                    Log.d(
                        TAG,
                        "dailyForecastLiveData observer triggered. CityForecastViewState.Content"
                    )
                    val forecast = it.forecast
                    dailyForecastAdapter.submitList(forecast.drop(1)) {
                        binding.weatherForecastFragmentDailyForecastRecyclerView.scrollToPosition(0)
                    }
                    hourlyForecastAdapter.submitList(forecastViewModel.getForecastForNextTwentyFourHours())
                    {
                        binding.weatherForecastFragmentHourlyForecastRecyclerView.scrollToPosition(0)
                    }

                    binding.weatherForecastFragmentCurrentDayMaxAndMinTemperature.text =
                        resources.getString(
                            R.string.min_and_max_temperature,
                            Math.round(forecast[0].temperatureMax),
                            Math.round(forecast[0].temperatureMin)
                        )
                    binding.weatherForecastFragmentCurrentDayWeatherStatus.text =
                        dailyForecastAdapter.mapWeatherCodeToWeatherStatus(
                            forecast[0].weatherCode, requireContext()
                        )

                    binding.progressBar.animateViewVisibility(View.INVISIBLE)
                    binding.weatherForecastFragmentNoCitiesLabel.animateViewVisibility(View.INVISIBLE)
                    for (view in binding.root) {
                        if (view != binding.weatherForecastFragmentNoCitiesLabel
                            && view != binding.progressBar
                        ) {
                            view.animateViewVisibility(View.VISIBLE)
                        }
                    }
                }

                is CityForecastViewState.Error -> {
                    Log.d(
                        TAG,
                        "dailyForecastLiveData observer triggered. CityForecastViewState.Error"
                    )
                }

            }
        }

        forecastViewModel.currentCityLiveData.distinctUntilChanged().observe(viewLifecycleOwner) {
            Log.d(TAG, "currentCityLiveData observer triggered. Current city is: $it")
            forecastViewModel.onNewCurrentCity(it)
            binding.weatherForecastFragmentCityNameLabel.text = it.name
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //We need option menu only for current fragment
        //so we remove it from action bar when we leave it;
        requireActivity().removeMenuProvider(menuProvider)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.settings_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.settingsMenuManageCitiesItem -> {
//                navController.navigate(R.id.citiesManagerFragment)
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

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
//            addItemDecoration(
//                DividerItemDecoration(
//                    activity,
//                    DividerItemDecoration.HORIZONTAL
//                )
//            )
        }
    }

    companion object {
        private const val TAG = "WeatherForecastFragment"
    }
}