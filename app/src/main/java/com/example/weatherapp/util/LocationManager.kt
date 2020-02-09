package com.example.weatherapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

class LocationManager(private val context: Context) {

    private val REQUEST_CHECK_SETTINGS = 0x1

    val liveGpsLatLon = MutableLiveData<Result<Location>>()
    val liveGpsState = MutableLiveData<Boolean>()
    private var mLocationManager: LocationManager = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun getCurrentDeviceLocation() {

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100f, mLocationListener)
    }

    private val mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if(location != null) {
                liveGpsLatLon.value = Result.Success(location)
            }
            else {
                liveGpsLatLon.value = Result.Error(Exception("No location found!"))
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {
            liveGpsState.value = true
        }
        override fun onProviderDisabled(p0: String?) {
            liveGpsState.value = false
        }
    }

    fun gpsState() : Boolean {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    fun requestGps(activity: Activity) {
        val googleApiClient = GoogleApiClient.Builder(context).addApi(LocationServices.API).build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2.toLong()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

        result.setResultCallback {
            val status: Status = it.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> Log.i("SUCCESS", "All location settings are satisfied.")
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i("GPS OFF","Requesting GPS ")
                    try {
                        status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.i("ERROR", "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                    Log.i("ERROR", "Location settings are unavailable!!!")
            }
        }
    }
}