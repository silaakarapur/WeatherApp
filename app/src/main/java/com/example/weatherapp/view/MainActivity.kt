package com.example.weatherapp.view

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.TokenWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.viewmodel.MainViewModel
import com.google.android.gms.maps.GoogleMap
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var mmap: GoogleMap
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor


    private var konumYoneticisi: LocationManager? = null
    private var konumDinleyicisi: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        var cName = GET.getString("cityName", "ankara")

        city_name.setText(cName)
        viewModel.refreshData(cName!!)

        getLiveData()
        swipe.setOnRefreshListener {
            ll_data_view.visibility = View.GONE
            tv_error.visibility = View.GONE
            pb_loading.visibility = View.GONE
            var cityName = GET.getString("cityName", cName)
            city_name.setText(cityName)
            viewModel.refreshData(cityName!!)
            swipe.isRefreshing = false
        }
        image_search_city.setOnClickListener {
            val cityName = city_name.text.toString()
            SET.putString("cityName", cityName)
            SET.apply()
            viewModel.refreshData(cityName)
            getLiveData()
            city_name.text=null
        }
        var text = ""
        image_get_location.setOnClickListener {
            konumYoneticisi = getSystemService(LOCATION_SERVICE) as LocationManager

            konumDinleyicisi = LocationListener {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val list = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (list.size > 0 && list[0] != null) {
                        println("")
                        text =list[0].adminArea.toString()

                           // " il ${list[0].adminArea}"
                   //     println("Tüm bilgiler: ${list[0]}")
                        println(text)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            SET.putString("cityName",text)
            SET.apply()
            viewModel.refreshData(text)
            getLiveData()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                konumDinleyicisi?.let {
                    konumYoneticisi?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, it)
                }
            }


        }



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    konumDinleyicisi?.let {
                        konumYoneticisi?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 0, 0f, it
                        )
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getLiveData() {
        viewModel.weatherData.observe(this, Observer { data ->
            data?.let {
                ll_data_view.visibility = View.VISIBLE
                pb_loading.visibility = View.GONE
                degree.text = data.main.temp.toString()
                country_code.text = data.sys.country
                tv_city_name.text = data.name
                tv_humidity.text = data.main.humidity.toString()
                tv_wind_speed.text = data.wind.speed.toString()
                tv_lat.text = data.coord.lat.toString()
                tv_lon.text = data.coord.lon.toString()

                Glide.with(this)
                    .load("https://openweathermap.org/img/wn/" + data.weather.get(0).icon + "@2x.png")
                    .into(img_weather_icon)


            }
            degree.text = data.main.temp.toString() + "°C"
            tv_humidity.text = "Nem: " + data.main.humidity.toString() + "%"
            tv_wind_speed.text = "Rüzgar Hızı " + data.wind.speed.toString()
            tv_lat.text = "Enlem: " + data.coord.lat.toString()
            tv_lon.text = "Boylam: " + data.coord.lon.toString()
        })
        viewModel.weatherLoad.observe(this, Observer { load ->
            load?.let {
                if (it) {
                    pb_loading.visibility = View.VISIBLE
                    tv_error.visibility = View.GONE
                    ll_data_view.visibility = View.GONE
                } else {
                    pb_loading.visibility = View.GONE
                }


            }
        })
        viewModel.weatherError.observe(this, Observer { error ->
            error?.let {
                if (it) {
                    ll_data_view.visibility = View.GONE
                    pb_loading.visibility = View.GONE
                    tv_error.visibility = View.VISIBLE
                } else {
                    tv_error.visibility = View.GONE
                }

            }
        })
    }

}