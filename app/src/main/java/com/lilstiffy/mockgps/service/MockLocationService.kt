package com.gps.soul.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.gps.soul.R
import android.location.LocationManager.GPS_PROVIDER
import android.os.Build
import kotlinx.coroutines.*

class MockLocationService : Service() {

    companion object {
        const val TAG = "MockLocationService"
        var instance: MockLocationService? = null
        const val ACTION_SHOW_MOCK_LOCATION_DIALOG = "com.gps.soul.SHOW_MOCK_LOCATION_DIALOG"
        private const val NOTIFICATION_CHANNEL_ID = "MockLocationServiceChannel"
        const val ACTION_REQUEST_NOTIFICATION_PERMISSION = "com.gps.soul.REQUEST_NOTIFICATION_PERMISSION"
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
    private var watchdogJob: Job? = null

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

        // --- Always start foreground service FIRST ---
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // --- Now check notification permission (non-blocking) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                // Ask activity to request it but DO NOT stop mocking
                sendBroadcast(Intent(ACTION_REQUEST_NOTIFICATION_PERMISSION))
            }
        }

        // --- Register test provider (critical for Huawei/Nokia) ---
        if (!registerTestProvider()) {
            // Retry in 1 second (Huawei sometimes delays provider creation)
            serviceScope.launch {
                delay(1000)
                if (!registerTestProvider()) {
                    Log.e(TAG, "Test provider STILL failed — giving up")
                    return@launch
                }
            }
        }

        isMocking = true

        // --- Main mocking loop ---
        mockJob = serviceScope.launch {
            mockLoop()
        }

        // --- Provider watchdog loop (CRITICAL for Huawei/Nokia) ---
        watchdogJob = serviceScope.launch {
            while (isActive) {
                delay(3000)
                if (!locationManager.allProviders.contains(GPS_PROVIDER)) {
                    Log.e(TAG, "Provider disappeared — re-registering")
                    registerTestProvider()
                }
            }
        }

        Log.d(TAG, "LocationManager mock started")
    }


    @SuppressLint("MissingPermission")
    private fun stopMockingLocation() {
        if (!isMocking) return
        isMocking = false

        mockJob?.cancel() // Cancel just the job, not the whole scope
        watchdogJob?.cancel()
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

        val manufacturer = Build.MANUFACTURER
        // --- Configuration for the glitch effect ---
        val glitchEffectEnabled = true
        val randomDelayEnabled = true
        // -------------------------------------------
        var latLng: LatLng = LatLng(0.0000000001, 0.0000000001)

        var nextLongDelayTime = System.currentTimeMillis() + (10_000L..20_000L).random()

        fun computeDelay(randomDelayEnabled: Boolean): Long {
            if (!randomDelayEnabled) {
                return 1000L
            }

            val now = System.currentTimeMillis()

            return if (now >= nextLongDelayTime) {
                // Time for a long delay
                nextLongDelayTime = now + (10_000L..20_000L).random() // schedule next window
                if (manufacturer.equals("Nokia", ignoreCase = true) || manufacturer.equals("HMD Global", ignoreCase = true)) {
                (10_000L..20_000L).random()} else{
                    (5_000L..10_000L).random()
                }
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
