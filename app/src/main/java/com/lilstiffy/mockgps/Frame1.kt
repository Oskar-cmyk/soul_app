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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.center
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.*
import com.google.gson.annotations.SerializedName




// Enum to represent the current screen being displayed
enum class Screen {
    MAIN, ABOUT, FAQ
}
// Change lat -> latitude and lon -> longitude
// Ensure you have this import

data class IpLocationResponse(
    @SerializedName("latitude") val ipLat: Double, // Renamed from latitude
    @SerializedName("longitude") val ipLng: Double, // Renamed from longitude
    @SerializedName("city") val city: String?,
    @SerializedName("country_name") val country: String? // 'country_name' is more reliable in their JSON
)


@Composable
fun MockToggleCircle(isMocking: Boolean,
                     onToggle: () -> Unit,
                     modifier: Modifier = Modifier,
                     backgroundColor: Color,
                     textColor: Color
) {
    var showWhiteCircle by remember { mutableStateOf(false) }

    // --- BREATHING ANIMATION SETUP ---
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f, // How much "air" it takes in
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    // Animate the main circle (fade & scale)
    val alpha by animateFloatAsState(
        targetValue = if (isMocking) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "circleAlpha"
    )

    // The scale used when clicking (expanding to fill screen)
    val expansionScale by animateFloatAsState(
        targetValue = if (isMocking) 5.85f else 1f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "circleScale"
    )

    // Combine breathing and expansion:
    // If mocking, use expansionScale. If NOT mocking, use breathingScale.
    val finalScale = if (isMocking) expansionScale else breathingScale

    LaunchedEffect(isMocking) {
        if (isMocking) {
            kotlinx.coroutines.delay(1400)
            showWhiteCircle = true
        } else {
            showWhiteCircle = false
        }
    }

    Box(
        modifier = modifier
            .size(100.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = androidx.compose.material.ripple.rememberRipple(
                    bounded = false,
                    radius = 70.dp
                ),
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        // Main circle (original color)
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = finalScale
                    scaleY = finalScale
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                }
                .clip(CircleShape)
                .background(textColor)
        )

        // Reborn white circle (Keep this as is)
        if (showWhiteCircle) {
            var whiteCircleTriggered by remember { mutableStateOf(false) }
            val whiteScale by animateFloatAsState(
                targetValue = if (whiteCircleTriggered) 1f else 0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "whiteCircleScale"
            )

            LaunchedEffect(showWhiteCircle) {
                if (showWhiteCircle) whiteCircleTriggered = true
            }

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = whiteScale * breathingScale
                        scaleY = whiteScale * breathingScale

                    }
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
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
    textColor: Color,
    locationToMock: LatLng?,
    locationReady: Boolean
) {
    // Animate the opacity based on whether the location is ready
    val textAlpha by animateFloatAsState(
        targetValue = if (locationReady) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "textFadeIn"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Center toggle circle
        MockToggleCircle(
            isMocking = isMocking,
            onToggle = {
                onToggle(activity.toggleMocking(locationToMock ?: LatLng(0.0, 0.0)))            },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-43).dp),
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (locationReady && locationToMock == null && !isMocking) {
                    // Show this ONLY when off and no data found
                    Text(
                        text = "latitude longitude",
                        fontSize = 16.sp,
                        color = textColor
                    )
                    Text(
                        text = "no data available",
                        fontSize = 16.sp,
                        color = textColor,
                        modifier = Modifier.graphicsLayer { alpha = textAlpha }
                    )
                } else {
                    Text(
                        text = "latitude longitude",
                        fontSize = 16.sp,
                        color = textColor
                    )
                    Text(
                        text = if (isMocking) "0.00000, 0.00000"
                        else "${locationToMock?.latitude ?: 0.0}, ${locationToMock?.longitude ?: 0.0}",
                        fontSize = 16.sp,
                        color = textColor,
                        modifier = Modifier.graphicsLayer { alpha = textAlpha }
                    )
                }
            }
        }
    }
}

