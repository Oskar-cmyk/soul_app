package com.lilstiffy.mockgps.ui.screens

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lilstiffy.mockgps.MainActivity
import com.lilstiffy.mockgps.R
import com.lilstiffy.mockgps.extensions.roundedShadow
import com.lilstiffy.mockgps.service.LocationHelper
import com.lilstiffy.mockgps.storage.StorageManager
import com.lilstiffy.mockgps.ui.components.FavoritesListComponent
import com.lilstiffy.mockgps.ui.components.FooterComponent
import com.lilstiffy.mockgps.ui.screens.viewmodels.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    activity: MainActivity,
) {
    val scope = rememberCoroutineScope()

    var isMocking by remember { mutableStateOf(false) }


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }




    Box(modifier = Modifier.fillMaxSize()) {


        Column(
            modifier = Modifier.statusBarsPadding()
        ) {




        }

        FooterComponent(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(1f)
                .navigationBarsPadding()

                .zIndex(32f)
                .roundedShadow(16.dp),
            address = mapViewModel.address.value,
            latLng = mapViewModel.markerPosition.value,
            isMocking = isMocking,
            onStart = { isMocking = activity.toggleMocking(mapViewModel.markerPosition.value) },
            onFavorite = { mapViewModel.toggleFavoriteForLocation() }
        )

        if (showBottomSheet) {
            FavoritesListComponent(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                data = StorageManager.favorites,
                onEntryClicked = { clickedEntry ->
                    if (isMocking) {
                        Toast.makeText(
                            activity,
                            "You can't switch location while mocking",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@FavoritesListComponent
                    }
                    mapViewModel.apply {
                        mapViewModel.updateMarkerPosition(clickedEntry.latLng)
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    }
                }
            )
        }

    }
}
