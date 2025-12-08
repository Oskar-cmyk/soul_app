package com.lilstiffy.mockgps

import android.Manifest
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.maps.model.LatLng
import com.lilstiffy.mockgps.extensions.TutorialActivity
import com.lilstiffy.mockgps.service.MockLocationService
import com.lilstiffy.mockgps.service.VibratorService
import com.lilstiffy.mockgps.storage.StorageManager
import com.lilstiffy.mockgps.ui.screens.MapScreen
import com.lilstiffy.mockgps.ui.theme.MockGpsTheme

class MainActivity : ComponentActivity() {
    private var mockLocationService: MockLocationService? = null
        private set(value) {
            field = value
            MockLocationService.instance = value
        }

    private var isBound = false
    private var showDialog by mutableStateOf(false)

    // New launcher for the notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // After permission is granted, try to start mocking again
            mockLocationService?.toggleMocking()
        } else {
            Toast.makeText(this, "Notification permission is required for the service to run.", Toast.LENGTH_LONG).show()
        }
    }

    // Updated receiver to handle both dialogs and permissions
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MockLocationService.ACTION_SHOW_MOCK_LOCATION_DIALOG -> showDialog = true
                MockLocationService.ACTION_REQUEST_NOTIFICATION_PERMISSION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MockLocationService.MockLocationBinder
            mockLocationService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            isBound = false
        }
    }

    fun toggleMocking(latLng: LatLng): Boolean {
        if (isBound) {
            val service = mockLocationService ?: return false
            val wasMocking = service.isMocking

            service.latLng = latLng
            service.toggleMocking()

            val isNowMocking = service.isMocking

            if (isNowMocking) {
                Toast.makeText(this, "Mocking location...", Toast.LENGTH_SHORT).show()
                VibratorService.vibrate()
            } else {
                if (wasMocking) {
                    Toast.makeText(this, "Stopped mocking location...", Toast.LENGTH_SHORT).show()
                    VibratorService.vibrate()
                }
            }
            return isNowMocking
        } else {
            Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        StorageManager.clearHistory()

        val serviceIntent = Intent(this, MockLocationService::class.java)

        startService(serviceIntent) // Using startService() is also fine here and more common.

        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        setContent {
            MockGpsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(activity = this)
                    if (showDialog) {
                        MockLocationDialog(
                            onDismiss = { showDialog = false },
                            onShowTutorial = {
                                startActivity(Intent(this, TutorialActivity::class.java))
                                showDialog = false
                            },
                            onOpenSettings = {
                                openDeveloperSettings()
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }

        // Updated intent filter to listen for both broadcasts
        val intentFilter = IntentFilter().apply {
            addAction(MockLocationService.ACTION_SHOW_MOCK_LOCATION_DIALOG)
            addAction(MockLocationService.ACTION_REQUEST_NOTIFICATION_PERMISSION)
        }

        ContextCompat.registerReceiver(this, broadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(broadcastReceiver)
    }

    private fun openDeveloperSettings() {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (e1: Exception) {
            try {
                Toast.makeText(this, "Enable Developer Options by tapping 'Build number' 7 times.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            } catch (e2: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }
}

@Composable
fun MockLocationDialog(
    onDismiss: () -> Unit,
    onShowTutorial: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enable Mock Location") },
        text = { Text("To use this feature, you must set this app as the mock location app in developer settings.") },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = onShowTutorial) {
                Text("Show Tutorial")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MockGpsTheme {}
}
