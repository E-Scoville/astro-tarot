package com.astrotarot.data

import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.data.LocalEphemerisCalculator
import com.astrotarot.engine.domain.AspectCalculator
import com.astrotarot.engine.domain.TarotAstrologyEngine
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.Spread
import com.astrotarot.engine.domain.model.SpreadPosition
import com.astrotarot.engine.domain.model.Spreads
import com.astrotarot.engine.domain.model.WeightedCard
import com.astrotarot.ui.ReadingUiState

/** Abstracts reading construction so the ViewModel can be unit-tested with a fake. */
interface ReadingBuilder {
    fun build(lat: Double, lon: Double, timestamp: Long, spread: Spread): ReadingUiState.Success

    /** Reopens a past reading exactly as it was recorded, deriving nothing anew. */
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

        // The stored sky is authoritative. Recomputation is only for records written
        // before the sky was persisted, and is a best effort: the ephemeris and the
        // weighting that produced those cards may since have changed.
        val sky = record.sky ?: legacySky(record)

        return ReadingUiState.Success(
            reading         = reading,
            positions       = sky.positions,
            aspects         = sky.aspects,
            ascendantDegree = sky.ascendantDegree,
            midheavenDegree = sky.midheavenDegree,
            lat             = record.lat,
            lon             = record.lon,
            timestamp       = record.timestamp,
            spread          = restoredSpread(record, reading.size),
        )
    }

    private fun legacySky(record: ReadingRecord): SavedSky {
        val astro = LocalEphemerisCalculator.calculate(record.lat, record.lon, record.timestamp)
        return SavedSky(
            positions       = astro.positions,
            aspects         = AspectCalculator.calculate(astro.positions),
            ascendantDegree = astro.ascendantDegree,
            midheavenDegree = astro.midheavenDegree,
        )
    }

    /**
     * The spread as the reading was shown under. Stored labels win; a legacy record
     * falls back to the live definition, padded so every card keeps a label even if
     * the spread has since been renamed, resized, or removed altogether.
     */
    private fun restoredSpread(record: ReadingRecord, cardCount: Int): Spread {
        record.spread?.let { saved ->
            return Spread(
                id        = record.spreadId,
                name      = saved.name,
                tagline   = "",
                positions = saved.positionLabels.map { SpreadPosition(it) },
            )
        }
        val live = Spreads.byId(record.spreadId)
        if (live.positions.size >= cardCount) return live
        return live.copy(
            positions = live.positions + List(cardCount - live.positions.size) { SpreadPosition("") },
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
        sky       = SavedSky(positions, aspects, ascendantDegree, midheavenDegree),
        spread    = SavedSpread(spread.name, spread.positions.map { it.label }),
    )
