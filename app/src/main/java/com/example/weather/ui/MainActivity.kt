package com.example.weather.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val forecastViewModel: ForecastViewModel by viewModel()

    private lateinit var hourlyForecastAdapter: ForecastAdapter
    private lateinit var dailyForecastAdapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        //region ViewCompat.setOnApplyWindowInsetsListener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //endregion

        hourlyForecastAdapter = ForecastAdapter()
        dailyForecastAdapter = ForecastAdapter()
        setForecastRecyclerView(binding.mainActivityHourlyForecastRecyclerView, hourlyForecastAdapter)
        setForecastRecyclerView(binding.mainActivityDailyForecastRecyclerView, dailyForecastAdapter)

        forecastViewModel.hourlyForecastList.observe(this, Observer {
            hourlyForecastAdapter.submitList(it)
        })

        forecastViewModel.dailyForecastList.observe(this, Observer {
            dailyForecastAdapter.submitList(it)
        })

        forecastViewModel.loadForecast(
            46.4857,
            30.7438,
            listOf("temperature_2m", "weather_code"),
            listOf("weather_code"),
        )
    }

    private fun setForecastRecyclerView(recyclerView: RecyclerView, forecastAdapter: ForecastAdapter) {
        recyclerView.apply {
            adapter = forecastAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.HORIZONTAL
                )
            )
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}