package com.example.weather.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.adapter.ForecastAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.receiver.LocationReceiver
import com.example.weather.viewmodel.ForecastViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), ForecastAdapter.OnItemClickListener,
    LocationReceiver.OnLocationEnabledListener {
    private lateinit var binding: ActivityMainBinding
    private val forecastViewModel: ForecastViewModel by viewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var hourlyForecastAdapter: ForecastAdapter
    private lateinit var dailyForecastAdapter: ForecastAdapter

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLocationAndLoadWeather()
            } else {
                showPermissionRationaleDialog()
            }
        }

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

        hourlyForecastAdapter = ForecastAdapter(this)
        dailyForecastAdapter = ForecastAdapter(this)
        setForecastRecyclerView(
            binding.mainActivityHourlyForecastRecyclerView,
            hourlyForecastAdapter
        )
        setForecastRecyclerView(binding.mainActivityDailyForecastRecyclerView, dailyForecastAdapter)

        forecastViewModel.hourlyForecastList.observe(this, Observer {
            hourlyForecastAdapter.submitList(it)
        })

        forecastViewModel.dailyForecastList.observe(this, Observer {
            dailyForecastAdapter.submitList(it)
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        manageLocationPermission()
        createLocationReceiver()
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAndLoadWeather() {
        Log.d(TAG, "getLocationAndLoadWeather() called")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d(TAG, "Last known location. Latitude: $latitude; Longitude: $longitude.")
                    forecastViewModel.loadForecast(
                        latitude,
                        longitude,
                        listOf("temperature_2m", "weather_code"),
                        listOf("weather_code"),
                    )
                } else {
                    requestNewLocationData()
                }
            }
    }

    private fun manageLocationPermission() {
        Log.d(TAG, "manageLocationPermission() called.")
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocationAndLoadWeather()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                showPermissionRationaleDialog()
            }

            else -> {
                requestPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        }
    }

    private fun setForecastRecyclerView(
        recyclerView: RecyclerView,
        forecastAdapter: ForecastAdapter
    ) {
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

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                getLocationAndLoadWeather()
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Location Permission Needed")
            .setMessage("A weather application needs access to the user's location to provide accurate and relevant weather information.")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
            .setNegativeButton(android.R.string.cancel, null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onItemClick(position: Int) {
//        val dailyForecast = dailyForecastAdapter.currentList[position] as Forecast.DailyForecast
//        val relevantHourlyForecast = forecastViewModel.getRelevantHourlyForecast()
//        if (dailyForecast.date == DateAndTimeMapper.getCurrentDate() && relevantHourlyForecast.isNotEmpty()) {
//            hourlyForecastAdapter.submitList(relevantHourlyForecast)
//        } else {
//            hourlyForecastAdapter.submitList(dailyForecast.hourlyForecastList)
//        }
    }

    override fun onLocationEnabled() {
        Log.d(TAG, "onLocationEnabled called!")
        manageLocationPermission()
    }

    private fun createLocationReceiver() {
        val locationReceiver = LocationReceiver(this)
        val filter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        val listenToBroadcastsFromOtherApps = false
        val receiverFlags = if (listenToBroadcastsFromOtherApps) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }
        ContextCompat.registerReceiver(this, locationReceiver, filter, receiverFlags)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}