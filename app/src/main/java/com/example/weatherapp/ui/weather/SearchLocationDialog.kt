package com.example.weatherapp.ui.weather

import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import com.example.weatherapp.R
import kotlinx.android.synthetic.main.search_locaton_dialog.view.*

class SearchLocationDialog(private val viewModel: WeatherViewModel) {

    lateinit var view : View

    var dialog: Dialog? = null
    var progressBar : ProgressBar? = null

    fun createDialog(context: Context) {

        dialog = Dialog(context)

        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.search_locaton_dialog, null)
        progressBar = view.search_pb
        dialog?.setContentView(view)

        view.search_btn.setOnClickListener {
            viewModel.getLocationDataByCityName(view.search_et.text.toString())
            view.search_pb.visibility = View.VISIBLE
        }

        dialog?.create()
        dialog?.show()
    }
}