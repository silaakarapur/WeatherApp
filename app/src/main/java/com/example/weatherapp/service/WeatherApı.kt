package com.example.weatherapp.service

import com.example.weatherapp.model.WeatherModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.openweathermap.org/data/2.5/weather?q=izmir&APPID=b11ae98a748cc8783ee5635b0add1cd1
interface WeatherApı {
@GET("data/2.5/weather?&units=metric&APPID=b11ae98a748cc8783ee5635b0add1cd1")
fun getData(
    @Query("q") cityName: String
):Single<WeatherModel>
}