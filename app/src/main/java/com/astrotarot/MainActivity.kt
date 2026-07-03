package com.astrotarot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.ReadingViewModel
import com.astrotarot.ui.screens.InfoScreen
import com.astrotarot.ui.screens.LoadingScreen
import com.astrotarot.ui.screens.ReadingScreen
import com.astrotarot.ui.screens.WelcomeScreen
import com.astrotarot.ui.theme.AstroTarotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AstroTarotTheme {
                val vm: ReadingViewModel = viewModel()
                val state by vm.state.collectAsState()
                var showInfo by remember { mutableStateOf(false) }

                if (showInfo) {
                    InfoScreen(
                        onBack = { showInfo = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    when (val s = state) {
                        is ReadingUiState.Idle,
                        is ReadingUiState.Error ->
                            WelcomeScreen(
                                state = s,
                                onReadingRequested = { ts -> vm.startReading(ts) },
                                onManualCoordinates = { lat, lon, ts -> vm.startReadingAt(lat, lon, ts) },
                                onShowInfo = { showInfo = true },
                                modifier = Modifier.fillMaxSize(),
                            )

                        is ReadingUiState.FetchingLocation,
                        is ReadingUiState.Calculating ->
                            LoadingScreen(
                                state = s,
                                modifier = Modifier.fillMaxSize(),
                            )

                        is ReadingUiState.Success ->
                            ReadingScreen(
                                state = s,
                                onNewReading = { vm.reset() },
                                modifier = Modifier.fillMaxSize(),
                            )
                    }
                }
            }
        }
    }
}
