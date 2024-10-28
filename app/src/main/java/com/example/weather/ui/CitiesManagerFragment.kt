package com.example.weather.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.adapter.CityAdapter
import com.example.weather.databinding.FragmentCitiesManagerBinding
import com.example.weather.viewmodel.ForecastViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CitiesManagerFragment : Fragment(), CityAdapter.OnViewItemClickListener,
    CityAdapter.OnDeleteTrackedItemClickListener {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentCitiesManagerBinding
    private lateinit var cityAdapter: CityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleBackButtonPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCitiesManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cityAdapter = CityAdapter(this, CityAdapter.CITY_MANAGING, this)
        binding.citiesManagerFragmentCitiesRecyclerView.apply {
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

        forecastViewModel.trackedCitiesLiveData.observe(viewLifecycleOwner) { list ->
            cityAdapter.submitList(list.toMutableList())
        }

        binding.citiesManagerFragmentAddCityButton.setOnClickListener {
            findNavController().navigate(R.id.action_citiesManagerFragment_to_citiesSearcherFragment)
        }
    }

    override fun onViewItemClick(position: Int) {
        forecastViewModel.setCurrentCity(cityAdapter.currentList[position])
        findNavController().popBackStack()
    }

    override fun onDeleteClickButton(position: Int) {
        forecastViewModel.deleteTrackedCity(cityAdapter.currentList[position])
    }

    private fun handleBackButtonPressed(){
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Log.d(TAG, "DETECTED BACK PRESS")
                viewLifecycleOwner.lifecycleScope.launch {
                    if(forecastViewModel.getListOfSavedCities().isEmpty()){
                        Log.d(TAG, "DETECTED BACK PRESS 1")
                        findNavController().navigate(R.id.action_citiesManagerFragment_to_citiesSearcherFragment2)
                    } else {
                        Log.d(TAG, "DETECTED BACK PRESS 2")
                        isEnabled = false
                        activity?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {
        private const val TAG = "CitiesManagerFragment"
    }
}