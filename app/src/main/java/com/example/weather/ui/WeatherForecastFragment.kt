package com.example.weather.ui

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
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
import com.example.weather.viewmodel.ForecastViewModel
import com.example.weather.viewstate.ForecastViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs


class WeatherForecastFragment : Fragment(), ForecastAdapter.OnDailyForecastItemClick {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentWeatherForecastBinding

    private lateinit var hourlyForecastAdapter: HourlyForecastItemsAdapter
    private lateinit var dailyForecastAdapter: ForecastAdapter

    private lateinit var temperatureUnit: String
    private var updateFrequency: Int = 1

    private val maxDistance = 255f

    private var originalXPosition: Float = 0f
    private var dX: Float = 0f
    private var isAnimationRunning = false
    private var isMovingWhenAnimationRunning = false

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

                R.id.settingsMenuTemperatureUnitItem -> {
                    showTemperatureDialog()
                    true
                }

                R.id.settingsMenuUpdateFrequencyItem -> {
                    showUpdateFrequencyDialog()
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
        setTouchRootTouchListener()

        setUpOriginalXPositions()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeCurrentCityState()
                observeCityForecastState()
                observeTimeOfDay()
                observeTemperatureUnitState()
                observeUpdateFrequencyState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //We need option menu only for current fragment
        //so we remove it from action bar when we leave it;
        requireActivity().removeMenuProvider(menuProvider)
    }

    private fun setUpOriginalXPositions() {
        originalXPosition = binding.root.x
    }

    private fun cancelAllAnimations() {
        for (view in binding.root) {
            view.animate().cancel()
        }
    }

