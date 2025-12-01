package com.lilstiffy.mockgps.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.lilstiffy.mockgps.R
import kotlinx.coroutines.*

class MockLocationService : Service() {

    companion object {
        const val TAG = "MockLocationService"
        var instance: MockLocationService? = null
        private const val NOTIFICATION_CHANNEL_ID = "MockLocationServiceChannel"
        private const val NOTIFICATION_ID = 69
    }

    private lateinit var locationManager: LocationManager
    var isMocking = false
        private set
    var latLng: LatLng = LatLng(0.0000000001, 0.0000000001)

    private var mockJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = MockLocationBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        stopMockingLocation()
        super.onDestroy()
    }

    fun toggleMocking() {
        if (isMocking) stopMockingLocation() else startMockingLocation()
    }

    // ⬇⬇⬇ START MOCKING WITH LOCATION MANAGER ONLY ⬇⬇⬇
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun startMockingLocation() {
        if (isMocking) return
        isMocking = true

        registerTestProvider()

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        mockJob = GlobalScope.launch(Dispatchers.IO) {
            mockLoop()
        }

        Log.d(TAG, "LocationManager mock started")
    }

    @SuppressLint("MissingPermission")
    private fun stopMockingLocation() {
        if (!isMocking) return
        isMocking = false

        mockJob?.cancel()
        unregisterTestProvider()

        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "LocationManager mock stopped")
    }
    // ⬆⬆⬆ END MOCKING WITH LOCATION MANAGER ⬆⬆⬆

    private fun registerTestProvider() {
        try {
            locationManager.addTestProvider(
                LocationManager.GPS_PROVIDER,
                false, false, false, false,
                true, true, true,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE
            )
        } catch (_: Exception) {}

        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
        } catch (_: Exception) {}

        Log.d(TAG, "Test provider registered")
    }

    private fun unregisterTestProvider() {
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {}
    }

    private fun createNotification(): Notification {
        val lat = String.format("%.5f", latLng.latitude)
        val lng = String.format("%.5f", latLng.longitude)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Mock GPS Active")
            .setContentText("Location: $lat, $lng")
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Mock Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun mockLoop() {
        while (isMocking) {
            val corrected = if (latLng.latitude == 0.0 && latLng.longitude == 0.0)
                LatLng(0.0000000001, 0.0000000001)
            else
                latLng

            val loc = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = corrected.latitude
                longitude = corrected.longitude
                accuracy = 1f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }

            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)

            Log.d(TAG, "Mocked location: ${loc.latitude}, ${loc.longitude}")

            delay(300L)
        }
    }

    inner class MockLocationBinder : Binder() {
        fun getService(): MockLocationService = this@MockLocationService
    }
}
