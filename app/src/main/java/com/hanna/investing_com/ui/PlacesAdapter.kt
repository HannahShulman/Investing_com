package com.hanna.investing_com.ui

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hanna.investing_com.R
import com.hanna.investing_com.entities.SubInterestPlace
import com.hanna.investing_com.extensions.distanceToMetersFormat

class PlacesAdapter : ListAdapter<SubInterestPlace, PlaceViewHolder>(diffUtil) {

     private var currentLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_place_layout, parent, false)
        )
    }

    fun submitData(list: List<SubInterestPlace>, currentLocation: Location?){
        this.currentLocation = currentLocation
        submitList(list)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.tv.text = holder.itemView.context.getString(
            R.string.place_representation, currentList[position].name,
            "("+ currentLocation?.distanceTo(Location("").apply {
            currentList[position].location.run {
                latitude = lat
                longitude = lng
            }
        })?.distanceToMetersFormat()+")")


    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<SubInterestPlace>() {
            override fun areItemsTheSame(
                oldItem: SubInterestPlace,
                newItem: SubInterestPlace
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: SubInterestPlace,
                newItem: SubInterestPlace
            ): Boolean {
                return oldItem == newItem && oldItem.location.lat == newItem.location.lat &&
                        oldItem.location.lng == newItem.location.lng
            }

        }
    }
}

class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv: TextView = itemView.findViewById(R.id.tv)
}