package com.gps.soul.ui.screens

import android.content.Context // Import Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

// Your data class
data class FaqSection(
    val title: String,
    val content: String
)

@Composable
fun FaqScreen(textColor: Color) {
    // 1. Get Context to access assets
    val context = LocalContext.current
    val cacheFileName = "faq_cache.json" // Our dynamic local file
    val assetFileName = "faq_backup.json" // Our read-only safety backup


    // State to hold the data
    var sections by remember { mutableStateOf<List<FaqSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var isOfflineMode by remember { mutableStateOf(false) }
    // URL to your raw JSON file
    val jsonUrl = "https://codeberg.org/Oskar-cmyk/mockgps_android/raw/branch/main_des/app/src/main/assets/faq_backup.json"
    // Fetch data when the screen launches
    // ... inside FaqScreen ...

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Try fetching from the internet
                val jsonString = URL(jsonUrl).readText()
                val listType = object : TypeToken<List<FaqSection>>() {}.type
                val fetchedList: List<FaqSection> = Gson().fromJson(jsonString, listType)

                // 2. SUCCESS: Save this new JSON to internal storage for next time
                context.openFileOutput(cacheFileName, Context.MODE_PRIVATE).use {
                    it.write(jsonString.toByteArray())
                }

                withContext(Dispatchers.Main) {
                    sections = fetchedList
                    isOfflineMode = false
                    isLoading = false
                }
            } catch (e: Exception) {
                // 3. NETWORK FAILED: Try the dynamic cache first, then the hardcoded asset
                try {
                    val cacheFile = File(context.filesDir, cacheFileName)
                    val jsonString = if (cacheFile.exists()) {
                        // Load from the saved cache
                        cacheFile.readText()
                    } else {
                        // Load from assets if cache doesn't exist yet
                        context.assets.open(assetFileName).bufferedReader().use { it.readText() }
                    }

                    val listType = object : TypeToken<List<FaqSection>>() {}.type
                    val localList: List<FaqSection> = Gson().fromJson(jsonString, listType)

                    withContext(Dispatchers.Main) {
                        sections = localList
                        isOfflineMode = true
                        isLoading = false
                    }
                } catch (fallbackException: Exception) {
                    withContext(Dispatchers.Main) {
                        errorText = "Failed to load content."
                        isLoading = false
                    }
                }
            }
        }
    }

    // ... (The rest of your UI code remains exactly the same) ...
    var startAnimation by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    // Trigger animation only after data is loaded
    LaunchedEffect(isLoading) {
        if (!isLoading && sections.isNotEmpty()) {
            startAnimation = true
        }
    }

    val offsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 600), label = "offset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600), label = "alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (errorText != null) {
            Text(
                text = errorText!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Define the fade gradient (transparent at top, opaque in middle, transparent at bottom)
            // You can adjust the color stops (0f, 0.05f, etc.) to control the fade height
            val fadeBrush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.05f to Color.Black, // Top fade ends here (5% down)
                0.95f to Color.Black, // Bottom fade starts here (95% down)
                1f to Color.Transparent
            )

            // 3. Use LazyColumn with the graphicsLayer mask
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .offset(y = offsetY)
                    .alpha(alpha)
                    // --- ADD THIS BLOCK FOR FADE EFFECT ---
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = fadeBrush,
                            blendMode = BlendMode.DstIn
                        )
                    },
                // --------------------------------------
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 50.dp) // Add top padding so text doesn't start immediately in the fade area
            ) {
                // ... (Header Item code remains the same) ...
                item {

                    Text(
                        text = "Frequently asked questions",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // ... (Index/Menu Item code remains the same) ...
                item {
                    Text(
                        text = "Index",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    sections.forEachIndexed { index, section ->
                        Text(
                            text = "${index + 1}. ${section.title}",
                            fontSize = 16.sp,
                            color = textColor,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index + 2)
                                    }
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                    Divider(color = textColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))
                }

                // ... (Content Items code remains the same) ...
                itemsIndexed(sections) { _, section ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = section.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = section.content,
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}
