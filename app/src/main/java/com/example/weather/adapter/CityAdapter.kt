package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.CityItemBinding
import com.example.weather.model.City

class CityAdapter(
    private val listener: OnViewItemClickListener,
    var cities: List<City>
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    interface OnViewItemClickListener {
        fun onViewItemClick(position: Int)
    }

    class CityViewHolder(private val binding: CityItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: City) {
            binding.cityItemFullCityLocationLabel.text = getFullCityLocation(item)
        }

        private fun getFullCityLocation(city: City): String {
            val locationBuilder = buildString {
                append("${city.name}")
                listOf(
                    city.admin1,
                    city.admin2,
                    city.admin3,
                    city.admin4,
                ).forEach { it?.let { append(", $it") } }
                if (city.country != null) {
                    append(", ${city.country}")
                }
            }
            return locationBuilder
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CityItemBinding.inflate(inflater, parent, false)
        val holder = CityViewHolder(binding)
        binding.root.setOnClickListener {
            listener.onViewItemClick(holder.adapterPosition)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cities[position])
    }

//    private object CityDiffer : DiffUtil.ItemCallback<CityLocation>() {
//        override fun areItemsTheSame(oldItem: CityLocation, newItem: CityLocation): Boolean {
//            return oldItem == newItem
//        }
//
//        override fun areContentsTheSame(oldItem: CityLocation, newItem: CityLocation): Boolean {
//            return oldItem == newItem
//        }
//    }
}