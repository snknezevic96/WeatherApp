package com.example.weatherapp.di

import android.util.Log
import com.example.weatherapp.data.remote.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {

    single<Gson>(override = true) {
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.sss")
            .create()
    }

    single { OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.d("http_interceptor", message) }))
        .cache(null)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build() }

    single { Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(get()))
        .client(get())
        .baseUrl("https://www.metaweather.com/")
        .build()
        .create(ApiService::class.java) }
}