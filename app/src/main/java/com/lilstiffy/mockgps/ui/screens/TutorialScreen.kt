package com.lilstiffy.mockgps.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TutorialScreen(onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("How to Enable Mock Locations", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("1. Go to your phone's Settings > About phone.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("2. Tap on 'Build number' 7 times to enable Developer Options.")
        Spacer(modifier = Modifier.height(16.dp))
        Text("3. Go back to the main Settings menu and find 'Developer options'.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("4. In Developer options, find and tap 'Select mock location app'.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("5. Choose this app from the list.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenSettings) {
            Text("Open Settings")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onDismiss) {
            Text("Done")
        }
    }
}
