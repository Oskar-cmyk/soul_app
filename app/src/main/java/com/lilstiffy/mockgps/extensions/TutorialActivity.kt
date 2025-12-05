package com.lilstiffy.mockgps.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.compose.ui.layout.layoutId
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.lilstiffy.mockgps.R

class TutorialActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val devOptionsEnabled = Settings.Global.getInt(
            contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

        // Only add the first slide if developer options are NOT enabled
        if (!devOptionsEnabled) {
            addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.slide_enable_developer_options))
        }

        // Always add the manufacturer-specific slide
        val mockLocationSlideLayout = when (Build.MANUFACTURER.lowercase()) {
            "samsung" -> R.layout.slide_samsung
            "huawei" -> R.layout.slide_huawei
            "xiaomi" -> R.layout.slide_xiaomi
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
            val openSettingsButton = view?.findViewById<Button>(R.id.open_settings_button)

            // Set the click listener on the button
            openSettingsButton?.setOnClickListener {
                openAboutPhoneSettings()
            }
        }
    }

    /**
     * This function is called by the 'onClick' attribute
     * in the XML layout files.
     */
    fun openDeveloperOptions(view: View) {
        try {
            // Best case: Open developer options directly.
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            // Fallback: Show a toast and open the main settings.
            Toast.makeText(this, "Couldn't open Developer Options directly.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    /**
     * Opens the "About phone" settings screen to guide the user
     * to tap the "Build number".
     */
    private fun openAboutPhoneSettings() {
        try {
            // Tries to open the "About phone" screen directly.
            startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            Toast.makeText(this, "Now, tap 'Build number' 7 times.", Toast.LENGTH_LONG).show()
        } catch (e: ActivityNotFoundException) {
            // If that fails, falls back to the main settings screen.
            startActivity(Intent(Settings.ACTION_SETTINGS))
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