@Composable
fun Frame1Responsive(
    modifier: Modifier = Modifier,
    activity: MainActivity
) {
    var isMocking by remember { mutableStateOf(activity.isServiceMocking()) }
    var currentScreen by remember { mutableStateOf(Screen.MAIN) } // State for navigation
    val lightBg = Color(0xffe0f9ff)
    val darkBg = Color(0xff2364c5)

    // Define colors based on the mocking state

    var delayedMocking by remember { mutableStateOf(isMocking) }
    var delayedBackground by remember { mutableStateOf(isMocking) }

    // 1. New state for the dynamic location
    var dynamicLocation by remember { mutableStateOf<LatLng?>(null) }

    var locationReady by remember { mutableStateOf(false) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // When app comes back to front, ask the activity for the true state
                isMocking = activity.isServiceMocking()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // 2. Fetch location from IP on launch
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://ipapi.co/json/")
                val connection = url.openConnection() as java.net.HttpURLConnection

                // Add a User-Agent to prevent the API from blocking the "anonymous" request
                connection.setRequestProperty("User-Agent", "Android-MockGps-App")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val locationData = com.google.gson.Gson().fromJson(responseString, IpLocationResponse::class.java)

                withContext(Dispatchers.Main) {
                    if (locationData.ipLat != 0.0) {
                        dynamicLocation = LatLng(locationData.ipLat, locationData.ipLng)                    }
                    // --- TRIGGER READY STATE ---
                    locationReady = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Even if it fails, we show the "baked" data so the app isn't stuck empty
                    locationReady = true
                }

            }
        }
    }


    LaunchedEffect(isMocking) {
        delay(700) // delay before text reacts
        delayedMocking = isMocking
    }
    LaunchedEffect(isMocking) {
        if (isMocking) {
            // Turning ON → delay before text changes
            kotlinx.coroutines.delay(700)
        }
        // Turning OFF → no delay
        delayedBackground = isMocking
    }
    LaunchedEffect(isMocking) {
        if (isMocking) {
            delay(3000) // Wait 3 seconds
            showSuccessPopup = true
        } else {
            showSuccessPopup = false // Reset when turned off
        }
    }
    if (showSuccessPopup && currentScreen == Screen.MAIN) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSuccessPopup = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showSuccessPopup = false }) {
                    Text("OK", color = Color(0xff2364c5))
                }
            },
            title = { Text("You are now on NULL Island", fontWeight = FontWeight.Bold) },
            text = { Text("You may now leave the app and use your phone as before. For an easy check, open your map app. Turn SOUL OFF when you need your phone to locate you again physically.") }, // Your custom text
            containerColor = Color.White,
            titleContentColor = Color(0xff2364c5),
            textContentColor = Color.Black
        )
    }
    val textColor by animateColorAsState(
        targetValue = if (delayedMocking) lightBg else darkBg,
        animationSpec = tween(durationMillis = 400),
        label = "delayedTextColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (delayedBackground) darkBg else lightBg,
        label = "backgroundColor"
    )

    val blurRadius by animateDpAsState(
        targetValue = if (isMocking) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 1600),
        label = "blurRadius"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isMocking) 0.4f else 0f,
        animationSpec = tween(durationMillis = 1600),
        label = "overlayAlpha"
    )
    val revealProgress by animateFloatAsState(
        targetValue = if (isMocking) 1f else 0f,
        animationSpec = tween(durationMillis = 1700, easing = FastOutSlowInEasing),
        label = "revealProgress"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        //  between background and content
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (revealProgress > 0f) {
                    // Calculate the center based on the actual drawing area
                    // Subtract 50.dp from the Y coordinate to match your button's offset
                    val toggleCenter = Offset(
                        x = size.width / 2f,
                        y = (size.height / 2f)
                    )

                    val radius = size.maxDimension * revealProgress

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xff2364c5), Color.Transparent),
                            center = toggleCenter, // Use the calculated center
                            radius = radius
                        ),
                        radius = radius,
                        center = toggleCenter // Use the calculated center
                    )
                }
            }

        }



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
                .padding(top = 100.dp)
                .padding(bottom = 20.dp)
        ) {
            when (currentScreen) {
                Screen.MAIN ->
                    MainContent(
                        activity = activity,
                        isMocking = isMocking,
                        onToggle = { isMocking = it },
                        textColor = textColor,
                        locationToMock = dynamicLocation,
                        locationReady = locationReady // Pass it here
                    )


                Screen.ABOUT ->
                    AboutScreen(textColor)

                Screen.FAQ ->
                    FaqScreen(textColor)

            }
        }
    }
}
