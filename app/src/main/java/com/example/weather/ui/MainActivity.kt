package com.example.weather.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weather.R
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.model.HourlyForecast
import com.example.weather.network.NetworkManager
import com.example.weather.utils.WeatherColorAnimator
import com.example.weather.viewmodel.ForecastViewModel
import com.example.weather.worker.ForecastRequestWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity()/*, LocationReceiver.OnLocationEnabledListener*/ {
    private lateinit var binding: ActivityMainBinding
    private val forecastViewModel: ForecastViewModel by viewModel()
    private val networkManager: NetworkManager by inject()

    private lateinit var navController: NavController

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private val networkCallback = networkManager.getNetworkCallback(
        { Log.d(TAG, "onAvailable") }, null, null
    )

    private val workerConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val workManager = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //region ViewCompat.setOnApplyWindowInsetsListener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //endregion

        setSupportActionBar(binding.mainActivityToolBar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.citiesSearcherFragment) {
                lifecycleScope.launch {
                    if (forecastViewModel.getListOfSavedCities().isEmpty()) {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    } else {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    }
                }

            }
            Log.d("BackStack", "Navigated to ${destination.label}")
        }
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))

        observeTrackedCities()
        if (networkManager.isInternetAvailable()) {
            forecastViewModel.loadForecastForTrackedCities()
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1000)
                AlertDialog.Builder(this@MainActivity)
                    .setCancelable(false)
                    .setTitle("Select weather update frequency")
                    .setPositiveButton(R.string.settings) { _, _ ->
                        startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }
        }
        observeTimeOfDay()
        observeUpdateFrequency()

    }

    override fun onStart() {
        super.onStart()
        networkManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onPause() {
        networkManager.unregisterNetworkCallback(networkCallback)
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        val currentDestination = navController.currentDestination
        return when {
            currentDestination?.id == R.id.citiesManagerFragment -> {
                lifecycleScope.launch {
                    if (forecastViewModel.getListOfSavedCities().isEmpty()) {
                        Log.d(TAG, "navigate from citiesManagerFragment to citiesSearcherFragment")
                        navController.navigate(R.id.action_citiesManagerFragment_to_citiesSearcherFragment2)
                    } else {
                        navController.navigateUp() || super.onSupportNavigateUp()
                    }
                }
                true

            }

            else -> {
                navController.navigateUp() || super.onSupportNavigateUp()
            }
        }

    }

    private fun setUpPeriodicWorkRequest(hourInterval: Int) {
        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<ForecastRequestWorker>(hourInterval.toLong(), TimeUnit.HOURS)
                .setConstraints(workerConstraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "forecast_request_periodic_worker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicWorkRequest
        )
        Log.d(TAG, "Periodic work scheduled every $hourInterval hours.")
    }

    private fun observeTrackedCities() {
        forecastViewModel.trackedCitiesLiveData.observe(this) { cities ->
            cities?.let {
                if (cities.isNotEmpty()) {
                    Log.d("EmptyCityMain", "city list is not empty")
                    forecastViewModel.loadForecastForTrackedCities()
                } else {
                    Log.d("EmptyCityMain", "city list is empty")
                }
            }
        }
    }

    private fun observeUpdateFrequency() {
        forecastViewModel.updateFrequency.onEach { frequency ->
            setUpPeriodicWorkRequest(frequency)
        }.launchIn(lifecycleScope)
    }

    private fun observeTimeOfDay() {
        forecastViewModel.timeOfDayState.onEach { state ->
            if (state == HourlyForecast.TimeOfDay.DAY) {
                WeatherColorAnimator.animateDrawableChange(
                    binding.root,
                    R.drawable.sunny_day_background,
                    250
                )
            } else {
                WeatherColorAnimator.animateDrawableChange(
                    binding.root,
                    R.drawable.clear_night_background,
                    250
                )
            }

        }.launchIn(lifecycleScope)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}