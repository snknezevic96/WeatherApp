package com.example.weatherapp.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/location/search")
    fun getLocationDataByCityName(@Query("query") cityName: String) : Call<List<LocationResponse>>

    @GET("api/location/{woeid}")
    fun getWeatherByLocationId(@Path("woeid") woeid: Int) : Call<WeatherResponse>

}