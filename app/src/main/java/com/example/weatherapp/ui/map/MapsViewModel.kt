package com.example.weatherapp.ui.map

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.weatherapp.ui.weather.WeatherActivity
import com.example.weatherapp.util.LocationManager
import com.example.weatherapp.util.getCityFromLatLon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsViewModel(private val locationManager: LocationManager) : ViewModel() {

    val currentGpsLocation = locationManager.liveGpsLatLon
    val liveGpsState = locationManager.liveGpsState

    var googleMap: GoogleMap? = null
    var currentMarker: Marker? = null

    init {
        if(!gpsState())
            locationManager.getCurrentDeviceLocation()
    }


    private fun gpsState() = locationManager.gpsState()

    private fun requestGps(activity: Activity) = locationManager.requestGps(activity)


    fun getCurrentLocation(activity: Activity) {

        when {
            !locationManager.gpsState() -> locationManager.requestGps(activity)
            else -> locationManager.getCurrentDeviceLocation()
        }
    }

    private fun updateLocationUI(activity: Activity) {

        try {
            if (gpsState()) {
                googleMap!!.isMyLocationEnabled = true
                googleMap!!.uiSettings.isMyLocationButtonEnabled = true
            }
            else {
                googleMap!!.isMyLocationEnabled = false
                googleMap!!.uiSettings.isMyLocationButtonEnabled = false
                requestGps(activity)
            }
        } catch (e: SecurityException) {
            Log.e("SECURITY_ERROR", e.message ?: "unknown")
        }
    }

    fun createNewMarker(latLon: LatLng, activity: Activity) {
        currentMarker?.remove()

        val cityName = getCityFromLatLon(
            latLon.latitude,
            latLon.longitude,
            activity
        )

        currentMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLon)
                .title(cityName)
                .snippet("Click here for weather info")
        )

        currentMarker?.showInfoWindow()
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLon, 8.5f))

        googleMap?.setOnInfoWindowClickListener { marker ->
            if(marker == currentMarker) {
                val intent = Intent(activity, WeatherActivity::class.java)
                intent.putExtra("cityName", marker.title.toString())
                activity.startActivity(intent)
            }
        }
    }

    fun onMapReady(googleMap: GoogleMap, activity: Activity) {
        this.googleMap = googleMap

        updateLocationUI(activity)
        getCurrentLocation(activity)

        googleMap.setOnMapClickListener { latLon ->
            createNewMarker(latLon, activity)
        }
    }
}