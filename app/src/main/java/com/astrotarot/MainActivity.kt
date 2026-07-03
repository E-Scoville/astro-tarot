package com.astrotarot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astrotarot.ui.LocalSoundManager
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.ReadingViewModel
import com.astrotarot.ui.SoundManager
import com.astrotarot.ui.screens.InfoScreen
import com.astrotarot.ui.screens.LoadingScreen
import com.astrotarot.ui.screens.ReadingScreen
import com.astrotarot.ui.screens.WelcomeScreen
import com.astrotarot.ui.theme.AstroTarotTheme

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(applicationContext)
        enableEdgeToEdge()
        setContent {
            AstroTarotTheme {
                CompositionLocalProvider(LocalSoundManager provides soundManager) {
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
                                onReadingRequested = { ts, spread -> vm.startReading(ts, spread) },
                                onManualCoordinates = { lat, lon, ts, spread -> vm.startReadingAt(lat, lon, ts, spread) },
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
                } // CompositionLocalProvider
            }
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseDrone()
    }

    override fun onResume() {
        super.onResume()
        soundManager.resumeDrone()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