    private fun setTouchRootTouchListener() {
        binding.root.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {

                        if (isAnimationRunning) {
                            binding.root.animate().cancel()
                            isAnimationRunning = false
                        }

                        dX = binding.root.x - event.rawX
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (isAnimationRunning) {
                            isMovingWhenAnimationRunning = true
                        } else {
                            isMovingWhenAnimationRunning = false
                        }

                        if(isMovingWhenAnimationRunning){
                            dX = originalXPosition - event.rawX
                        }

                        val newX = event.rawX + dX
                        binding.root.x = newX

                        val distanceFromStart =
                            abs(newX - originalXPosition).coerceAtMost(maxDistance)

                        val alpha = 1 - (distanceFromStart / maxDistance).coerceIn(0f, 1f)
                        binding.root.alpha = alpha

                        if (distanceFromStart >= maxDistance) {
                            val direction = if (newX > originalXPosition) 1 else -1
                            Log.d(TAG, "maxDistance reached. Direction is: $direction")

                            forecastViewModel.setNextToSwipedCityAsCurrentCity(direction)
                            newDataAppearanceAnimation(binding.root, direction)
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isMovingWhenAnimationRunning = false
                        if (!isAnimationRunning) {
                            if (abs(binding.root.x - originalXPosition) <= maxDistance) {
                                animateReturn(binding.root, originalXPosition)
                            }
                        }
                    }
                }
                return true
            }

        })
    }

    private fun newDataAppearanceAnimation(view: View, direction: Int) {
        val oppositeX = originalXPosition + (if (direction > 0) -maxDistance else maxDistance)
        view.x = oppositeX
        view.alpha = 0f

        isAnimationRunning = true
        view.animate()
            .alpha(1f)
            .x(originalXPosition)
            .setDuration(600)
            .withEndAction {
                isAnimationRunning = false
            }
            .start()
    }

    private fun animateReturn(view: View, originalX: Float) {
        val animator = ValueAnimator.ofFloat(view.x, originalX)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val currentX = animation.animatedValue as Float
            view.x = currentX

            val distanceFromStart = abs(currentX - originalX)

            val alpha = 1 - (distanceFromStart / maxDistance).coerceIn(0f, 1f)
            view.alpha = alpha
        }
        animator.start()
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

    private fun observeTimeOfDay() {
        forecastViewModel.timeOfDayState.onEach { state ->
            if (state == HourlyForecast.TimeOfDay.DAY) {
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentHourlyForecastRecyclerView,
                    resources.getColor(R.color.liberty, context?.theme),
                    250
                )
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentDailyForecastRecyclerView,
                    resources.getColor(R.color.liberty, context?.theme),
                    250
                )
            } else {
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentHourlyForecastRecyclerView,
                    resources.getColor(R.color.mesmerize, context?.theme),
                    250
                )
                WeatherColorAnimator.animateColorChange(
                    binding.weatherForecastFragmentDailyForecastRecyclerView,
                    resources.getColor(R.color.mesmerize, context?.theme),
                    250
                )
            }

        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeTemperatureUnitState() {
        forecastViewModel.temperatureUnitState.onEach { state ->
            temperatureUnit = state
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeUpdateFrequencyState() {
        forecastViewModel.updateFrequency.onEach { state ->
            updateFrequency = state
            Log.d(TAG, "Update frequency value changed to $state")
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showTemperatureDialog() {
        val items = resources.getStringArray(R.array.temperature_units)
        var selectedItem = temperatureUnit
        val initialItemIndex = items.indexOf(selectedItem)

        AlertDialog.Builder(activity)
            .setTitle("Select temperature unit")
            .setSingleChoiceItems(items, initialItemIndex) { _, i ->
                selectedItem = items[i]
            }
            .setPositiveButton(android.R.string.ok) { _, _: Int ->
                forecastViewModel.saveTemperatureUnit(selectedItem)
                if (forecastViewModel.calculateTemperatureInGivenUnit(
                        dailyForecastAdapter.currentList,
                        selectedItem
                    )
                ) {
//                    dailyForecastAdapter.submitList(dailyForecastAdapter.currentList)
//                    hourlyForecastAdapter.submitList(hourlyForecastAdapter.currentList)
                    dailyForecastAdapter.notifyDataSetChanged()
                    hourlyForecastAdapter.notifyDataSetChanged()
                }
            }
            .show()
    }

    private fun showUpdateFrequencyDialog() {
        val items = listOf("1", "2", "6", "12", "24").toTypedArray()
        var selectedItem = updateFrequency.toString()
        val initialItemIndex = items.indexOf(selectedItem)

        AlertDialog.Builder(activity)
            .setTitle("Select weather update frequency")
            .setSingleChoiceItems(items, initialItemIndex) { _, i ->
                selectedItem = items[i]
            }
            .setPositiveButton(android.R.string.ok) { _, _: Int ->
                forecastViewModel.saveUpdateFrequency(selectedItem.toInt())
                Log.d(
                    TAG,
                    "showUpdateFrequencyDialog() positive answer; value: ${selectedItem.toInt()}"
                )
            }
            .show()
    }

    private fun observeCityForecastState() {
        forecastViewModel.forecastState.onEach { state ->
            Log.d(TAG, "forecastViewModel.cityForecastState onEach triggered")
            when (state) {
                is ForecastViewState.Loading -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.Loading"
                    )
                    makeAllViewsInvisibleExceptGiven(binding.progressBar)
                }

                is ForecastViewState.NoCitiesAvailable -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.NoCitiesAvailable"
                    )
                    makeAllViewsInvisibleExceptGiven(binding.weatherForecastFragmentNoCitiesLabel)
                }

                is ForecastViewState.Content -> {
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.Content"
                    )
                    val dailyForecasts = state.dailyForecasts
//                    val hourlyForecasts = state.hourlyForecasts
                    dailyForecastAdapter.submitList(dailyForecasts) {
                        binding.weatherForecastFragmentDailyForecastRecyclerView.scrollToPosition(0)
                    }
                    val timeZone = forecastViewModel.getCurrentCity()?.timezone ?: "Auto"
                    hourlyForecastAdapter.submitList(
                        forecastViewModel.setUpHourlyForecasts(
                            state.dailyForecasts,
                            timeZone
                        )
                    )
                    {
                        scrollToCurrentHour()
                    }

//                    binding.weatherForecastFragmentCurrentDayMaxAndMinTemperature.text =
//                        resources.getString(
//                            R.string.min_and_max_temperature,
//                            Math.round(dailyForecasts[0].temperatureMax),
//                            Math.round(dailyForecasts[0].temperatureMin)
//                        )
//                    binding.weatherForecastFragmentCurrentDayWeatherStatus.text =
//                        dailyForecastAdapter.mapWeatherCodeToWeatherStatus(
//                            dailyForecasts[0].weatherCode, requireContext()
//                        )

                    makeAllViewsVisibleExceptGiven(
                        binding.progressBar,
                        binding.weatherForecastFragmentNoCitiesLabel
                    )
                }

                is ForecastViewState.Error -> {
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

    companion object {
        private const val TAG = "WeatherForecastFragment"
    }
}