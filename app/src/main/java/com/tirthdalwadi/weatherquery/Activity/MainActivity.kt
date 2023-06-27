package com.tirthdalwadi.weatherquery.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.tirthdalwadi.weatherquery.R
import com.tirthdalwadi.weatherquery.databinding.ActivityMainBinding
import com.tirthdalwadi.weatherquery.databinding.ActivityMainBinding.inflate
import com.tirthdalwadi.weatherquery.roomDB.CityName
import com.tirthdalwadi.weatherquery.roomDB.CityNameDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: CityNameDatabase
    private var location: Location? = null
    private var newPlace: String? = null
    private var flag: Boolean = false
    private val PERMISSION_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)

        database = CityNameDatabase.getDatabase(this.applicationContext)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getUserLocationWeather()

        binding.ivPressMe.setOnClickListener {
            closeKeyBoard()
            binding.pbLoading.isVisible = true
            newPlace = binding.etvCityName.text.toString()
            callAPI(newPlace!!)
        }
        binding.etvCityName.setOnEditorActionListener(actionListener)
    }


    private fun initialSearch() {
        var cityNameList: List<CityName>
        GlobalScope.launch {
            cityNameList = database.getCityNameDao().getCityName()
            if (cityNameList.isNotEmpty()) {
                newPlace = cityNameList[0].city
                callAPI(newPlace!!)
            }
        }
    }

    private fun getUserLocationWeather() {
        Log.d("Debug", "Entered getUserLocationWeather()")
        binding.pbLoading.isVisible = true
        if (checkPermissions()) {
            Log.d("Debug", "Permission Given")
            if (isLocationEnabled()) {
                Log.d("Debug", "Location is enabled")
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                } else {
                    fusedLocationProviderClient!!.lastLocation.addOnCompleteListener { task ->
                        location = task.result
                        Log.d("Debug", "$location")
                        if (location != null) {
                            try {
                                val locationLatLng =
                                    "${location!!.latitude},${location!!.longitude}"
                                Log.d("Debug", "api called for current Location")
                                callAPI(locationLatLng)
                                newPlace = locationLatLng
                            } catch (e: IOException) {
                            }
                        } else {
                            binding.pbLoading.isVisible = false
                            initialSearch()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "GPS is Turned OFF",
                    Toast.LENGTH_SHORT
                ).show()
                Toast.makeText(
                    this,
                    "Showing weather of last searched city",
                    Toast.LENGTH_LONG
                ).show()
                binding.pbLoading.isVisible = false
                initialSearch()
            }
        } else {
            binding.pbLoading.isVisible = false
            requestPermission()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun checkPermissions(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    private val actionListener =
        OnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    newPlace = binding.etvCityName.text.toString()
                    callAPI(newPlace!!)
                }
            }
            false
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
                getUserLocationWeather()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun callAPI(place: String) {
        val url =
            "https://api.weatherapi.com/v1/current.json?key=68f3a3fc07c74f6c808111719231003&q="
        val newUrl = "$url$place&aqi=no"

// Request a json object response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, newUrl, null,
            { response ->
                Log.d("MainActivity", "Message: $response")
                flag = true
                val location = response.getJSONObject("location")
                val city = location.get("name")
                val country = location.get("country")
                val current = response.getJSONObject("current")
                val currentTemp = current.get("temp_c")
                val wind = current.get("wind_kph")
                val localDateTime = current.get("last_updated")
                val cloud = current.get("cloud")
                val humidity = current.get("humidity")
                val weatherConditionObject = current.getJSONObject("condition")
                val weatherCondition = weatherConditionObject.get("text")
                val now = localDateTime.toString().substring(11, 13)

                binding.tvWeatherCondition.text = "$weatherCondition"
                binding.tvLocation.text = "$city, $country"
                binding.tvCurrTemp.text = "$currentTemp"
                binding.tvWindSpeed.text = "Wind Speed: $wind kph"
                binding.tvCloud.text = "Cloud: $cloud"
                binding.tvHumidity.text = "Humidity: $humidity"
                binding.etvCityName.text.clear()
                binding.llLocation.isVisible = true

                if (now.toInt() >= 18 || now.toInt() <= 5) {
                    binding.clMainBody.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.night, null)
                    binding.tvCurrTemp.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvWeatherCondition.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvTempUnit.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvWindSpeed.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.etvCityName.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.etvCityName.setHintTextColor(Color.parseColor("#CDCECE"))
                    binding.ivPressMe.setColorFilter(Color.parseColor("#FFFFFF"))
                    binding.etvCityName.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    binding.tvCloud.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvHumidity.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    binding.clMainBody.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.sunny, null)
                    binding.tvCurrTemp.setTextColor(Color.parseColor("#000000"))
                    binding.tvWeatherCondition.setTextColor(Color.parseColor("#000000"))
                    binding.tvTempUnit.setTextColor(Color.parseColor("#000000"))
                    binding.tvWindSpeed.setTextColor(Color.parseColor("#000000"))
                    binding.etvCityName.setTextColor(Color.parseColor("#000000"))
                    binding.etvCityName.setHintTextColor(Color.parseColor("#000000"))
                    binding.ivPressMe.setColorFilter(Color.parseColor("#000000"))
                    binding.etvCityName.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    binding.tvCloud.setTextColor(Color.parseColor("#000000"))
                    binding.tvHumidity.setTextColor(Color.parseColor("#000000"))
                }

                binding.tvWeatherCondition.isVisible = true
                binding.tvLocation.isVisible = true
                binding.tvWindSpeed.isVisible = true
                binding.tvCloud.isVisible = true
                binding.tvHumidity.isVisible = true
                binding.llTemperatureValue.isVisible = true
                binding.pbLoading.isVisible = false
                updateLatestCityName()
            },

            {
                Log.d("MainActivity", "Error Message: $it")
                Toast.makeText(
                    this,
                    "City Not Found !!! \n Please Enter a Valid City.",
                    Toast.LENGTH_LONG
                ).show()

                binding.clMainBody.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.mainbg, null)
                binding.etvCityName.setTextColor(Color.parseColor("#000000"))
                binding.etvCityName.setHintTextColor(Color.parseColor("#000000"))
                binding.etvCityName.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#000000"))
                binding.ivPressMe.setColorFilter(Color.parseColor("#000000"))

                binding.etvCityName.text.clear()
                binding.tvLocation.text = ""
                binding.llLocation.isVisible = false
                binding.tvCurrTemp.text = ""
                binding.tvWindSpeed.text = ""
                binding.tvHumidity.text = ""
                binding.tvCloud.text = ""
                binding.tvWeatherCondition.text = ""
                binding.tvWeatherCondition.isVisible = false
                binding.tvCloud.isVisible = false
                binding.tvHumidity.isVisible = false
                binding.tvWindSpeed.isVisible = false
                binding.llTemperatureValue.isVisible = false
                binding.pbLoading.isVisible = false
            }
        )
        MySingleton.getInstance(this.applicationContext).addToRequestQueue(jsonObjectRequest)
    }


    private fun closeKeyBoard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun updateLatestCityName() {
        GlobalScope.launch {
            database.getCityNameDao().deleteAll()
            database.getCityNameDao().insertCityName(CityName(0, newPlace!!))
        }
    }
}


class MySingleton constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: MySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: MySingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}