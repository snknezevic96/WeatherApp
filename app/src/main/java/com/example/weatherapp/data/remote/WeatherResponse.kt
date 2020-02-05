package com.example.weatherapp.data.remote

import com.example.weatherapp.data.model.ConsolidatedWeather
import com.example.weatherapp.data.model.Parent
import com.example.weatherapp.data.model.Source

data class WeatherResponse(
    val consolidated_weather: List<ConsolidatedWeather>,
    val latt_long: String,
    val location_type: String,
    val parent: Parent,
    val sources: List<Source>,
    val sun_rise: String,
    val sun_set: String,
    val time: String,
    val timezone: String,
    val timezone_name: String,
    val title: String,
    val woeid: Int
)