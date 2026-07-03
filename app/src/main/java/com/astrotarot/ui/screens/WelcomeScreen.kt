package com.astrotarot.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.theme.DimWhite
import com.astrotarot.ui.theme.Gold

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
fun WelcomeScreen(
    state: ReadingUiState,
    onReadingRequested: () -> Unit,
    onManualCoordinates: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showManual by remember { mutableStateOf(false) }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onReadingRequested()
        else showManual = true   // permission denied — offer manual fallback
    }

    fun onGpsButtonTapped() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) onReadingRequested()
        else permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    fun onManualSubmit() {
        val lat = latText.toDoubleOrNull()
        val lon = lonText.toDoubleOrNull()
        when {
            lat == null || lon == null -> inputError = "Enter valid decimal numbers"
            lat < -90 || lat > 90     -> inputError = "Latitude must be −90 to 90"
            lon < -180 || lon > 180   -> inputError = "Longitude must be −180 to 180"
            else -> { inputError = ""; onManualCoordinates(lat, lon) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            PulsingOrb()

            Spacer(Modifier.height(32.dp))

            Text(
                text = "ASTRO TAROT",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 6.sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Celestial guidance through\nplanetary alignment",
                style = MaterialTheme.typography.bodyMedium,
                color = DimWhite,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = ::onGpsButtonTapped,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("DRAW A READING", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = { showManual = !showManual }) {
                Text(
                    text = if (showManual) "▲ Hide manual coordinates"
                           else "▼ Enter coordinates manually",
                    color = DimWhite,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // ── Manual coordinate entry ──────────────────────────────
            AnimatedVisibility(
                visible = showManual,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = DimWhite,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = DimWhite,
                        cursorColor = Gold,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = latText,
                            onValueChange = { latText = it; inputError = "" },
                            label = { Text("Latitude") },
                            placeholder = { Text("40.04", color = DimWhite) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = lonText,
                            onValueChange = { lonText = it; inputError = "" },
                            label = { Text("Longitude") },
                            placeholder = { Text("-111.73", color = DimWhite) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (inputError.isNotEmpty()) {
                        Text(
                            text = inputError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = ::onManualSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Text("USE THESE COORDINATES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // ── Error message ────────────────────────────────────────
            if (state is ReadingUiState.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "⚠  ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PulsingOrb() {
    val transition = rememberInfiniteTransition(label = "orb")
    val scale by transition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "alpha",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
    ) {
        Text("☽ ✦ ☾", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
    }
}
