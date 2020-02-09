package com.example.weatherapp.ui.weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.weatherapp.*
import com.example.weatherapp.data.remote.WeatherResponse
import kotlinx.android.synthetic.main.activity_weather.*
import org.koin.android.viewmodel.ext.android.viewModel
import com.example.weatherapp.ui.map.MapsActivity
import com.example.weatherapp.util.*


class WeatherActivity : AppCompatActivity() {

    var cityFromMaps: String? = null

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var weatherDailyAdapter: WeatherAdapter

    private val viewModel: WeatherViewModel by viewModel()

    private lateinit var searchDialog: SearchLocationDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        viewModel.checkPermission(this)

        searchDialog = SearchLocationDialog(viewModel)

        cityFromMaps = intent.extras?.getString("cityName")

        observeLocationState()

        if (cityFromMaps != null)
            viewModel.getLocationDataByCityName(cityFromMaps!!)
         else
            viewModel.getCurrentLocation(this)

        setRecyclers()
        setOnClickListeners()
        setRefreshListener()

        observeLocationData()
        observeWeatherData()
        observeLiveGpsLocation()

        observeGpsState()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.locationPermissionGranted = true

            viewModel.initializeLocationListener()
            viewModel.requestGps(this)
        }
    }

    private fun observeLocationState() = viewModel.locationState.observe(this, Observer {state ->
        if (state == null) return@Observer

        if(state == LocationState.IN_PROGRESS)
            setLoadingViews()
    })

    private fun observeGpsState() = viewModel.gpsState.observe(this, Observer {
        if (it == null) return@Observer

        if (it == true)
            viewModel.getCurrentLocation(this)
    })

    private fun observeLiveGpsLocation() = viewModel.currentGpsLocation.observe(this, Observer {
        if (it == null) return@Observer

        if (cityFromMaps == null) {
            when (it) {
                is Result.Success -> {
                    val cityName = viewModel.getLocationDataByLatLon(it.data.latitude, it.data.longitude, this)
                    loading_tv.text = "${getString(R.string.loading_weather_for_city)} $cityName..."
                }
                is Result.Error ->
                    this.toastMessage(it.exception.message ?: getString(R.string.unknown_error_message))
            }
            viewModel.clearGpsLocation()
            viewModel.setLocationStateDone()
        }
    })

    private fun observeLocationData() = viewModel.locationData.observe(this, Observer {
        if (it == null) return@Observer

        when (it) {
            is Result.Success -> {
                viewModel.getWeatherData(it.data.woeid)
                searchDialog.dialog?.cancel()
            }
            is Result.Error -> {
                searchDialog.progressBar?.visibility = View.GONE
                this.toastMessage(it.exception.message ?: getString(R.string.unknown_error_message))
                setViewsVisibilityAfterWeatherDataChange()
            }
        }
        cityFromMaps = null
        viewModel.setLocationStateDone()
    })

    private fun observeWeatherData() = viewModel.weatherData.observe(this, Observer {
        if (it == null) return@Observer

        when (it) {
            is Result.Success -> setData(it.data)
            is Result.Error -> this.toastMessage(it.exception.message ?: getString(R.string.unknown_error_message))
        }
        setViewsVisibilityAfterWeatherDataChange()
    })

    private fun setRefreshListener() {

        pull_to_refresh.setOnRefreshListener {
            viewModel.refreshCurrentLocation(this)
            pull_to_refresh.isRefreshing = false
        }
    }

    private fun setOnClickListeners() {
        add_location_iv.setOnClickListener {
            searchDialog.createDialog(this)
        }

        go_to_map_iv.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun setRecyclers() {
        weatherAdapter = WeatherAdapter("info")
        weather_info_recycler.adapter = weatherAdapter

        weatherDailyAdapter =
            WeatherAdapter("daily")
        weather_daily_recycler.adapter = weatherDailyAdapter
    }

    private fun setData(data: WeatherResponse) {
        val todayWeather = data.consolidated_weather[0]
        val weatherDrawable = viewModel.getImageForWeather(todayWeather.weather_state_name)

        weater_icon.setImageDrawable(this.getDrawable(weatherDrawable))

        city_name_tv.text = data.title
        current_temperature.text = todayWeather.the_temp.toInt().toString()
        date_tv.text = viewModel.getDateText(todayWeather.applicable_date)

        weatherAdapter.refreshAdapter(viewModel.getTodayWeatherInfo(todayWeather))
        weatherDailyAdapter.refreshAdapter(viewModel.getWeekWeatherInfo(data.consolidated_weather))
    }

    private fun setViewsVisibilityAfterWeatherDataChange() {
        loading_tv.visibility = View.GONE
        loading_pb.visibility = View.GONE
        city_name_tv.visibility = View.VISIBLE
        date_tv.visibility = View.VISIBLE
        weather_info_recycler.visibility = View.VISIBLE
    }

    private fun setLoadingViews() {

        city_name_tv.visibility = View.INVISIBLE
        date_tv.visibility = View.INVISIBLE
        weather_info_recycler.visibility = View.INVISIBLE

        loading_pb.visibility = View.VISIBLE
        loading_tv.visibility = View.VISIBLE
        loading_tv.text = "Searching for location..."
    }

}
