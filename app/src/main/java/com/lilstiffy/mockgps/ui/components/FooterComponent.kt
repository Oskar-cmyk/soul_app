package com.gps.soul.ui.components

import android.location.Address
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.gps.soul.extensions.displayString
import com.gps.soul.extensions.prettyPrint
import com.gps.soul.ui.theme.ButtonGreen
import com.gps.soul.ui.theme.ButtonRed

@Composable
fun FooterComponent(
    modifier: Modifier = Modifier,
    address: Address?,
    latLng: LatLng,
    isMocking: Boolean,
    isFavorite: Boolean = false,
    onStart: () -> Unit,
    onFavorite: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Location section
            Row {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth(0.85f),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    text = address?.displayString() ?: "\n"
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Coordinates section
            Row {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.onSurface,
                    text = latLng.prettyPrint()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Toggle mocking button
                IconToggleButton(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    checked = isMocking,
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = ButtonRed,
                        containerColor = ButtonGreen,
                        contentColor = Color.White
                    ),
                    onCheckedChange = {
                        onStart()
                    },
                ) {
                    Row {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            imageVector = if (isMocking) Icons.Filled.Close else Icons.Filled.PlayArrow,
                            tint = Color.White,
                            contentDescription = "toggle mocking"
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            text = if (isMocking) "Stop mocking" else "Start mocking"
                        )
                    }
                }
            }

        }
    }
}
