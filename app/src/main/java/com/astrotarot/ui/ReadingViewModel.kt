package com.astrotarot.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.data.LocalEphemerisCalculator
import com.astrotarot.engine.domain.AspectCalculator
import com.astrotarot.engine.domain.TarotAstrologyEngine
import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.WeightedCard
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class ReadingUiState {
    data object Idle : ReadingUiState()
    data object FetchingLocation : ReadingUiState()
    data object Calculating : ReadingUiState()
    data class Success(
        val reading: List<WeightedCard>,
        val positions: List<PlanetPosition>,
        val aspects: List<Aspect>,
        val ascendantDegree: Double,
        val midheavenDegree: Double,
        val lat: Double,
        val lon: Double,
        val timestamp: Long,
    ) : ReadingUiState()
    data class Error(val message: String) : ReadingUiState()
}

class ReadingViewModel(app: Application) : AndroidViewModel(app) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(app)
    private val engine = TarotAstrologyEngine(FULL_DECK)

    private val _state = MutableStateFlow<ReadingUiState>(ReadingUiState.Idle)
    val state: StateFlow<ReadingUiState> = _state.asStateFlow()

    fun startReading() {
        if (_state.value is ReadingUiState.FetchingLocation ||
            _state.value is ReadingUiState.Calculating) return

        viewModelScope.launch {
            _state.value = ReadingUiState.FetchingLocation
            try {
                val location = fetchLocation()
                _state.value = ReadingUiState.Calculating
                val result = withContext(Dispatchers.Default) {
                    buildReading(location.latitude, location.longitude)
                }
                _state.value = result
            } catch (e: Exception) {
                _state.value = ReadingUiState.Error(e.message ?: "Location unavailable")
            }
        }
    }

    @SuppressLint("MissingPermission")  // caller ensures permission is granted
    private suspend fun fetchLocation(): android.location.Location =
        suspendCancellableCoroutine { cont ->
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(15_000L)
                .build()
            fusedClient.getCurrentLocation(request, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) cont.resume(loc)
                    else cont.resumeWithException(Exception("Could not determine location"))
                }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private fun buildReading(lat: Double, lon: Double): ReadingUiState.Success {
        val now      = System.currentTimeMillis()
        val astro    = LocalEphemerisCalculator.calculate(lat, lon, now)
        val aspects  = AspectCalculator.calculate(astro.positions)
        val reading  = engine.generateWeightedReading(astro.positions, cardsToDraw = 3)
        return ReadingUiState.Success(
            reading          = reading,
            positions        = astro.positions,
            aspects          = aspects,
            ascendantDegree  = astro.ascendantDegree,
            midheavenDegree  = astro.midheavenDegree,
            lat              = lat,
            lon              = lon,
            timestamp        = now,
        )
    }

    fun reset() { _state.value = ReadingUiState.Idle }
}
