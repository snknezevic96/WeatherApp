package com.example.weatherapp.data.remote

data class LocationResponse(
    val latt_long: String,
    val location_type: String,
    val title: String,
    val woeid: Int
)