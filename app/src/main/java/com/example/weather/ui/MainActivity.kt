package com.example.weather.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val forecastViewModel: ForecastViewModel by viewModel()

    private lateinit var forecastAdapter: ForecastAdapter

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

        setUpRecyclerView()

        forecastViewModel.hourlyForecastList.observe(this, Observer {
            forecastAdapter.submitList(it)
        })

        forecastViewModel.loadForecast(46.4857, 30.7438, listOf("temperature_2m","weather_code"), 1)
    }

    private fun setUpRecyclerView() {
        forecastAdapter = ForecastAdapter()
        binding.mainForecastRecyclerView.apply {
            adapter = forecastAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.HORIZONTAL
                )
            )
        }
    }


    /*
    private fun setUpRESTApi(){
        val call = ApiClient.forecastService.getForecast(46.4857, 30.7438, listOf("temperature_2m","weather_code"), 1)

        call.enqueue(object: Callback<ForecastResponse> {
            override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    // Handle the retrieved post data
                    println(post)
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
     */

    companion object {
        private const val TAG = "MainActivity"
    }
}