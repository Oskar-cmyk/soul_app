package com.gps.soul

import android.app.Application
import com.gps.soul.service.VibratorService
import com.gps.soul.storage.StorageManager

class SOULApp : Application() {
    companion object {
        lateinit var shared: SOULApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        shared = this
        StorageManager.initialise(this)
        StorageManager.clearHistory()
        VibratorService.initialise(this)
    }

}