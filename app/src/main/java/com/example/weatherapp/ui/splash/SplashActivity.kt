package com.example.weatherapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R
import com.example.weatherapp.ui.weather.WeatherActivity

class SplashActivity : AppCompatActivity() {

    private val mHandler = Handler()
    private val mLauncher = Launcher()

    override fun onStart() {
        super.onStart()

        mHandler.postDelayed(mLauncher, SPLASH_DELAY.toLong())
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)
    }

    override fun onStop() {
        mHandler.removeCallbacks(mLauncher)
        super.onStop()
    }

    private fun launch() {

        if (!isFinishing) {
            startActivity( Intent(this, WeatherActivity::class.java) )
            finish()
        }
    }

    private inner class Launcher : Runnable {
        override fun run() {
            launch()
        }
    }

    companion object {
        //show splash for 2 seconds
        private const val SPLASH_DELAY = 2000
    }
}