package com.example.weather.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.databinding.FragmentWeatherForecastBinding
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WeatherForecastFragment : Fragment() {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentWeatherForecastBinding
    private lateinit var hourlyForecastAdapter: ForecastAdapter
    private lateinit var dailyForecastAdapter: ForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerViews()

        forecastViewModel.hourlyForecastLiveData.observe(viewLifecycleOwner) {
            hourlyForecastAdapter.submitList(it)
        }

        forecastViewModel.dailyForecastLiveData.observe(viewLifecycleOwner) {
            dailyForecastAdapter.submitList(it)
        }

        forecastViewModel.currentCityLiveData.observe(viewLifecycleOwner){
            forecastViewModel.onNewCurrentCity(it)
            binding.textView.text = it.name
        }
    }

    private fun setUpRecyclerViews() {
        hourlyForecastAdapter = ForecastAdapter()
        dailyForecastAdapter = ForecastAdapter()

        setUpForecastRecyclerView(
            binding.weatherForecastFragmentHourlyForecastRecyclerView,
            hourlyForecastAdapter
        )

        setUpForecastRecyclerView(
            binding.weatherForecastFragmentDailyForecastRecyclerView,
            dailyForecastAdapter
        )
    }

    private fun setUpForecastRecyclerView(
        recyclerView: RecyclerView,
        forecastAdapter: ForecastAdapter
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
}