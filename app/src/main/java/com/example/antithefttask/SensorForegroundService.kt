package com.example.antithefttask

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.hardware.*
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class SensorForegroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var actionType: String? = null
    private var isPocketMode = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        actionType = intent?.getStringExtra("ACTION_TYPE")
        startForegroundService()
        registerSensors()
        registerPowerReceiver()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "service_channel")
            .setContentTitle("Anti Theft Service Running")
            .setContentText("Phone Monitoringing Service")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(1, notification)
    }

    private fun registerSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (actionType == "movement") {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        } else if (actionType == "pocket") {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            proximitySensor?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
    }

    private fun registerPowerReceiver() {
        if (actionType == "charging") {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(powerConnectedReceiver, filter)
        }
    }

    private val powerConnectedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    Log.e("Charger State", "Power connected")
                    startAlarm()
                }

                Intent.ACTION_POWER_DISCONNECTED -> {
                    Log.e("Charger State", "Power disconnected")
                    startAlarm()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER && actionType == "movement") {
                val acceleration =
                    Math.sqrt((it.values[0] * it.values[0] + it.values[1] * it.values[1] + it.values[2] * it.values[2]).toDouble())
                if (acceleration > 15) {
                    Log.e("Movement Detected", "$acceleration > 15")
                    startAlarm()
                }
            } else if (it.sensor.type == Sensor.TYPE_PROXIMITY && actionType == "pocket") {
                if (it.values[0] < (proximitySensor?.maximumRange ?: 0f)) {
                    if (!isPocketMode) {
                        isPocketMode = true
                        Log.e("Pocket Mode", "Device in pocket")
                        startAlarm()
                    }
                } else {
                    if (isPocketMode) {
                        isPocketMode = false
                        Log.e("Pocket Mode", "Device out of pocket")
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @SuppressLint("ShowToast")
    private fun startAlarm() {
        Toast.makeText(this, "Triggered", Toast.LENGTH_LONG).show()
        Log.e("Alarm", "Triggered")
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        unregisterReceiver(powerConnectedReceiver)
    }
}


