package com.gps.soul.ui.screens.viewmodels

import android.location.Address
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.gps.soul.extensions.displayString
import com.gps.soul.service.LocationHelper
import com.gps.soul.service.MockLocationService
import com.gps.soul.storage.StorageManager
import com.gps.soul.ui.models.LocationEntry

class MapViewModel : ViewModel() {
    var markerPosition: MutableState<LatLng> = mutableStateOf(StorageManager.getLatestLocation())
        private set
    var address: MutableState<Address?> = mutableStateOf(null)
        private set

    var markerPositionIsFavorite: MutableState<Boolean> = mutableStateOf(false)

    fun updateMarkerPosition(latLng: LatLng) {
        markerPosition.value = latLng
        MockLocationService.instance?.latLng = latLng

        LocationHelper.reverseGeocoding(latLng) { foundAddress ->
            address.value = foundAddress
        }

        checkIfFavorite()
    }

    fun toggleFavoriteForLocation() {
        StorageManager.toggleFavoriteForPosition(currentLocationEntry())
        checkIfFavorite()
    }

    private fun checkIfFavorite() {
        val currentLocationEntry = currentLocationEntry()
        markerPositionIsFavorite.value = StorageManager.containsFavoriteEntry(currentLocationEntry)
    }

    private fun currentLocationEntry(): LocationEntry {
        return LocationEntry(
            latLng = markerPosition.value,
            addressLine = address.value?.displayString()
        )
    }

}