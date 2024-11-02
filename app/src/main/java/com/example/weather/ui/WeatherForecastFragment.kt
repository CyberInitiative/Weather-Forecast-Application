package com.example.weather.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.DailyForecastItem
import com.example.weather.HourlyForecastItem
import com.example.weather.R
import com.example.weather.adapter.DailyForecastAdapter
import com.example.weather.adapter.HourlyForecastAdapter
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.model.DailyForecast
import com.example.weather.model.HourlyForecast
import com.example.weather.utils.WeatherColorAnimator
import com.example.weather.viewmodel.ForecastViewModel
import com.example.weather.viewstate.ForecastViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs


class WeatherForecastFragment : Fragment(), DailyForecastAdapter.OnDailyForecastItemClick {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentWeatherForecastBinding

    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyForecastAdapter

    private var currentDailyItem: DailyForecastItem? = null

    private lateinit var temperatureUnit: String
    private var updateFrequency: Int = 1

    private val maxDistance = 255f

    /**
     * If there is only one tracked city, user can swipe screen only for this distance.
     */
    private val singleCityBorder = 25f

    private var originalXPosition: Float = 0f
    private var dX: Float = 0f
    private var isAnimationRunning = false
    private var isMovingWhenAnimationRunning = false
    private var isSingleCityBorderReach = false

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
        viewLifecycleOwner.lifecycleScope.launch {
            forecastViewModel.getListOfSavedCities().also {
                if (it.isEmpty()) {
                    findNavController().navigate(R.id.action_weatherForecastFragment_to_citiesSearcherFragment)
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(menuProvider)
        setUpRecyclerViews()
        setRootTouchListener()

        originalXPosition = binding.root.x

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

    @SuppressLint("ClickableViewAccessibility")
    private fun setRootTouchListener() {
        binding.root.setOnTouchListener { view, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {

                    if (isAnimationRunning) {
                        view.animate().cancel()
                        isAnimationRunning = false
                    }

                    dX = view.x - event.rawX
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isAnimationRunning) {
                        isMovingWhenAnimationRunning = true
                    } else {
                        isMovingWhenAnimationRunning = false
                    }

                    if (isMovingWhenAnimationRunning) {
                        dX = originalXPosition - event.rawX
                    }

                    if (!isSingleCityBorderReach) {

                        val distanceFromStart: Float

                        val newX = event.rawX + dX
                        view.x = newX

                        if (forecastViewModel.getTrackedCitiesStateValue().size == 1) {
                            distanceFromStart =
                                abs(newX - originalXPosition).coerceAtMost(singleCityBorder)

                            val alpha = 1 - (distanceFromStart / maxDistance).coerceIn(0f, 1f)
                            view.alpha = alpha

                            if (distanceFromStart >= singleCityBorder) {
                                isSingleCityBorderReach = true
                            }

                        } else {
                            distanceFromStart =
                                abs(newX - originalXPosition).coerceAtMost(maxDistance)

                            val alpha = 1 - (distanceFromStart / maxDistance).coerceIn(0f, 1f)
                            view.alpha = alpha

                            if (distanceFromStart >= maxDistance) {
                                val direction = if (newX > originalXPosition) 1 else -1
                                Log.d(TAG, "maxDistance reached. Direction is: $direction")

                                forecastViewModel.setNextToSwipedCityAsCurrentCity(direction)
                                newDataAppearanceAnimation(view, direction)
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMovingWhenAnimationRunning = false
                    isSingleCityBorderReach = false
                    if (!isAnimationRunning) {
                        if (abs(view.x - originalXPosition) <= maxDistance) {
                            animateReturn(binding.root, originalXPosition)
                        }
                    }
                }
            }
            true
        }
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
                        dailyForecastAdapter.currentList.map { it.data },
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

                is ForecastViewState.Content -> {
                    binding.weatherForecastFragmentHourlyForecastRecyclerView.stopScroll()
                    Log.d(
                        TAG,
                        "forecastViewModel.cityForecastState onEach triggered. CityForecastViewState.Content"
                    )
                    val dailyForecasts = state.dailyForecasts
                    dailyForecastAdapter.submitList(
                        forecastViewModel.setUpDailyForecasts(dailyForecasts)
                    ) {
                        binding.weatherForecastFragmentDailyForecastRecyclerView.scrollToPosition(0)
                        currentDailyItem = dailyForecastAdapter.currentList[0]
                        currentDailyItem!!.isScrolled = true
                        dailyForecastAdapter.notifyItemChanged(0)
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

                null -> {
                    makeAllViewsInvisibleExceptGiven()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
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
        hourlyForecastAdapter = HourlyForecastAdapter()
        dailyForecastAdapter = DailyForecastAdapter(this)

        setUpRecyclerView(
            binding.weatherForecastFragmentDailyForecastRecyclerView, dailyForecastAdapter
        )

        setUpRecyclerView(
            binding.weatherForecastFragmentHourlyForecastRecyclerView, hourlyForecastAdapter
        )

        setHourlyForecastRecyclerViewScrollListener()
    }

    private fun setHourlyForecastRecyclerViewScrollListener() {
        fun findDailyForecastItem(date: String): DailyForecastItem? {
            return dailyForecastAdapter
                .currentList
                .firstOrNull {
                    it.data.date == date
                }
        }

        fun setDailyForecastItemScrolledStatus(item: DailyForecastItem, isScrolled: Boolean) {
            item.isScrolled = isScrolled
            dailyForecastAdapter.notifyItemChanged(
                dailyForecastAdapter.currentList.indexOf(
                    item
                )
            )
        }

        binding.weatherForecastFragmentHourlyForecastRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d(TAG, "Scroller listener triggered.")

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val date = when (hourlyForecastAdapter.currentList[firstVisibleItemPosition]) {
                    is HourlyForecastItem.Data -> (hourlyForecastAdapter.currentList[firstVisibleItemPosition] as HourlyForecastItem.Data).forecast.date
                    is HourlyForecastItem.Header -> (hourlyForecastAdapter.currentList[firstVisibleItemPosition] as HourlyForecastItem.Header).date
                }

                if (currentDailyItem != null) {
                    if (currentDailyItem!!.data.date != date) {
                        setDailyForecastItemScrolledStatus(currentDailyItem!!, false)
                        currentDailyItem = findDailyForecastItem(date)

                        currentDailyItem?.let {
                            setDailyForecastItemScrolledStatus(it, true)
                        }
                    }
                } else {
                    currentDailyItem = findDailyForecastItem(date)
                    currentDailyItem?.let {
                        setDailyForecastItemScrolledStatus(currentDailyItem!!, true)
                    }
                }
            }
        })
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
            .firstOrNull { it.hourState == HourlyForecastItem.Data.HourState.PRESENT }
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
        val item = dailyForecastAdapter.currentList[position] as DailyForecastItem
        findHeader(item.data)
    }

    companion object {
        private const val TAG = "WeatherForecastFragment"
    }
}