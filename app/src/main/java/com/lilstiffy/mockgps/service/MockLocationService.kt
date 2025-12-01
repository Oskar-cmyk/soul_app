package com.lilstiffy.mockgps.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.lilstiffy.mockgps.R
import com.lilstiffy.mockgps.storage.StorageManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MockLocationService : Service() {

    companion object {
        const val TAG = "MockLocationService"
        var instance: MockLocationService? = null
        private const val NOTIFICATION_CHANNEL_ID = "MockLocationServiceChannel"
        private const val NOTIFICATION_ID = 69
    }

    var isMocking = false
        private set

    lateinit var latLng: LatLng

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return MockLocationBinder()
    }

    override fun onDestroy() {
        stopMockingLocation()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    fun toggleMocking() {
        if (isMocking) stopMockingLocation() else startMockingLocation()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun startMockingLocation() {
        StorageManager.addLocationToHistory(latLng)

        if (!isMocking) {
            isMocking = true

            fusedLocationClient.setMockMode(true).addOnSuccessListener {
                Log.d(TAG, "Mock mode set to true")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to set mock mode to true", it)
            }

            // Promote to foreground service
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)

            GlobalScope.launch(Dispatchers.IO) {
                mockLocation()
            }
            Log.d(TAG, "Mock location started")
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopMockingLocation() {
        if (isMocking) {
            isMocking = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            fusedLocationClient.setMockMode(false).addOnSuccessListener {
                Log.d(TAG, "Mock mode set to false")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to set mock mode to false", it)
            }
        }
        Log.d(TAG, "Mock location stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mock Location Service"
            val descriptionText = "Displays a persistent notification while mocking location."
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val lat = String.format("%.4f", latLng.latitude)
        val lng = String.format("%.4f", latLng.longitude)
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Mock GPS")
            .setContentText("Mocking location active at $lat, $lng")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private suspend fun mockLocation() {
        while (isMocking) {
            val correctedLatLng = if (latLng.latitude == 0.0 && latLng.longitude == 0.0) {
                LatLng(0.0000000001, 0.0000000001)
            } else {
                latLng
            }

            val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = correctedLatLng.latitude
                longitude = correctedLatLng.longitude
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    isFromMockProvider = true
                }
                altitude = 12.5
                accuracy = 1.0f
                speed = 0.0f
                bearing = 0.0f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bearingAccuracyDegrees = 0.1f
                    verticalAccuracyMeters = 1.0f
                    speedAccuracyMetersPerSecond = 0.1f
                }
            }

            fusedLocationClient.setMockLocation(mockLocation).addOnSuccessListener {
                Log.d(TAG, "Mock location set successfully")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to set mock location", it)
            }
            // Sleep for a duration to simulate location update frequency
            kotlinx.coroutines.delay(200L)
        }
    }

    inner class MockLocationBinder : Binder() {
        fun getService(): MockLocationService {
            return this@MockLocationService
        }
    }
}
