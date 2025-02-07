package com.example.antithefttask

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.hardware.*
import android.media.AudioManager
import android.media.ToneGenerator
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
    private var toneGenerator: ToneGenerator? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        actionType = intent?.getStringExtra("ACTION_TYPE")
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        registerSensors()
        registerPowerReceiver()
        createNotificationChannel()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Foreground Service Channel"  // Customize
            val descriptionText = "Channel for foreground service notifications" // Customize
            val importance = NotificationManager.IMPORTANCE_DEFAULT // Or higher
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            startForegroundService()
        }
    }
    private fun startForegroundService() {

     val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Anti-Theft Service") // More descriptive
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Ensure this is correct!
            .build()

        val foregroundServiceType = when (actionType) {
            "movement", "pocket" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            "charging" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC // Or appropriate type
            else -> 0
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, foregroundServiceType)
        } else {
            startForeground(1, notification)
        }
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
        toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 500)
        Handler(Looper.getMainLooper()).postDelayed({
           stopSelf()
        }, 600)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
       // sensorManager.unregisterListener(this)
        //unregisterReceiver(powerConnectedReceiver)
    }
}


