package com.example.weatherapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.weatherapp.R
import com.example.weatherapp.Result
import com.example.weatherapp.data.remote.WeatherResponse
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    private lateinit var weatherInfoAdapter: WeatherInfoAdapter
    private lateinit var weatherDailyAdapter: WeatherInfoAdapter

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress_bar.visibility = View.VISIBLE
        loading_view.visibility = View.VISIBLE
        loading_view.text = "Searching for location..."

        weatherInfoAdapter = WeatherInfoAdapter("info")
        weather_info_recycler.adapter = weatherInfoAdapter

        weatherDailyAdapter = WeatherInfoAdapter("daily")
        weather_daily_recycler.adapter = weatherDailyAdapter

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1L, 1F, mLocationListener)

        observeLocationData()
        observeWeatherData()

        add_location.setOnClickListener {
            if(search_layout.visibility == View.VISIBLE) {
                city_name.visibility = View.VISIBLE
                date_time.visibility = View.VISIBLE
                search_layout.visibility = View.INVISIBLE
            }
            else {
                city_name.visibility = View.INVISIBLE
                date_time.visibility = View.INVISIBLE
                search_layout.visibility = View.VISIBLE
            }
        }

        search_btn.setOnClickListener {
            viewModel.getLocationDataByCityName(search_tv.text.toString())
            search_progress_bar.visibility = View.VISIBLE
        }
    }

    private var mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {

            if(location != null) {

                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                val addresses: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val cityName = addresses[0].locality

                viewModel.getLocationDataByCityName(cityName)
                loading_view.text = "Loading weather for $cityName..."

                unregisterLocationListener()
            }
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}
    }


    private fun observeLocationData() = viewModel.locationData.observe(this, Observer {
        if(it == null) return@Observer

        when(it) {
            is Result.Success -> {
                viewModel.getWeatherData(it.data.woeid)
            }
            is Result.Error -> {
                search_progress_bar.visibility = View.GONE
                Toast.makeText(this, it.exception.message, Toast.LENGTH_LONG).show()
            }
        }
    })

    private fun observeWeatherData() = viewModel.weatherData.observe(this, Observer {
        if(it == null) return@Observer

        when(it) {
            is Result.Success -> {
                Log.i("API_MESSAGE", it.data.toString())

                setData(it.data)
            }
            is Result.Error -> {
                Toast.makeText(this, it.exception.message, Toast.LENGTH_LONG).show()
            }
        }
        loading_view.visibility = View.GONE
        progress_bar.visibility = View.GONE
        search_progress_bar.visibility = View.GONE
        search_layout.visibility = View.INVISIBLE
        city_name.visibility = View.VISIBLE
        date_time.visibility = View.VISIBLE
    })

    private fun unregisterLocationListener() = locationManager.removeUpdates(mLocationListener)

    private fun setData(data: WeatherResponse) {

        city_name.text = data.title

        val todayWeather = data.consolidated_weather[0]

        val imageDrawable = viewModel.getImageForWeather(todayWeather.weather_state_name)
        weater_icon.setImageDrawable(this.getDrawable(imageDrawable))

        val dateName = viewModel.parseDateToDayName(todayWeather.applicable_date)
        val date = viewModel.parseStringToDate(todayWeather.applicable_date)
        val mm = if(date.monthValue < 10) "0${date.monthValue}" else date.monthValue.toString()
        val ddMM = "${date.dayOfMonth}-$mm"

        date_time.text = "$dateName, $ddMM"
        current_temperature.text = "${todayWeather.the_temp.toInt()}°"

        val weatherInfo = listOf(
            Pair("${todayWeather.humidity}%", R.drawable.ic_humidity),
            Pair("${todayWeather.wind_speed.toInt()}mph", R.drawable.ic_wind),
            Pair("${todayWeather.min_temp.toInt()}°", R.drawable.ic_min),
            Pair("${todayWeather.max_temp.toInt()}°", R.drawable.ic_max)
        )
        val weatherPerDay = mutableListOf<Pair<String, Int>>()

        data.consolidated_weather.takeLast(5).forEach {
            it.applicable_date
            val date = viewModel.parseDateToDayName(it.applicable_date)
            val icon = viewModel.getImageForWeather(it.weather_state_name)
            weatherPerDay.add(Pair(date, icon))
        }

        weatherInfoAdapter.refreshAdapter(weatherInfo)
        weatherDailyAdapter.refreshAdapter(weatherPerDay)
    }
}
