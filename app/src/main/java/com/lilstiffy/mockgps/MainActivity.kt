package com.lilstiffy.mockgps

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.google.android.gms.maps.model.LatLng
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

    private val mockLocationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MockLocationService.ACTION_SHOW_MOCK_LOCATION_DIALOG) {
                showDialog = true
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
            mockLocationService?.latLng = latLng
            mockLocationService?.toggleMocking()
            if (mockLocationService?.isMocking == true) {
                Toast.makeText(this, "Mocking location...", Toast.LENGTH_SHORT).show()
                VibratorService.vibrate()
                return true
            } else if (mockLocationService?.isMocking == false) {
                Toast.makeText(this, "Stopped mocking location...", Toast.LENGTH_SHORT).show()
                VibratorService.vibrate()
                return false
            }
        } else {
            Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        StorageManager.clearHistory()

        setContent {
            MockGpsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(activity = this)
                    if (showDialog) {
                        MockLocationDialog(onDismiss = { showDialog = false }, onConfirm = {
                            openDeveloperSettings()
                            showDialog = false
                        })
                    }
                }
            }
        }

        val serviceIntent = Intent(this, MockLocationService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        registerReceiver(mockLocationReceiver, IntentFilter(MockLocationService.ACTION_SHOW_MOCK_LOCATION_DIALOG))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(mockLocationReceiver)
    }

    private fun openDeveloperSettings() {
        val intent = if (Settings.System.getString(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) == "0") {
            // Developer options are disabled, open "About phone"
            Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
        } else {
            // Developer options are enabled
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        }
        startActivity(intent)
    }
}

@Composable
fun MockLocationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enable Mock Location") },
        text = { Text("To use this feature, you must set this app as the mock location app in developer settings. Please follow the tutorial to enable this setting.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MockGpsTheme {}
}
