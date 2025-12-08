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
        const val ACTION_SHOW_MOCK_LOCATION_DIALOG = "com.lilstiffy.mockgps.SHOW_MOCK_LOCATION_DIALOG"
        private const val NOTIFICATION_CHANNEL_ID = "MockLocationServiceChannel"
        private const val NOTIFICATION_ID = 69
    }

    private lateinit var locationManager: LocationManager
    var isMocking = false
        private set
    var latLng: LatLng = LatLng(0.0000000001, 0.0000000001)

    // Create a lifecycle-aware coroutine scope
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
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
        serviceJob.cancel() // Cancel the entire scope when the service is destroyed
        super.onDestroy()
    }

    fun toggleMocking() {
        if (isMocking) stopMockingLocation() else startMockingLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startMockingLocation() {
        if (isMocking) return

        if (!registerTestProvider()) {
            // Failed to register, broadcast should have been sent
            return
        }

        isMocking = true

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Use the service's own scope, not GlobalScope
        mockJob = serviceScope.launch {
            mockLoop()
        }

        Log.d(TAG, "LocationManager mock started")
    }

    @SuppressLint("MissingPermission")
    private fun stopMockingLocation() {
        if (!isMocking) return
        isMocking = false

        mockJob?.cancel() // Cancel just the job, not the whole scope
        unregisterTestProvider()

        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "LocationManager mock stopped")
    }

    @SuppressLint("MissingPermission")
    private fun registerTestProvider(): Boolean {
        // Proactively remove any zombie provider left from a bad session
        unregisterTestProvider()

        try {
            locationManager.addTestProvider(
                LocationManager.GPS_PROVIDER,
                false, false, false, false,
                true, true, true,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE
            )
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            Log.d(TAG, "Test provider registered")
            return true
        } catch (e: SecurityException) {
            sendBroadcast(Intent(ACTION_SHOW_MOCK_LOCATION_DIALOG))
            Log.e(TAG, "SecurityException: Mock location app not set.", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register test provider", e)
            return false
        }
    }

    @SuppressLint("MissingPermission")
    private fun unregisterTestProvider() {
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            Log.d(TAG, "Test provider unregistered successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister test provider. It may have already been removed.", e)
        }
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
    private suspend fun glitchEffect() {
        Log.d(TAG, "GLITCH: Setting temporary glitch location.")
        val glitchLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = -0.00000001
            longitude = 0.0
            accuracy = 1f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        }
        try {
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, glitchLocation)
        } catch (e: Exception) {
            Log.e(TAG, "Glitch effect failed.", e)
        }
        delay(1000L) // How long to show the glitch location (1 second)
    }

    @SuppressLint("MissingPermission")
    private suspend fun mockLoop() {
        // --- Configuration for the glitch effect ---
        val glitchEffectEnabled = true
        val randomDelayEnabled = true
        // -------------------------------------------

        var nextLongDelayTime = System.currentTimeMillis() + (10_000L..20_000L).random()

        fun computeDelay(randomDelayEnabled: Boolean): Long {
            if (!randomDelayEnabled) {
                return 1000L
            }

            val now = System.currentTimeMillis()

            return if (now >= nextLongDelayTime) {
                // Time for a long delay
                nextLongDelayTime = now + (10_000L..20_000L).random() // schedule next window
                5_000L
            } else {
                2000L
            }
        }

        while (isMocking) {
            val corrected = if (latLng.latitude == 0.0 && latLng.longitude == 0.0)
                LatLng(0.00000001, 0.0000000) // Avoid Null Island
            else
                latLng

            val loc = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = corrected.latitude
                longitude = corrected.longitude
                accuracy = 1f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

            }

            try {
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set mock location", e)
            }

            Log.d(TAG, "Mocked location: ${loc.latitude}, ${loc.longitude}")

            val delayMillis = computeDelay(randomDelayEnabled)
            delay(delayMillis)

            if (glitchEffectEnabled && isMocking) {
                glitchEffect()
            }
        }
    }

    inner class MockLocationBinder : Binder() {
        fun getService(): MockLocationService = this@MockLocationService
    }
}
