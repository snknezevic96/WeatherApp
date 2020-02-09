package com.example.weatherapp.ui.weather


import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.R
import com.example.weatherapp.data.Repository
import com.example.weatherapp.data.model.ConsolidatedWeather
import com.example.weatherapp.data.remote.LocationResponse
import com.example.weatherapp.data.remote.WeatherResponse
import com.example.weatherapp.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeatherViewModel(
    private val repository: Repository,
    private val locationManager: LocationManager
) : ViewModel() {

    val locationData = MutableLiveData<Result<LocationResponse>>()
    val weatherData = MutableLiveData<Result<WeatherResponse>>()

    val locationState = MutableLiveData<LocationState>()
    val gpsState = locationManager.liveGpsState
    val currentGpsLocation = locationManager.liveGpsLatLon

    var locationPermissionGranted = false

    fun getCurrentLocation(activity: Activity) {
        if (locationState.value != LocationState.IN_PROGRESS) {

            locationState.value = when {
                !locationPermissionGranted -> {
                    activity.getLocationPermission()
                    LocationState.LOCATION_PERMISSION_NOT_GRANTED
                }
                !locationManager.gpsState() -> {
                    locationManager.requestGps(activity)
                    LocationState.GSP_TURNED_OFF
                }
                else -> {
                    locationManager.getCurrentDeviceLocation()
                    LocationState.IN_PROGRESS
                }
            }
        }
    }

    fun clearGpsLocation() {
        currentGpsLocation.value = null
    }

    fun setLocationStateDone() {
        locationState.value = LocationState.DONE
    }

    fun checkPermission(activity: Activity) {
        locationPermissionGranted = activity.getLocationPermission().also { granted ->
            if (granted && !locationManager.gpsState())
                locationManager.getCurrentDeviceLocation()
        }
    }

    fun refreshCurrentLocation(activity: Activity) {
        //locationRequested = true

        if (gpsState.value == null)
            gpsState.value = locationManager.gpsState()
        getCurrentLocation(activity)
    }

    fun initializeLocationListener() {
        //used only for case when location permission granted, but location is off
        //call will initialize location lister (need for listening on gps provider state change)
        locationManager.getCurrentDeviceLocation()
    }

    fun requestGps(activity: Activity) = locationManager.requestGps(activity)

    fun getImageForWeather(type: String): Int {
        return when (type) {
            "Snow" -> R.drawable.ic_snow
            "Sleet" -> R.drawable.ic_sleet
            "Hail" -> R.drawable.ic_hail
            "Thunderstorm" -> R.drawable.ic_thunderstorm
            "Heavy Rain" -> R.drawable.ic_heavy_rain
            "Light Rain" -> R.drawable.ic_light_rain
            "Showers" -> R.drawable.ic_showers
            "Heavy Cloud" -> R.drawable.ic_heavy_cloud
            "Light Cloud" -> R.drawable.ic_light_cloud
            "Clear" -> R.drawable.ic_clear
            else -> R.drawable.ic_error
        }
    }

    fun getDateText(dateString: String): String {

        val dateName = parseDateToDayName(dateString)
        val date = parseStringToDate(dateString)
        val mm = if (date.monthValue < 10) "0${date.monthValue}" else date.monthValue.toString()
        val ddMM = "${date.dayOfMonth}-$mm"
        return "$dateName, $ddMM"
    }

    fun getTodayWeatherInfo(weather: ConsolidatedWeather) = listOf(
        Pair("${weather.humidity}%", R.drawable.ic_humidity),
        Pair("${weather.wind_speed.toInt()}mph", R.drawable.ic_wind),
        Pair("${weather.min_temp.toInt()}°", R.drawable.ic_min),
        Pair("${weather.max_temp.toInt()}°", R.drawable.ic_max)
    )

    fun getWeekWeatherInfo(weatherList: List<ConsolidatedWeather>): List<Pair<String, Int>> {
        val weatherPerDay = mutableListOf<Pair<String, Int>>()

        weatherList.takeLast(5).forEach {
            val date = parseDateToDayName(it.applicable_date)
            val icon = getImageForWeather(it.weather_state_name)
            weatherPerDay.add(Pair(date, icon))
        }
        return weatherPerDay
    }

    private fun parseDateToDayName(date: String) =
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-d")).dayOfWeek.name.take(3)

    private fun parseStringToDate(date: String) =
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-d"))

    fun getLocationDataByLatLon(lat: Double, lon: Double, context: Context) : String {
        val cityName = getCityFromLatLon(lat, lon, context)
        getLocationDataByCityName(cityName)
        return cityName
    }

    /****************************************** API ***********************************************/

    fun getLocationDataByCityName(cityName: String) {
            locationState.value = LocationState.IN_PROGRESS

            viewModelScope.launch(Dispatchers.IO) {
                repository.getLocationDataByCityName(cityName).enqueue(object : Callback<List<LocationResponse>> {
                        override fun onFailure(call: Call<List<LocationResponse>>, t: Throwable) {

                            Log.i("API_MESSAAGE", t.message ?: "unknown error")
                            locationData.postValue(Result.Error(Exception(t)))
                        }

                        override fun onResponse(call: Call<List<LocationResponse>>, response: Response<List<LocationResponse>>) {

                            if (response.body() != null) {
                                when (response.code()) {
                                    200 -> {
                                        if (response.body()!!.isNotEmpty())
                                            locationData.postValue(Result.Success(response.body()!![0]))
                                        else
                                            locationData.postValue(Result.Error(Exception("City not found.")))
                                    }
                                    else -> locationData.postValue(Result.Error(Exception(response.message())))
                                }
                            } else
                                locationData.postValue(Result.Error(Exception(response.message())))
                        }

                    })
            }
    }

    fun getWeatherData(woeid: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getWeatherByLocationId(woeid).enqueue(object : Callback<WeatherResponse> {
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherData.postValue(Result.Error(Exception(t)))
                }

                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.body() != null) {
                        when (response.code()) {
                            200 -> {
                                weatherData.postValue(Result.Success(response.body()!!))
                            }
                            else -> weatherData.postValue(Result.Error(Exception(response.message())))
                        }
                    } else
                        weatherData.postValue(Result.Error(Exception(response.message())))
                }

            })
        }
    }
}