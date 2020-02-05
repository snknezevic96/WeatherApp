package com.example.weatherapp.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.R
import com.example.weatherapp.Result
import com.example.weatherapp.data.Repository
import com.example.weatherapp.data.remote.LocationResponse
import com.example.weatherapp.data.remote.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel(private val repository: Repository,
                    private val context: Context) : ViewModel() {

    val locationData = MutableLiveData<Result<LocationResponse>>()
    val weatherData = MutableLiveData<Result<WeatherResponse>>()

    fun getLocationDataByCityName(cityName: String) {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getLocationDataByCityName(cityName).enqueue(object : Callback<List<LocationResponse>> {
                override fun onFailure(call: Call<List<LocationResponse>>, t: Throwable) {
                    Log.i("API_MESSAAGE", t.message ?: "unknown error")
                    locationData.postValue(Result.Error(Exception(t)))
                }

                override fun onResponse(call: Call<List<LocationResponse>>, response: Response<List<LocationResponse>>) {
                    if(response.body() != null) {
                        when(response.code()) {
                            200 -> {
                                if(response.body()!!.isNotEmpty())
                                    locationData.postValue(Result.Success(response.body()!![0]))
                                else
                                    locationData.postValue(Result.Error(Exception("City not found.")))
                            }
                            else -> locationData.postValue(Result.Error(Exception(response.message())))
                        }
                    }
                    else
                        locationData.postValue(Result.Error(Exception(response.message())))
                }

            })
        }
    }

    fun getWeatherData(woeid: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getWeatherByLocationId(woeid).enqueue(object: Callback<WeatherResponse> {
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherData.postValue(Result.Error(Exception(t)))
                }

                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if(response.body() != null) {
                        when(response.code()) {
                            200 -> {
                                weatherData.postValue(Result.Success(response.body()!!))
                            }
                            else -> weatherData.postValue(Result.Error(Exception(response.message())))
                        }
                    }
                    else
                        weatherData.postValue(Result.Error(Exception(response.message())))
                }

            })
        }
    }

    fun getImageForWeather(type: String) : Int {
        return when(type) {
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

    fun parseDateToDayName(date: String) = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-d")).dayOfWeek.name.take(3)
    fun parseStringToDate(date: String) =  LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-d"))
}