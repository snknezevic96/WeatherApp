package com.example.weatherapp.di

import com.example.weatherapp.ui.MainViewModel
import com.example.weatherapp.data.Repository
import com.squareup.picasso.Picasso
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val applicationModule = module {

    factory { Picasso.get() }
    single { Repository(get()) }

    viewModel { MainViewModel(get(), get()) }
}