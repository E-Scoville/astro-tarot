package com.astrotarot.data

import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.data.LocalEphemerisCalculator
import com.astrotarot.engine.domain.AspectCalculator
import com.astrotarot.engine.domain.TarotAstrologyEngine
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.Spread
import com.astrotarot.engine.domain.model.Spreads
import com.astrotarot.engine.domain.model.WeightedCard
import com.astrotarot.ui.ReadingUiState

/** Abstracts reading construction so the ViewModel can be unit-tested with a fake. */
interface ReadingBuilder {
    fun build(lat: Double, lon: Double, timestamp: Long, spread: Spread): ReadingUiState.Success

    /** Rebuilds a past reading: sky recomputed from the inputs, cards taken from the record. */
    fun restore(record: ReadingRecord): ReadingUiState.Success
}

object EngineReadingBuilder : ReadingBuilder {

    private val engine = TarotAstrologyEngine(FULL_DECK)
    private val deckByName = FULL_DECK.associateBy { it.name }

    override fun build(lat: Double, lon: Double, timestamp: Long, spread: Spread): ReadingUiState.Success {
        val astro   = LocalEphemerisCalculator.calculate(lat, lon, timestamp)
        val aspects = AspectCalculator.calculate(astro.positions)
        val reading = engine.generateSpreadReading(astro.positions, spread, aspects = aspects)
        return ReadingUiState.Success(
            reading         = reading,
            positions       = astro.positions,
            aspects         = aspects,
            ascendantDegree = astro.ascendantDegree,
            midheavenDegree = astro.midheavenDegree,
            lat             = lat,
            lon             = lon,
            timestamp       = timestamp,
            spread          = spread,
        )
    }

    override fun restore(record: ReadingRecord): ReadingUiState.Success {
        val astro   = LocalEphemerisCalculator.calculate(record.lat, record.lon, record.timestamp)
        val aspects = AspectCalculator.calculate(astro.positions)
        val reading = record.cards.mapNotNull { saved ->
            deckByName[saved.name]?.let { card ->
                WeightedCard(
                    card             = card,
                    weight           = saved.weight,
                    reversed         = saved.reversed,
                    primaryInfluence = saved.primaryInfluence
                        ?.let { runCatching { CelestialBody.valueOf(it) }.getOrNull() },
                    reversalMarker   = saved.reversalMarker,
                )
            }
        }
        if (reading.isEmpty()) throw IllegalStateException("Saved reading could not be restored.")
        return ReadingUiState.Success(
            reading         = reading,
            positions       = astro.positions,
            aspects         = aspects,
            ascendantDegree = astro.ascendantDegree,
            midheavenDegree = astro.midheavenDegree,
            lat             = record.lat,
            lon             = record.lon,
            timestamp       = record.timestamp,
            spread          = Spreads.byId(record.spreadId),
        )
    }
}

fun ReadingUiState.Success.toRecord(savedAt: Long = System.currentTimeMillis()): ReadingRecord =
    ReadingRecord(
        savedAt   = savedAt,
        timestamp = timestamp,
        lat       = lat,
        lon       = lon,
        spreadId  = spread.id,
        cards     = reading.map {
            SavedCard(
                name             = it.card.name,
                weight           = it.weight,
                reversed         = it.reversed,
                primaryInfluence = it.primaryInfluence?.name,
                reversalMarker   = it.reversalMarker,
            )
        },
    )
