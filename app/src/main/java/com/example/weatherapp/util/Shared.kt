package com.example.weatherapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1


fun Activity.getLocationPermission(): Boolean {
     return if (ContextCompat.checkSelfPermission(this.applicationContext,
             Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) true
     else {
         ActivityCompat.requestPermissions(
             this,
             arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
             PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
         )
         false
     }
 }

fun Context.toastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}


fun getCityFromLatLon(lat: Double, lon: Double, context: Context) : String {
    val geoCoder = Geocoder(context, Locale.getDefault())
    val addresses = geoCoder.getFromLocation(lat, lon, 1)
    return if(!addresses.isNullOrEmpty() && addresses[0].locality != null) addresses[0].locality else ""
}



