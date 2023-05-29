package com.tirthdalwadi.weatherquery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.tirthdalwadi.weatherquery.databinding.ActivitySplashScreenBinding
import com.tirthdalwadi.weatherquery.databinding.ActivitySplashScreenBinding.inflate

@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = inflate(layoutInflater)
        setContentView(binding.root)


        val top = AnimationUtils.loadAnimation(this, R.anim.top)

        binding.ivSplashScreen.animation = top
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        },3000)
    }
}