package com.example.weatherapp.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.weatherapp.R
import com.example.weatherapp.util.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.koin.android.viewmodel.ext.android.viewModel


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: MapsViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        observeCurrentGpsLocation()
        observeGpsState()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun observeCurrentGpsLocation() = viewModel.currentGpsLocation.observe(this, Observer {
        if(it == null) return@Observer

        when(it) {
            is Result.Success -> {
                val latLon = LatLng(it.data.latitude, it.data.longitude)
                viewModel.createNewMarker(latLon, this)
            }
            is Result.Error -> {
                this.toastMessage(it.exception.message ?: getString(R.string.unknown_error_message))
            }
        }
    })

    private fun observeGpsState() = viewModel.liveGpsState.observe(this, Observer {
        if(it == null) return@Observer
        viewModel.getCurrentLocation(this)
    })

    override fun onMapReady(googleMap: GoogleMap) {
       viewModel.onMapReady(googleMap, this)
    }

}
