package com.example.weatherapp.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import kotlinx.android.synthetic.main.weather_day_info_item.view.*
import kotlinx.android.synthetic.main.weather_info_item.view.*
import kotlinx.android.synthetic.main.weather_info_item.view.image_view
import kotlinx.android.synthetic.main.weather_info_item.view.value_view

class WeatherInfoAdapter(private val type: String) : RecyclerView.Adapter<WeatherInfoAdapter.ViewHolder>(){

    var items = listOf<Pair<String, Int>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when(type) {
            "info" -> { R.layout.weather_info_item}
            "daily" -> {R.layout.weather_day_info_item}
            else -> 0
        }
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]

        holder.valueView.text = item.first
        holder.imageView.setImageResource(item.second)

        if(position == items.lastIndex)
            holder.endLine?.visibility = View.GONE
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val valueView = itemView.value_view
        val imageView = itemView.image_view
        val endLine = itemView.end_line_view ?: null
    }

    fun refreshAdapter(items: List<Pair<String, Int>>) {
        this.items = items
        notifyDataSetChanged()
    }
}