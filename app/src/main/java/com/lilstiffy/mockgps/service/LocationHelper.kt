package com.gps.soul.service

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import com.gps.soul.SOULApp


object LocationHelper {
    val DEFAULT_LOCATION = LatLng(0.0000000, 0.0000000)

    // Geocoding
    fun reverseGeocoding(latLng: LatLng, result: (Address?) -> Unit) {
        val geocoder = Geocoder(SOULApp.shared.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { response ->
                val address = response.firstOrNull()
                result(address)
            }
        } else {
            @Suppress("DEPRECATION")
            val response = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = response?.firstOrNull()
            result(address)
        }
    }

    /**
     * @param searchterm Search term the user wants to do a coordinate look up for
     * @param result lambda containing [LatLng] object if a result was found from the Geocoding lookup.
     */
    fun geocoding(searchterm: String, result: (LatLng?) -> Unit) {
        val geocoder = Geocoder(SOULApp.shared.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(
                searchterm,
                1
            ) { response ->
                val address = response.firstOrNull()
                if (address == null) {
                    result(null)
                    return@getFromLocationName
                }

                result(LatLng(address.latitude, address.longitude))
            }
        } else {
            @Suppress("DEPRECATION")
            val response = geocoder.getFromLocationName(searchterm, 1)
            val address = response?.firstOrNull()

            if (address == null) {
                result(null)
                return
            }

            result(LatLng(address.latitude, address.longitude))
        }
    }
}
