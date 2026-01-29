package com.gps.soul.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.gps.soul.R


class TutorialActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showStatusBar(true)
        val devOptionsEnabled = Settings.Global.getInt(
            contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

        // Only add the developer options slide if they are NOT enabled
        if (!devOptionsEnabled) {
            // Choose the manufacturer-specific layout for enabling developer options
            val enableDevOptionsSlideLayout = when (Build.MANUFACTURER.lowercase()) {
                "samsung" -> R.layout.slide_samsung
                "huawei" -> R.layout.slide_huawei
                "xiaomi" -> R.layout.slide_xiaomi
                else -> R.layout.slide_enable_dev // Default layout
            }
            addSlide(AppIntroCustomLayoutFragment.newInstance(enableDevOptionsSlideLayout))
        }

        // Always add the manufacturer-specific slide
        val mockLocationSlideLayout = when (Build.MANUFACTURER.lowercase()) {
            "samsung" -> R.layout.slide_enable_samsung
            "huawei" -> R.layout.slide_enable_huawei
            "xiaomi" -> R.layout.slide_enable_xiaomi
            else -> R.layout.slide_mock_location_setup // Default layout
        }
        addSlide(AppIntroCustomLayoutFragment.newInstance(mockLocationSlideLayout))

        isWizardMode = true
    }

    // This function is called every time the slide changes.
    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)

        // Check if the current slide is the one with our button
        if (newFragment is AppIntroCustomLayoutFragment &&
            newFragment.arguments?.getInt("layoutResId") == R.layout.slide_enable_developer_options) {
            val view = newFragment.view
            // Find the button by its ID

        }
    }
    override fun onResume() {
        super.onResume()
        // Every time the user comes back to the app, check if they fixed the settings
        if (isSystemReadyForMocking()) {
            finish() // Closes TutorialActivity and reveals MainActivity
        }
    }

    /**
     * Checks if Developer Options are enabled AND if this app
     * is selected as the Mock Location provider.
     */
    private fun isSystemReadyForMocking(): Boolean {
        // 1. Check Developer Options
        val devOptionsEnabled = Settings.Global.getInt(
            contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

        if (!devOptionsEnabled) return false

        // 2. Check if this app is the selected Mock Location App
        // We do this by trying to add a test provider.
        // If it throws a SecurityException, we are NOT the selected app.
        val locationManager = getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        return try {
            locationManager.addTestProvider(
                android.location.LocationManager.GPS_PROVIDER,
                false, false, false, false, true, true, true,
                0, 5
            )
            locationManager.removeTestProvider(android.location.LocationManager.GPS_PROVIDER)
            true // Success! We are the mock provider
        } catch (e: SecurityException) {
            false // Not selected yet
        } catch (e: Exception) {
            // Some devices might throw other exceptions if GPS is disabled
            false
        }
    }
    /**
     * This function is called by the 'onClick' attribute
     * in the XML layout files.
     */
    fun openDeveloperOptions(view: View) {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            Toast.makeText(
                this,
                "Go to Select mock location app→ Select SOUL as Mock location application",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // Fallback to main settings
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: Exception) {}
        }
    }

    /**
     * Opens the "About phone" settings screen to guide the user
     * to tap the "Build number".
     */
    fun openAboutPhoneSettings(view: View) {
        try {
            startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            Toast.makeText(
                this,
                "Go to About phone → Software information → tap Build number 7 times",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // absolute last resort, but prevents crash
        }
    }





    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}
