package com.example.weather.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.adapter.CityAdapter
import com.example.weather.databinding.FragmentCitiesManagerBinding
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CitiesManagerFragment : Fragment(), CityAdapter.OnViewItemClickListener {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentCitiesManagerBinding
    private lateinit var cityAdapter: CityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCitiesManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cityAdapter = CityAdapter(this, emptyList())
        binding.citiesManagerFragmentCitiesRecyclerView.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        forecastViewModel.trackedCitiesLiveData.observe(viewLifecycleOwner) {
            cityAdapter.cities = it
            cityAdapter.notifyDataSetChanged()
        }

        forecastViewModel.searchForCities()

        binding.citiesManagerFragmentAddCityButton.setOnClickListener {
            findNavController().navigate(R.id.action_citiesManagerFragment_to_citiesSearcherFragment)
        }
    }

    override fun onViewItemClick(position: Int) {
        forecastViewModel.setCurrentCity(cityAdapter.cities[position])
        findNavController().popBackStack()
    }

    companion object {
        private const val TAG = "CitiesManagerFragment"
    }
}