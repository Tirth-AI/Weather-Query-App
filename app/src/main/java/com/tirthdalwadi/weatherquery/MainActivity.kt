package com.tirthdalwadi.weatherquery

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tirthdalwadi.weatherquery.databinding.ActivityMainBinding
import com.tirthdalwadi.weatherquery.databinding.ActivityMainBinding.inflate
import java.math.RoundingMode.valueOf
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivPressMe.setOnClickListener {
            closeKeyBoard()
            binding.pbLoading.isVisible = true
            callAPI()
        }


        binding.etvCityName.setOnEditorActionListener(actionListener)
    }
    private val actionListener =
        OnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> callAPI()
            }
            false
        }


    @SuppressLint("SetTextI18n")
    private fun callAPI() {

        val place = binding.etvCityName.text
        val url = "https://api.weatherapi.com/v1/current.json?key=68f3a3fc07c74f6c808111719231003&q="
        val newUrl = "$url$place&aqi=no"

// Request a json object response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, newUrl, null,
            { response ->
                Log.d("MainActivity", "Message: $response" )
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
                val now = localDateTime.toString().substring(11,13)

                binding.tvWeatherCondition.text = "$weatherCondition"
                binding.tvLocation.text = "$city, $country"
                binding.tvCurrTemp.text = "$currentTemp"
                binding.tvWindSpeed.text = "Wind Speed: $wind kph"
                binding.tvCloud.text = "Cloud: $cloud"
                binding.tvHumidity.text = "Humidity: $humidity"
                binding.etvCityName.text.clear()
                binding.llLocation.isVisible = true

                if(now.toInt() >= 18 || now.toInt() <= 5)
                {
                    binding.clMainBody.background = ResourcesCompat.getDrawable(resources, R.drawable.night, null)
                    binding.tvCurrTemp.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvWeatherCondition.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvTempUnit.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvWindSpeed.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.etvCityName.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.etvCityName.setHintTextColor(Color.parseColor("#CDCECE"))
                    binding.ivPressMe.setColorFilter(Color.parseColor("#FFFFFF"))
                    binding.etvCityName.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    binding.tvCloud.setTextColor(Color.parseColor("#FFFFFF"))
                    binding.tvHumidity.setTextColor(Color.parseColor("#FFFFFF"))
                }
                else
                {
                    binding.clMainBody.background = ResourcesCompat.getDrawable(resources, R.drawable.sunny, null)
                    binding.tvCurrTemp.setTextColor(Color.parseColor("#000000"))
                    binding.tvWeatherCondition.setTextColor(Color.parseColor("#000000"))
                    binding.tvTempUnit.setTextColor(Color.parseColor("#000000"))
                    binding.tvWindSpeed.setTextColor(Color.parseColor("#000000"))
                    binding.etvCityName.setTextColor(Color.parseColor("#000000"))
                    binding.etvCityName.setHintTextColor(Color.parseColor("#000000"))
                    binding.ivPressMe.setColorFilter(Color.parseColor("#000000"))
                    binding.etvCityName.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
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
            },

            {
                Log.d("MainActivity", "Error Message: $it" )
                Toast.makeText(this, "City Not Found !!! \n Please Enter a Valid City.", Toast.LENGTH_LONG).show()

                binding.clMainBody.background = ResourcesCompat.getDrawable(resources, R.drawable.mainbg, null)
                binding.etvCityName.setTextColor(Color.parseColor("#000000"))
                binding.etvCityName.setHintTextColor(Color.parseColor("#000000"))
                binding.etvCityName.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
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
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}