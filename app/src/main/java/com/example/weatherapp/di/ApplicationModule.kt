package com.example.weatherapp.di

import com.example.weatherapp.ui.weather.WeatherViewModel
import com.example.weatherapp.data.Repository
import com.example.weatherapp.ui.map.MapsViewModel
import com.example.weatherapp.util.LocationManager
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val applicationModule = module {

    single { Repository(get()) }

    single { LocationManager(get()) }


    viewModel { WeatherViewModel(get(), get()) }

    viewModel { MapsViewModel(get()) }
}