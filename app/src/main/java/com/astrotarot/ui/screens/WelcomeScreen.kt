package com.astrotarot.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.astrotarot.ui.ReadingUiState

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
fun WelcomeScreen(
    state: ReadingUiState,
    onReadingRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onReadingRequested()
    }

    fun onButtonTapped() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) onReadingRequested()
        else permissionLauncher.launch(LOCATION_PERMISSIONS)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = ::onButtonTapped,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.size(width = 200.dp, height = 52.dp),
            ) {
                Text(
                    text = "DRAW A READING",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }

            if (state is ReadingUiState.Error) {
                Spacer(Modifier.height(24.dp))
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
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
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
