package com.example.weather.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.weather.databinding.CityItemBinding
import com.example.weather.databinding.TrackedCityItemBinding
import com.example.weather.model.City

class CityAdapter(
    private val listener: OnViewItemClickListener,
    private val adapterType: Int
) : ListAdapter<City, CityAdapter.CityViewHolder>(CityDiffer) {

    constructor(
        listener: OnViewItemClickListener,
        adapterType: Int,
        onDeleteTrackedItemClickListener: OnDeleteTrackedItemClickListener
    ) : this(listener, adapterType) {
        this.onDeleteTrackedItemClickListener = onDeleteTrackedItemClickListener
    }

    private lateinit var onDeleteTrackedItemClickListener: OnDeleteTrackedItemClickListener

    private var isDeleteMode = false

    interface OnViewItemClickListener {
        fun onViewItemClick(position: Int)
    }

    interface OnDeleteTrackedItemClickListener {
        fun onDeleteClickButton(position: Int)
    }

    abstract class CityViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: City)
    }

    class SearchedCityViewHolder(private val binding: CityItemBinding) :
        CityViewHolder(binding) {
        override fun bind(item: City) {
            binding.cityItemFullCityLocationLabel.text = getFullCityLocation(item)
        }

        private fun getFullCityLocation(city: City): String {
            val locationBuilder = buildString {
                append(city.name)
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

    class TrackedCityViewHolder(private val binding: TrackedCityItemBinding) :
        CityViewHolder(binding) {
        override fun bind(item: City) {
            binding.trackedCityItemNameLabel.text = getNameAndCountry(item)
        }

        val button = binding.trackedCityItemDeleteButton

        private fun getNameAndCountry(city: City): String {
            val locationBuilder = buildString {
                append(city.name)
                if (city.country != null) {
                    append(", ${city.country}")
                }
            }
            return locationBuilder
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (adapterType) {
            CITY_SEARCH -> CITY_SEARCH
            CITY_MANAGING -> CITY_MANAGING
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewBinding
        val holder = when (viewType) {
            CITY_SEARCH -> {
                binding = CityItemBinding.inflate(inflater, parent, false)
                SearchedCityViewHolder(binding)
            }

            CITY_MANAGING -> {
                binding = TrackedCityItemBinding.inflate(inflater, parent, false)
                binding.root.setOnLongClickListener {
                    Log.d(TAG, "isDeleteMode before: $isDeleteMode")
                    isDeleteMode = !isDeleteMode
                    notifyDataSetChanged()
                    Log.d(TAG, "isDeleteMode after: $isDeleteMode")
                    true
                }

                TrackedCityViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }

        binding.root.setOnClickListener {
            listener.onViewItemClick(holder.adapterPosition)
        }

        if (holder is TrackedCityViewHolder) {
            holder.button.setOnClickListener {
                onDeleteTrackedItemClickListener.onDeleteClickButton(holder.adapterPosition)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (holder is TrackedCityViewHolder) {
            if (isDeleteMode) {
                (holder).button.visibility = View.VISIBLE
            } else {
                (holder).button.visibility = View.INVISIBLE
            }
        }
    }

    private object CityDiffer : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val CITY_SEARCH = 1
        const val CITY_MANAGING = 2
        private const val TAG = "CityAdapter"
    }
}