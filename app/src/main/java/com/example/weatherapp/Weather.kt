package com.example.weatherapp

data class Weather(
    val base: String, // stations
    val clouds: Clouds,
    val cod: Int, // 200
    val coord: Coord,
    val dt: Int, // 1724694743
    val id: Int, // 1270216
    val main: Main,
    val name: String, // Hāthras
    val rain: Rain,
    val sys: Sys,
    val timezone: Int, // 19800
    val visibility: Int, // 10000
    val weather: List<WeatherX>,
    val wind: Wind
)