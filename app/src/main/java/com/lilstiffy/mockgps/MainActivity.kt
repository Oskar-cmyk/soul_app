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
import androidx.core.content.ContextCompat.startActivity
import androidx.compose.ui.platform.LocalContext // <-- Import this
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
                        MockLocationDialog(
                            onDismiss = { showDialog = false },
                            onConfirm = {
                                startActivity(Intent(this, TutorialActivity::class.java))
                                showDialog = false
                            }
                        )
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
        try {
            // The best-case scenario: Dev options are on.
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (e1: Exception) {
            try {
                // The fallback: Dev options are off, so show the "About" screen.
                Toast.makeText(this, "Enable Developer Options by tapping 'Build number' 7 times.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            } catch (e2: Exception) {
                // The final fallback: Just open the main settings screen.
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }
}

@Composable
fun MockLocationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val context = LocalContext.current // Get the context here

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enable Mock Location") },
        text = { Text("To use this feature, you must set this app as the mock location app in developer settings. Follow the tutorial to enable it.") },
        confirmButton = {
            Button(onClick = {
                // Use the context to create the Intent and start the activity
                val intent = Intent(context, TutorialActivity::class.java)
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Show Tutorial")
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
