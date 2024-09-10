package com.example.weather.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.adapter.CityAdapter
import com.example.weather.databinding.FragmentCitiesSearcherBinding
import com.example.weather.model.City
import com.example.weather.viewmodel.ForecastViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CitiesSearcherFragment : Fragment(), CityAdapter.OnViewItemClickListener {

    private val forecastViewModel: ForecastViewModel by activityViewModel()

    private lateinit var binding: FragmentCitiesSearcherBinding
    private lateinit var cityAdapter: CityAdapter

//    private var onCityLocationChosenListener: OnCityLocationChosenListener? = null

//    interface OnCityLocationChosenListener{
//        fun onCityLocationChosen(cityLocation: CityLocation)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCitiesSearcherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCityInputTextChangedListener()

        cityAdapter = CityAdapter(this, emptyList())
        binding.citiesSearcherFragmentRecyclerView.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        forecastViewModel.citySuggestionsLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "in fragment: ${it.joinToString(", ")}")
            cityAdapter.cities = it
            cityAdapter.notifyDataSetChanged()
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        onCityLocationChosenListener = context as OnCityLocationChosenListener
//    }

//    override fun onDetach() {
//        onCityLocationChosenListener = null
//        super.onDetach()
//    }

    private fun setUpCityInputTextChangedListener() {
        binding.citiesSearcherFragmentCityInputEditText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    if (it.length >= 2) {
                        forecastViewModel.loadCities(p0.toString())
                    }
                }
            }

        })
    }

    override fun onViewItemClick(position: Int) {
        val city: City = cityAdapter.cities[position]

//        forecastViewModel.loadForecast(
//            city.latitude, city.longitude, listOf("temperature_2m", "weather_code"),
//            listOf("weather_code")
//        )
        forecastViewModel.loadForecastAndSet(city)
        forecastViewModel.clearCitySuggestionsLiveData()
        forecastViewModel.saveCity(city)
        findNavController().popBackStack()

//        onCityLocationChosenListener?.onCityLocationChosen(cityLocation) ?: throw IllegalStateException("onCityLocationChosenListener is null!")
    }

    companion object {
        private const val TAG = "CitiesSearcherFragment"
    }

}