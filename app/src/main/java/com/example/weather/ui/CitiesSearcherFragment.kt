package com.example.weather.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.WeatherColorAnimator
import com.example.weather.adapter.CityAdapter
import com.example.weather.databinding.FragmentCitiesSearcherBinding
import com.example.weather.model.City
import com.example.weather.model.HourlyForecast
import com.example.weather.viewmodel.CitySearchViewModel
import com.example.weather.viewmodel.ForecastViewModel
import com.example.weather.viewstate.CitySearchViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CitiesSearcherFragment : Fragment(), CityAdapter.OnViewItemClickListener {

    private val citySearchViewModel: CitySearchViewModel by viewModel()
    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentCitiesSearcherBinding
    private lateinit var cityAdapter: CityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCitiesSearcherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCityInputTextChangedListener()
        cityAdapter = CityAdapter(this, CityAdapter.CITY_SEARCH)
        binding.citiesSearcherFragmentRecyclerView.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            val itemDecorator = DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
            ResourcesCompat.getDrawable(resources, R.drawable.item_divider, null)
                ?.let { itemDecorator.setDrawable(it) }
            addItemDecoration(itemDecorator)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeSearchedCitiesState()
                observeTimeOfDay()
            }
        }
    }

    private fun observeSearchedCitiesState() {
        citySearchViewModel.citySuggestionsState.onEach { state ->
            when (state) {
                is CitySearchViewState.Initial -> {
                    binding.citiesSearcherFragmentNoCitiesFoundLabel.animateViewVisibility(View.INVISIBLE)
                    cityAdapter.submitList(emptyList())
                }

                is CitySearchViewState.Loading -> {

                }

                is CitySearchViewState.Content -> {
                    val content = state.cities
                    cityAdapter.submitList(content.toMutableList())
                    if (content.isNotEmpty()) {
                        binding.citiesSearcherFragmentNoCitiesFoundLabel.animateViewVisibility(View.INVISIBLE)
                    } else {
                        binding.citiesSearcherFragmentNoCitiesFoundLabel.animateViewVisibility(View.VISIBLE)
                    }
                }

                is CitySearchViewState.Error -> {
                    Log.d(
                        TAG,
                        "citySearchViewModel.citySuggestionsState onEach triggered. CityForecastViewState.Error"
                    )
                }
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

    private fun setUpCityInputTextChangedListener() {
        binding.citiesSearcherFragmentCityInputEditText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val trimmed = it.toString().trim()
                    if (trimmed.isNotEmpty()) {
                        citySearchViewModel.searchCity(trimmed)
                    } else {
                        citySearchViewModel.cleanCitiesSuggestions()
                    }
                }
            }
        })
    }

    override fun onViewItemClick(position: Int) {
        val city: City = cityAdapter.currentList[position]
        citySearchViewModel.cleanCitiesSuggestions()

        forecastViewModel.saveTrackedCity(city)

        val navController = findNavController()
        val previousBackStackEntry = navController.previousBackStackEntry

        if (previousBackStackEntry != null && previousBackStackEntry.destination.id == R.id.citiesManagerFragment) {
            navController.navigate(R.id.action_citiesSearcherFragment_to_weatherForecastFragment)
        } else {
            navController.navigate(R.id.action_citiesSearcherFragment_to_weatherForecastFragment2)
        }
    }

    private fun observeTimeOfDay() {
        forecastViewModel.timeOfDayState.onEach { state ->
            if (state == HourlyForecast.TimeOfDay.DAY) {
                binding.citiesSearcherFragmentCityInputEditText.setHintTextColor(
                    resources.getColor(
                        R.color.castle_moat,
                        requireContext().theme
                    )
                )

                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.search_svgrepo_com)?.apply {
                    setTint(ContextCompat.getColor(requireContext(), R.color.castle_moat))
                }
                binding.citiesSearcherFragmentCityInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)

                WeatherColorAnimator.animateDrawableChange(
                    binding.citiesSearcherFragmentCityInputEditText,
                    R.drawable.search_box_day,
                    250
                )
            } else {
                binding.citiesSearcherFragmentCityInputEditText.setHintTextColor(
                    resources.getColor(
                        R.color.peaceful_night,
                        requireContext().theme
                    )
                )

                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.search_svgrepo_com)?.apply {
                    setTint(ContextCompat.getColor(requireContext(), R.color.peaceful_night))
                }
                binding.citiesSearcherFragmentCityInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)

                WeatherColorAnimator.animateDrawableChange(
                    binding.citiesSearcherFragmentCityInputEditText,
                    R.drawable.search_box_night,
                    250
                )
            }

        }.launchIn(lifecycleScope)
    }

    companion object {
        private const val TAG = "CitiesSearcherFragment"
    }

}