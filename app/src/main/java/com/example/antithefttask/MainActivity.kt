package com.example.antithefttask

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antithefttask.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    var bannerAd: AdView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.Button1.setOnClickListener {
            Toast.makeText(this, "pocket", Toast.LENGTH_LONG).show()
            startServiceWithAction("pocket")
        }
        binding.Button2.setOnClickListener {
            Toast.makeText(this, "charging", Toast.LENGTH_LONG).show()
            startServiceWithAction("charging")
        }
        binding.Button3.setOnClickListener {
            Toast.makeText(this, "movement", Toast.LENGTH_LONG).show()
            startServiceWithAction("movement")
        }
        loadAdMobBanner()
    }

    fun loadAdMobBanner() {
        val adUnitIdd = "ca-app-pub-3940256099942544/6300978111"


        bannerAd = AdView(this)
        bannerAd?.setAdSize(AdSize.BANNER)
        bannerAd?.adUnitId = adUnitIdd
        val adRequest = AdRequest.Builder().build()
        bannerAd?.loadAd(adRequest)

        bannerAd?.adListener = object : AdListener() {
            override fun onAdClicked() {

                Log.e("bannerAdLoad", "onAdClicked: ")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("bannerAdLoad", "onAdFailedToLoad: ")

            }

            override fun onAdImpression() {
                Log.e("bannerAdLoad", "onAdImpression: ")
            }

            override fun onAdLoaded() {
                binding.topBanner.removeAllViews()
                binding.topBanner.addView(bannerAd)



            }

        }




    }

    private fun startServiceWithAction(action: String) {
        val intent = Intent(this, SensorForegroundService::class.java)
        intent.putExtra("ACTION_TYPE", action)
        startService(intent)
    }
}