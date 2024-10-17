package com.example.weather.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weather.ui.WeatherForecastFragment

class WeatherFragmentStateAdapter(
    fragment: Fragment,
    private var itemCount: Int
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return itemCount
    }

    override fun createFragment(position: Int): Fragment {
        Log.d(TAG, "createFragment(position: Int): Fragment called;")
        return WeatherForecastFragment.newInstance(position)
    }

    companion object {
        private const val TAG = "WeatherFragmentStateAdapter"
    }
}