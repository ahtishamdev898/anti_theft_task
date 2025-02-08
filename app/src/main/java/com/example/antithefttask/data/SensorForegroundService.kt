package com.example.antithefttask.data

import android.app.*
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.ServiceInfo
import android.hardware.*
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.antithefttask.R

class SensorForegroundService : Service(), SensorEventListener {

    companion object {
        var isServiceRunning = false
    }

    private lateinit var sensorManager: SensorManager
    private var mediaPlayer: MediaPlayer? = null
    private var accelerometer: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var actionType: String? = null
    private var isPocketMode = false

    private var unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_USER_PRESENT) {
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        actionType = intent?.getStringExtra("ACTION_TYPE")

        registerSensors()
        registerReceivers()
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

    private fun registerReceivers() {
        if (actionType == "charging") {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(powerConnectedReceiver, filter)
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
    }

    private val powerConnectedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_DISCONNECTED -> {
                    if (actionType == "charging") {
                        startAlarm()
                    }

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
                        startAlarm()
                    }
                } else {
                    if (isPocketMode) {
                        isPocketMode = false

                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    private fun startAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.cute_ring_tone_soft)
            if (mediaPlayer != null) {
                mediaPlayer?.setOnCompletionListener {
                    Log.d(TAG, "Sound playback completed.")
                    startAlarm()
                }
                mediaPlayer?.start()
                Log.d(TAG, "Sound playback started.")
            } else {
                Log.e(TAG, "Failed to create MediaPlayer.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }




    override fun onDestroy() {
        super.onDestroy()
           unRegisteredResources()

    }

    private fun unRegisteredResources() {
        try {
            unregisterReceiver(unlockReceiver)
        } catch (e: Exception) {
            Log.w("SensorService", "Unlock receiver not registered or already unregistered.")
        }
        try {
            unregisterReceiver(powerConnectedReceiver)
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.w("SensorService", "Power receiver not registered or already unregistered.")
        }
        Log.e("SSSS", "stopService:2 ")

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}


