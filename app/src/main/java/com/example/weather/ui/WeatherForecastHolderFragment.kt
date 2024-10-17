package com.example.weather.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.R
import com.example.weather.adapter.CityForecastAdapter
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.adapter.WeatherFragmentStateAdapter
import com.example.weather.databinding.FragmentWeatherForecastHolderBinding
import com.example.weather.model.City
import com.example.weather.viewmodel.CitiesViewModel
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WeatherForecastHolderFragment : Fragment(), ForecastAdapter.OnDailyForecastItemClick {
    private val forecastViewModel: ForecastViewModel by activityViewModel()
    private val citiesViewModel: CitiesViewModel by activityViewModel()

    private lateinit var binding: FragmentWeatherForecastHolderBinding

    private lateinit var weatherFragmentStateAdapter: WeatherFragmentStateAdapter
    private lateinit var cityForecastAdapter: CityForecastAdapter

    private val updateFrequencyValues = listOf("1", "2", "6", "12", "24").toTypedArray()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.settings_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.settingsMenuManageCitiesItem -> {
                    findNavController().navigate(R.id.action_weatherForecastHolderFragment_to_citiesManagerFragment)
                    true
                }

                R.id.settingsMenuTemperatureUnitItem -> {
                    showTemperatureUnitSettingsDialog()
                    true
                }

                R.id.settingsMenuUpdateFrequencyItem -> {
                    showUpdateFrequencySettingsDialog()
                    true
                }

                else -> false
            }
        }
    }

    private val viewPagerOnPageChangeCallback = object : ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            forecastViewModel.currentCityPosition = position
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherForecastHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(menuProvider)

        observeTrackedCitiesState()
        observeForecastLoadingState()
    }

    override fun onDestroyView() {
        //We need option menu only for current fragment;
        //remove when leave.
        requireActivity().removeMenuProvider(menuProvider)
        binding.weatherForecastHolderFragmentViewPager.unregisterOnPageChangeCallback(viewPagerOnPageChangeCallback)
        super.onDestroyView()
    }

    private fun setUpViewPager(cities: List<City>, itemsNumber: Int) {
        weatherFragmentStateAdapter = WeatherFragmentStateAdapter(this, itemsNumber)
        cityForecastAdapter = CityForecastAdapter(cities, this)
        binding.weatherForecastHolderFragmentViewPager.adapter = weatherFragmentStateAdapter
        binding.weatherForecastHolderFragmentViewPager.registerOnPageChangeCallback(viewPagerOnPageChangeCallback)
        binding.weatherForecastHolderFragmentViewPager.setCurrentItem(forecastViewModel.currentCityPosition, true)
    }

    private fun observeTrackedCitiesState() {
        citiesViewModel.trackedCitiesState.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && (forecastViewModel.getForecastLoadingState() != null && !forecastViewModel.getForecastLoadingState()!!)) {
//                Log.d(TAG, "single state list:\n${it.joinToString("\n")}")
                forecastViewModel.loadForecastForCities(it)

            }
        }
    }

    private fun observeForecastLoadingState(){
        forecastViewModel.forecastLoadingState.observe(viewLifecycleOwner) { state ->
            if(state != null && state != false){
                citiesViewModel.getTrackedCities()?.let {
                    setUpViewPager(it, it.size)
                }
            }
        }
    }

    private fun showTemperatureUnitSettingsDialog() {
        val items = resources.getStringArray(R.array.temperature_units)
        var selectedItem = forecastViewModel.getTemperatureUnitStateValue()
        val initialItemIndex = items.indexOf(selectedItem)

        AlertDialog.Builder(activity)
            .setTitle("Select temperature unit")
            .setSingleChoiceItems(items, initialItemIndex) { _, i ->
                selectedItem = items[i]
            }
            .setPositiveButton(android.R.string.ok) { _, _: Int ->
                forecastViewModel.saveTemperatureUnit(selectedItem)
            }
            .show()
    }

    private fun showUpdateFrequencySettingsDialog() {
        var selectedItem = forecastViewModel.getUpdateFrequencyStateValue().toString()
        val initialItemIndex = updateFrequencyValues.indexOf(selectedItem)

        AlertDialog.Builder(activity)
            .setTitle("Select forecast update frequency")
            .setSingleChoiceItems(updateFrequencyValues, initialItemIndex) { _, i ->
                selectedItem = updateFrequencyValues[i]
            }
            .setPositiveButton(android.R.string.ok) { _, _: Int ->
                forecastViewModel.saveUpdateFrequency(selectedItem.toInt())
            }
            .show()
    }

    companion object {
        private const val TAG = "WeatherForecastHolderFragment"
    }

    override fun onItemClick(position: Int) {

    }
}