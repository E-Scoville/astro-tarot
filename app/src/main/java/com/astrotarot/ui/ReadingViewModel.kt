package com.astrotarot.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.astrotarot.data.EngineReadingBuilder
import com.astrotarot.data.FileReadingHistoryStore
import com.astrotarot.data.FusedLocationProvider
import com.astrotarot.data.LocationProvider
import com.astrotarot.data.ReadingBuilder
import com.astrotarot.data.ReadingHistoryStore
import com.astrotarot.data.ReadingRecord
import com.astrotarot.data.toRecord
import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.Spread
import com.astrotarot.engine.domain.model.Spreads
import com.astrotarot.engine.domain.model.WeightedCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
        val spread: Spread,
    ) : ReadingUiState()
    data class Error(val message: String) : ReadingUiState()
}

class ReadingViewModel(
    private val locationProvider: LocationProvider,
    private val historyStore: ReadingHistoryStore,
    private val readingBuilder: ReadingBuilder = EngineReadingBuilder,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _state = MutableStateFlow<ReadingUiState>(ReadingUiState.Idle)
    val state: StateFlow<ReadingUiState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<ReadingRecord>>(emptyList())
    val history: StateFlow<List<ReadingRecord>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            _history.value = withContext(ioDispatcher) {
                runCatching { historyStore.load() }.getOrDefault(emptyList())
            }
        }
    }

    fun startReading(timestamp: Long = System.currentTimeMillis(), spread: Spread = Spreads.ANGLES) {
        if (isBusy()) return
        viewModelScope.launch {
            _state.value = ReadingUiState.FetchingLocation
            try {
                val (lat, lon) = locationProvider.currentCoordinates()
                _state.value = ReadingUiState.Calculating
                finishReading(lat, lon, timestamp, spread)
            } catch (e: CancellationException) {
                throw e   // never swallow cancellation — structured concurrency depends on it
            } catch (e: Exception) {
                _state.value = ReadingUiState.Error(e.message ?: "Location unavailable")
            }
        }
    }

    /** Skip GPS entirely and use manually entered coordinates. */
    fun startReadingAt(
        lat: Double, lon: Double,
        timestamp: Long = System.currentTimeMillis(),
        spread: Spread = Spreads.ANGLES,
    ) {
        if (isBusy()) return
        viewModelScope.launch {
            _state.value = ReadingUiState.Calculating
            try {
                finishReading(lat, lon, timestamp, spread)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = ReadingUiState.Error(e.message ?: "Could not calculate reading")
            }
        }
    }

    /** Rebuild a past reading from history. Restored readings are not re-saved. */
    fun restoreReading(record: ReadingRecord) {
        if (isBusy()) return
        viewModelScope.launch {
            _state.value = ReadingUiState.Calculating
            try {
                _state.value = withContext(computeDispatcher) { readingBuilder.restore(record) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = ReadingUiState.Error(e.message ?: "Could not restore reading")
            }
        }
    }

    fun reset() { _state.value = ReadingUiState.Idle }

    private fun isBusy(): Boolean =
        _state.value is ReadingUiState.FetchingLocation ||
        _state.value is ReadingUiState.Calculating

    private suspend fun finishReading(lat: Double, lon: Double, timestamp: Long, spread: Spread) {
        val success = withContext(computeDispatcher) {
            readingBuilder.build(lat, lon, timestamp, spread)
        }
        // Persist before showing; a failed save must not block the reading.
        runCatching {
            withContext(ioDispatcher) { historyStore.save(success.toRecord()) }
            _history.value = withContext(ioDispatcher) { historyStore.load() }
        }
        _state.value = success
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReadingViewModel(
                    locationProvider = FusedLocationProvider(context),
                    historyStore     = FileReadingHistoryStore(File(context.filesDir, "reading_history.json")),
                )
            }
        }
    }
}
