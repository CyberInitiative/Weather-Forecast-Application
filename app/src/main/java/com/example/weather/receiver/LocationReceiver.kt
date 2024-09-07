package com.example.weather.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class LocationReceiver(private val listener: OnLocationEnabledListener) : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p0 != null){
            val locationManager = p0.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(locationManager.isLocationEnabled){
                Log.d(TAG, "Broadcast enabled!")
                listener.onLocationEnabled()
            } else if (!locationManager.isLocationEnabled) {
                Log.d(TAG, "Broadcast disabled")
            }
        }
    }

    interface OnLocationEnabledListener{
        fun onLocationEnabled()
    }

    companion object{
       private const val TAG = "LocationReceiver"
    }
}