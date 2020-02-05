package com.example.weatherapp.data

import com.example.weatherapp.data.remote.ApiService

class Repository(private val apiService: ApiService) {

    fun getLocationDataByCityName(cityName: String) = apiService.getLocationDataByCityName(cityName)

    fun getWeatherByLocationId(woeid: Int) = apiService.getWeatherByLocationId(woeid)
}