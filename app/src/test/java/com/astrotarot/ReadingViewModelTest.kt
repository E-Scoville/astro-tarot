package com.astrotarot

import com.astrotarot.data.LocationProvider
import com.astrotarot.data.ReadingBuilder
import com.astrotarot.data.ReadingHistoryStore
import com.astrotarot.data.ReadingRecord
import com.astrotarot.data.SavedCard
import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.domain.model.Spread
import com.astrotarot.engine.domain.model.Spreads
import com.astrotarot.engine.domain.model.WeightedCard
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.ReadingViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    // ── Fakes ─────────────────────────────────────────────────────────────────

    private class FakeLocationProvider(
        var result: Pair<Double, Double>? = 40.0 to -111.0,
        var error: Exception? = null,
    ) : LocationProvider {
        var callCount = 0
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun currentCoordinates(): Pair<Double, Double> {
            callCount++
            gate?.await()
            error?.let { throw it }
            return result!!
        }
    }

    private class FakeHistoryStore : ReadingHistoryStore {
        val records = mutableListOf<ReadingRecord>()
        var failOnSave = false
        override fun load(): List<ReadingRecord> = records.toList()
        override fun save(record: ReadingRecord) {
            if (failOnSave) throw RuntimeException("disk full")
            records.removeAll { it.savedAt == record.savedAt }
            records.add(0, record)
        }
        override fun delete(savedAt: Long) {
            records.removeAll { it.savedAt == savedAt }
        }
    }

    private class FakeReadingBuilder : ReadingBuilder {
        var buildError: Exception? = null
        var lastBuildArgs: List<Any>? = null

        private fun success(lat: Double, lon: Double, ts: Long, spread: Spread) =
            ReadingUiState.Success(
                reading = listOf(WeightedCard(FULL_DECK.first(), 1.0, reversed = false)),
                positions = emptyList(), aspects = emptyList(),
                ascendantDegree = 0.0, midheavenDegree = 0.0,
                lat = lat, lon = lon, timestamp = ts, spread = spread,
            )

        override fun build(lat: Double, lon: Double, timestamp: Long, spread: Spread): ReadingUiState.Success {
            buildError?.let { throw it }
            lastBuildArgs = listOf(lat, lon, timestamp, spread)
            return success(lat, lon, timestamp, spread)
        }

        override fun restore(record: ReadingRecord): ReadingUiState.Success =
            success(record.lat, record.lon, record.timestamp, Spreads.byId(record.spreadId))
                .copy(savedAt = record.savedAt)
    }

    private fun viewModel(
        location: FakeLocationProvider = FakeLocationProvider(),
        history: FakeHistoryStore = FakeHistoryStore(),
        builder: FakeReadingBuilder = FakeReadingBuilder(),
    ) = ReadingViewModel(
        locationProvider  = location,
        historyStore      = history,
        readingBuilder    = builder,
        computeDispatcher = dispatcher,
        ioDispatcher      = dispatcher,
    )

    private fun sampleRecord() = ReadingRecord(
        savedAt = 1L, timestamp = 1720000000000L, lat = 40.0, lon = -111.0,
        spreadId = "angles",
        cards = listOf(SavedCard(FULL_DECK.first().name, 1.0, false, null, null)),
    )

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `startReading reaches Success without keeping the reading`() = runTest(dispatcher) {
        val history = FakeHistoryStore()
        val vm = viewModel(history = history)

        vm.startReading(timestamp = 123L, spread = Spreads.SINGLE)
        testScheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue("expected Success, got $state", state is ReadingUiState.Success)
        assertEquals(123L, (state as ReadingUiState.Success).timestamp)
        assertFalse("a new reading is not kept until asked for", state.isSaved)
        assertEquals(0, history.records.size)
        assertEquals(0, vm.history.value.size)
    }

    @Test
    fun `saveCurrentReading keeps the reading and marks it saved`() = runTest(dispatcher) {
        val history = FakeHistoryStore()
        val vm = viewModel(history = history)
        vm.startReading(timestamp = 123L, spread = Spreads.SINGLE)
        testScheduler.advanceUntilIdle()

        vm.saveCurrentReading(savedAt = 500L)
        testScheduler.advanceUntilIdle()

        assertEquals(1, history.records.size)
        assertEquals("single", history.records.first().spreadId)
        assertEquals(500L, history.records.first().savedAt)
        assertEquals(1, vm.history.value.size)
        assertTrue((vm.state.value as ReadingUiState.Success).isSaved)
    }

    @Test
    fun `saving twice does not duplicate the reading`() = runTest(dispatcher) {
        val history = FakeHistoryStore()
        val vm = viewModel(history = history)
        vm.startReading()
        testScheduler.advanceUntilIdle()

        vm.saveCurrentReading(savedAt = 500L)
        testScheduler.advanceUntilIdle()
        vm.saveCurrentReading(savedAt = 900L)
        testScheduler.advanceUntilIdle()

        assertEquals(1, history.records.size)
        assertEquals(500L, history.records.first().savedAt)
    }

    @Test
    fun `a reading that fails to save is not reported as kept`() = runTest(dispatcher) {
        val history = FakeHistoryStore().apply { failOnSave = true }
        val vm = viewModel(history = history)
        vm.startReading()
        testScheduler.advanceUntilIdle()

        vm.saveCurrentReading(savedAt = 500L)
        testScheduler.advanceUntilIdle()

        assertEquals(0, history.records.size)
        assertFalse(
            "a failed write must not claim the reading is kept",
            (vm.state.value as ReadingUiState.Success).isSaved,
        )
    }

    @Test
    fun `saveCurrentReading does nothing when there is no reading on screen`() = runTest(dispatcher) {
        val history = FakeHistoryStore()
        val vm = viewModel(history = history)

        vm.saveCurrentReading(savedAt = 500L)
        testScheduler.advanceUntilIdle()

        assertEquals(0, history.records.size)
        assertTrue(vm.state.value is ReadingUiState.Idle)
    }

    @Test
    fun `deleteReading removes it and refreshes the collection`() = runTest(dispatcher) {
        val history = FakeHistoryStore().apply {
            records.add(sampleRecord())
            records.add(sampleRecord().copy(savedAt = 2L))
        }
        val vm = viewModel(history = history)
        testScheduler.advanceUntilIdle()

        vm.deleteReading(sampleRecord())
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(2L), history.records.map { it.savedAt })
        assertEquals(listOf(2L), vm.history.value.map { it.savedAt })
    }

    @Test
    fun `deleting the reading on screen marks it no longer kept`() = runTest(dispatcher) {
        val history = FakeHistoryStore().apply { records.add(sampleRecord()) }
        val vm = viewModel(history = history)
        testScheduler.advanceUntilIdle()

        vm.restoreReading(sampleRecord())
        testScheduler.advanceUntilIdle()
        assertTrue((vm.state.value as ReadingUiState.Success).isSaved)

        vm.deleteReading(sampleRecord())
        testScheduler.advanceUntilIdle()

        val state = vm.state.value as ReadingUiState.Success
        assertFalse("the open reading is no longer in the collection", state.isSaved)
        // It stays on screen; only its kept status changed.
        assertEquals(1720000000000L, state.timestamp)
    }

    @Test
    fun `location failure produces Error state with message`() = runTest(dispatcher) {
        val vm = viewModel(location = FakeLocationProvider(error = Exception("GPS off")))

        vm.startReading()
        testScheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is ReadingUiState.Error)
        assertEquals("GPS off", (state as ReadingUiState.Error).message)
    }

    @Test
    fun `startReadingAt skips the location provider entirely`() = runTest(dispatcher) {
        val location = FakeLocationProvider()
        val builder = FakeReadingBuilder()
        val vm = viewModel(location = location, builder = builder)

        vm.startReadingAt(51.5, -0.1, timestamp = 9L, spread = Spreads.HOUSES)
        testScheduler.advanceUntilIdle()

        assertEquals(0, location.callCount)
        assertTrue(vm.state.value is ReadingUiState.Success)
        assertEquals(listOf(51.5, -0.1, 9L, Spreads.HOUSES), builder.lastBuildArgs)
    }

    @Test
    fun `builder failure in startReadingAt produces Error instead of crashing`() = runTest(dispatcher) {
        val builder = FakeReadingBuilder().apply { buildError = IllegalStateException("ephemeris out of range") }
        val vm = viewModel(builder = builder)

        vm.startReadingAt(0.0, 0.0)
        testScheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is ReadingUiState.Error)
        assertEquals("ephemeris out of range", (state as ReadingUiState.Error).message)
    }

    @Test
    fun `second startReading while busy is ignored`() = runTest(dispatcher) {
        val location = FakeLocationProvider().apply { gate = CompletableDeferred() }
        val vm = viewModel(location = location)

        vm.startReading()
        testScheduler.runCurrent()
        assertTrue(vm.state.value is ReadingUiState.FetchingLocation)

        vm.startReading()   // should be a no-op while the first is in flight
        location.gate!!.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, location.callCount)
        assertTrue(vm.state.value is ReadingUiState.Success)
    }

    @Test
    fun `reset returns to Idle`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startReading()
        testScheduler.advanceUntilIdle()
        assertTrue(vm.state.value is ReadingUiState.Success)

        vm.reset()
        assertTrue(vm.state.value is ReadingUiState.Idle)
    }

    @Test
    fun `history is loaded on init`() = runTest(dispatcher) {
        val history = FakeHistoryStore().apply { records.add(sampleRecord()) }
        val vm = viewModel(history = history)
        testScheduler.advanceUntilIdle()

        assertEquals(1, vm.history.value.size)
    }

    @Test
    fun `restoreReading rebuilds Success from a record without re-keeping it`() = runTest(dispatcher) {
        val history = FakeHistoryStore().apply { records.add(sampleRecord()) }
        val vm = viewModel(history = history)
        testScheduler.advanceUntilIdle()

        vm.restoreReading(sampleRecord())
        testScheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is ReadingUiState.Success)
        assertEquals(1720000000000L, (state as ReadingUiState.Success).timestamp)
        assertEquals(Spreads.ANGLES, state.spread)
        assertEquals(1, history.records.size)   // restore did not append
    }
}
