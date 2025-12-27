package com.gps.soul

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.gps.soul.ui.screens.AboutScreen
import com.gps.soul.ui.screens.FaqScreen

// Enum to represent the current screen being displayed
enum class Screen {
    MAIN, ABOUT, FAQ
}

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
        // You can add an icon or text here if you want
    }
}
@Composable
fun Header(
    textColor: Color,
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        when (currentScreen) {

            Screen.MAIN -> {
                // ABOUT
                Text(
                    text = "ABOUT",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.clickable {
                        onScreenChange(Screen.ABOUT)
                    }
                )

                // FAQ
                Text(
                    text = "FAQ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.clickable {
                        onScreenChange(Screen.FAQ)
                    }
                )
            }

            Screen.ABOUT, Screen.FAQ -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentScreen == Screen.ABOUT) "ABOUT" else "FAQ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "X",
                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.clickable {
                            onScreenChange(Screen.MAIN)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent(
    activity: MainActivity,
    isMocking: Boolean,
    onToggle: (Boolean) -> Unit,
    textColor: Color
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Center toggle circle
        MockToggleCircle(
            isMocking = isMocking,
            onToggle = {
                val glitchLocation = LatLng(46.0561281, 14.5057642)
                onToggle(activity.toggleMocking(glitchLocation))
            },
            modifier = Modifier.align(Alignment.Center),
            backgroundColor = Color.Transparent,
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = if (isMocking)
                    "latitude longitude\n0.00000, 0.00000"
                else
                    "latitude longitude\n46.0561281, 14.5057642",
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun Frame1Responsive(
    modifier: Modifier = Modifier,
    activity: MainActivity
) {
    var isMocking by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.MAIN) } // State for navigation

    // Define colors based on the mocking state
    val backgroundColor = if (isMocking) Color(0xff2364c5) else Color(0xffe0f9ff)
    val textColor = if (isMocking) Color(0xffe0f9ff) else Color(0xff2364c5)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

        // HEADER (never moves)
        Header(
            textColor = textColor,
            currentScreen = currentScreen,
            onScreenChange = { currentScreen = it }
        )

        // CONTENT (changes)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 160.dp)
        ) {
            when (currentScreen) {
                Screen.MAIN ->
                    MainContent(
                        activity = activity,
                        isMocking = isMocking,
                        onToggle = { isMocking = it },
                        textColor = textColor
                    )

                Screen.ABOUT ->
                    AboutScreen(textColor)

                Screen.FAQ ->
                    FaqScreen(textColor)
            }
        }
    }
}
