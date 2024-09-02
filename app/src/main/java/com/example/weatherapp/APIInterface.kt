package com.example.weatherapp

import com.google.android.gms.awareness.state.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("weather")
    fun getWeatherData(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<com.example.weatherapp.Weather>
}