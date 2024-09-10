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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.weather.R
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

class MainActivity : AppCompatActivity()/*, LocationReceiver.OnLocationEnabledListener*/ {
    private lateinit var binding: ActivityMainBinding
    private val forecastViewModel: ForecastViewModel by viewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var navController: NavController

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                getLocationAndLoadWeather()
//            } else {
//                showPermissionRationaleDialog()
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        onBackPressedDispatcher.addCallback(this) {
            if (!navController.navigateUp()) {
                finish()
            }
        }

//        forecastViewModel.loadCities()
        forecastViewModel.loadForecastForTrackedCities()
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        createLocationReceiver()
//        manageLocationPermission()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settingsMenuManageCitiesItem -> {
                navController.navigate(R.id.citiesManagerFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //fun loadForecastForTrackedCities()
/*
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
                        longitude
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

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000 * 60 * 60).build()

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
 */
    companion object {
        private const val TAG = "MainActivity"
    }
}