package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.di.applicationModule
import com.example.weatherapp.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.EmptyLogger

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            if(BuildConfig.DEBUG) AndroidLogger() else EmptyLogger()

            androidContext(this@App)

            modules(listOf(networkModule, applicationModule))
        }
    }
}