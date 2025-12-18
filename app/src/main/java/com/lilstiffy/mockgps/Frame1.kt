package com.lilstiffy.mockgps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.blur


import com.gps.soul.MainActivity
import com.gps.soul.storage.StorageManager
import com.gps.soul.ui.components.FavoritesListComponent
import com.gps.soul.ui.screens.viewmodels.MapViewModel
import kotlinx.coroutines.launch

@Composable
fun MockToggleCircle(
    isMocking: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(textColor)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Frame1Responsive(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel(),
    activity: MainActivity
) {
    val scope = rememberCoroutineScope()
    var isMocking by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Define colors based on the mocking state
    val backgroundColor = if (isMocking) Color(0xff2364c5) else Color(0xffe0f9ff)
    val textColor = if (isMocking) Color(0xffe0f9ff) else Color(0xff2364c5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp)
    ) {
        // Top-left menu
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ABOUT",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "FAQ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        // Center toggle circle
        MockToggleCircle(
            isMocking = isMocking,
            onToggle = {
                isMocking = activity.toggleMocking(
                    mapViewModel.markerPosition.value
                )
            },
            modifier = Modifier.align(Alignment.Center),
            backgroundColor = backgroundColor,
            textColor = textColor
        )

        // Bottom text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isMocking) "ON NULL ISLAND" else "OFF NULL ISLAND",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = if (isMocking) "latitude longitude\n0.00000, 0.00000" else "latitude longitude\n46.0561281, 14.5057642",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}
