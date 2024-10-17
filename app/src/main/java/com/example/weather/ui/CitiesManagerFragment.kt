package com.example.weather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.adapter.CityAdapter
import com.example.weather.databinding.FragmentCitiesManagerBinding
import com.example.weather.viewmodel.CitiesViewModel
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CitiesManagerFragment : Fragment(), CityAdapter.OnViewItemClickListener,
    CityAdapter.OnDeleteTrackedItemClickListener {

    private val forecastViewModel: ForecastViewModel by activityViewModel()
    private val citiesViewModel: CitiesViewModel by activityViewModel()

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

        citiesViewModel.trackedCitiesState.observe(viewLifecycleOwner) { list ->
            cityAdapter.submitList(list.toMutableList())
        }

//        forecastViewModel.loadListOfTrackedCities()

        binding.citiesManagerFragmentAddCityButton.setOnClickListener {
            findNavController().navigate(R.id.action_citiesManagerFragment_to_citiesSearcherFragment)
        }
    }

    override fun onViewItemClick(position: Int) {
//        forecastViewModel.setCurrentCity(cityAdapter.currentList[position])
        findNavController().popBackStack()
    }

    override fun onDeleteClickButton(position: Int) {
        citiesViewModel.deleteCity(cityAdapter.currentList[position])
    }

    companion object {
        private const val TAG = "CitiesManagerFragment"
    }
}