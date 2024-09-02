package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the SearchView
        searchView = binding.searchView

        // Check for network connection before fetching data
        if (isNetworkAvailable(this)) {
            // Fetch data for default location (e.g., Greater Noida)
            fetchWeatherData("Greater Noida") // Replace with your desired default location
        } else {
            Toast.makeText(this, "No internet connection available.", Toast.LENGTH_SHORT).show()
        }

        updateCurrentDayAndDate()
        searchCity()
    }
    private fun changeImageAccordingToWeatherCondition(conditions: String) {
        when (conditions) {
            // Clear/Sunny
            "clear sky", "sky is clear", "few clouds", "scattered clouds" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            // Cloudy
            "broken clouds", "overcast clouds", "haze","overcast", "mist", "fog", "foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            // Rain
            "light rain", "moderate rain", "heavy intensity rain", "very heavy rain",
            "extreme rain", "freezing rain", "light intensity shower rain", "shower rain",
            "heavy intensity shower rain", "ragged shower rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            // Snow
            "light snow", "moderate snow", "heavy intensity snow", "sleet", "shower sleet",
            "light rain and snow", "rain and snow", "light shower snow", "shower snow",
            "heavy shower snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            // Other (use a default)
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
    }

    private fun searchCity() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query) // Pass the entered city name to fetchWeatherData
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // You can handle text changes here if needed
                return true
            }
        })
    }

    // Updated fetchWeatherData to use the entered city
    private fun fetchWeatherData(city: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(APIInterface::class.java)

        val response = retrofit.getWeatherData(city, "58eb0428f8d2525c34534e85502c86ff", "metric") // Replace with your API key
        response.enqueue(object : Callback<Weather> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val temp = responseBody.main.temp
                        val humidity = responseBody.main.humidity
                        val windSpeed = responseBody.wind.speed
                        val conditions = responseBody.weather[0].description
                        val seaLevel = responseBody.main.sea_level
                        val sunrise = responseBody.sys.sunrise
                        val sunset = responseBody.sys.sunset
                        val maxTemp = responseBody.main.temp_max
                        val minTemp = responseBody.main.temp_min
                        val location = responseBody.name
                        val country = responseBody.sys.country
                        val conditionWeatherStatus=responseBody.weather[0].description
                        Log.d("WeatherApp", "Temperature: $temp") // Log the temperature

                        runOnUiThread {
                            binding.temp.text = "$temp °C"
                            binding.humidity.text = "$humidity%"
                            binding.windSpeed.text = "$windSpeed m/s"
                            binding.maxTemp.text = "Max Temp: $maxTemp °C"
                            binding.minTemp.text = "Min Temp: $minTemp °C"
                            binding.locationText.text = "$location, $country"
                            binding.conditionWeather.text = conditions
                            binding.conditionWeatherStatus.text = conditionWeatherStatus
                            binding.seaLevel.text = "$seaLevel hPa"
                            binding.sunriseTime.text = formatTimestampToTime(sunrise)
                            binding.sunsetTime.text = formatTimestampToTime(sunset)
                        }

                        changeImageAccordingToWeatherCondition(conditionWeatherStatus)

                        val iconId = responseBody.weather[0].icon
                        // Update the animation based on the weather icon (e.g., sunny, cloudy, rainy)
                        when (iconId) {
                            "01d", "01n" -> binding.lottieAnimationView.setAnimation(R.raw.sun) // Sunny
                            "02d", "02n", "03d", "03n" -> binding.lottieAnimationView.setAnimation(R.raw.cloud) // Partly Cloudy
                            "04d", "04n" -> binding.lottieAnimationView.setAnimation(R.raw.cloud) // Cloudy
                            "09d", "09n", "10d", "10n" -> binding.lottieAnimationView.setAnimation(R.raw.rain) // Rain
                            else -> binding.lottieAnimationView.setAnimation(R.raw.sun) // Default to sunny
                        }
                        binding.lottieAnimationView.playAnimation()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.e("WeatherApp", "Network Error: ")
            }
        })
    }

    // Helper function to check network availability
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        for (network in networks) {
            val networkInfo = connectivityManager.getNetworkInfo(network)
            if (networkInfo != null && networkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateCurrentDayAndDate() {
        val calendar = Calendar.getInstance()
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        binding.day.text = dayOfWeekFormat.format(calendar.time)
        binding.date.text = dateFormat.format(calendar.time)
    }
    private fun formatTimestampToTime(timestamp: Int): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val netDate = (timestamp * 1000).toLong() // Convert seconds to milliseconds

        // Create a Calendar object with the timestamp in UTC
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")) // UTC time zone
        calendar.timeInMillis = netDate

        // Convert the UTC time to local time
        val localCalendar = Calendar.getInstance(Locale.getDefault())
        localCalendar.timeInMillis = calendar.timeInMillis

        return sdf.format(localCalendar.time)
    }

}