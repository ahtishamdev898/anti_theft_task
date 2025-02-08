package com.example.antithefttask.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antithefttask.R
import com.example.antithefttask.data.SensorForegroundService
import com.example.antithefttask.data.adSize
import com.example.antithefttask.databinding.ActivityMainBinding
import com.example.antithefttask.data.getAdSize
import com.example.antithefttask.data.loadAdMobBanner
import com.example.antithefttask.data.loadCollapsable
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        this.loadAdMobBanner(binding.topBanner)
        this.loadCollapsable(binding.collaps)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.Button1.setOnClickListener {

            if (!SensorForegroundService.isServiceRunning) {
                Toast.makeText(this, "Cover/Uncover The Promixity Sensor", Toast.LENGTH_LONG).show()

                startServiceWithAction("pocket")
            } else {
                Toast.makeText(this, "Service Already Running", Toast.LENGTH_LONG).show()
            }
        }
        binding.Button2.setOnClickListener {
            if (!SensorForegroundService.isServiceRunning) {
                Toast.makeText(this, "Now Remove/Place Charger", Toast.LENGTH_LONG).show()
                startServiceWithAction("charging")


            } else {
                Toast.makeText(this, "Service Already Running", Toast.LENGTH_LONG).show()
            }
        }
        binding.Button3.setOnClickListener {
            if (!SensorForegroundService.isServiceRunning) {
                Toast.makeText(this, "Shake the Device", Toast.LENGTH_LONG).show()
                startServiceWithAction("movement")
            } else {
                Toast.makeText(this, "Service Already Running", Toast.LENGTH_LONG).show()
            }

        }

        binding.Button4.setOnClickListener {
            if (SensorForegroundService.isServiceRunning) {
                stopSensorService()
                binding.textView2.text = "Service is Not Running"
                binding.textView2.setTextColor(getColor(R.color.red))
            } else {
                Toast.makeText(this, "Service is Not Running", Toast.LENGTH_LONG).show()
            }

        }

    }
    private fun stopSensorService() {
        val serviceIntent = Intent(this, SensorForegroundService::class.java)
        stopService(serviceIntent)
    }

    private fun startServiceWithAction(action: String) {
        binding.textView2.text = "${action} service is running"
        binding.textView2.setTextColor(getColor(R.color.green))
        val intent = Intent(this, SensorForegroundService::class.java)
        intent.putExtra("ACTION_TYPE", action)
        startService(intent)
    }


    override fun onResume() {
        super.onResume()
        if (!SensorForegroundService.isServiceRunning) {
            binding.textView2.text = "Service is Not Running"
            binding.textView2.setTextColor(getColor(R.color.red))

        }
    }

}