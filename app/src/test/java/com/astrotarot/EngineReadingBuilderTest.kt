package com.astrotarot

import com.astrotarot.data.EngineReadingBuilder
import com.astrotarot.data.ReadingRecord
import com.astrotarot.data.SavedCard
import com.astrotarot.data.SavedSky
import com.astrotarot.data.SavedSpread
import com.astrotarot.data.toRecord
import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.Spreads
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineReadingBuilderTest {

    private val storedSky = SavedSky(
        // Deliberately NOT the real sky for this lat/lon/time, so a recompute is
        // detectable: if these values come back, the stored sky was honoured.
        positions = listOf(
            PlanetPosition(CelestialBody.SUN, "ARIES", 5.5, 1, false),
            PlanetPosition(CelestialBody.MERCURY, "GEMINI", 65.25, 3, true),
        ),
        aspects = listOf(Aspect(CelestialBody.SUN, CelestialBody.MERCURY, AspectType.SEXTILE, 1.75)),
        ascendantDegree = 123.5,
        midheavenDegree = 33.25,
    )

    private fun record(
        sky: SavedSky? = storedSky,
        spread: SavedSpread? = SavedSpread("The Three Angles", listOf("One", "Two")),
        spreadId: String = "angles",
        cards: List<SavedCard> = listOf(
            SavedCard("The Magician", 3.25, reversed = false, primaryInfluence = "MERCURY", reversalMarker = null),
            SavedCard("Two of Wands", 1.5, reversed = true, primaryInfluence = "MARS", reversalMarker = "℞"),
        ),
    ) = ReadingRecord(
        savedAt = 1L, timestamp = 1720000000000L,
        lat = 40.2338, lon = -111.6585,
        spreadId = spreadId, cards = cards, sky = sky, spread = spread,
    )

    @Test
    fun `restore returns the stored sky rather than recomputing it`() {
        val restored = EngineReadingBuilder.restore(record())

        assertEquals(storedSky.positions, restored.positions)
        assertEquals(storedSky.aspects, restored.aspects)
        assertEquals(storedSky.ascendantDegree, restored.ascendantDegree, 0.0)
        assertEquals(storedSky.midheavenDegree, restored.midheavenDegree, 0.0)
    }

    @Test
    fun `restore returns the stored cards and orientations verbatim`() {
        val restored = EngineReadingBuilder.restore(record())

        assertEquals(listOf("The Magician", "Two of Wands"), restored.reading.map { it.card.name })
        assertEquals(listOf(false, true), restored.reading.map { it.reversed })
        assertEquals(listOf(3.25, 1.5), restored.reading.map { it.weight })
        assertEquals(CelestialBody.MARS, restored.reading[1].primaryInfluence)
        assertEquals("℞", restored.reading[1].reversalMarker)
    }

    @Test
    fun `restore uses the labels the reading was shown under, not the live spread`() {
        val restored = EngineReadingBuilder.restore(
            record(spread = SavedSpread("Old Spread Name", listOf("Then-A", "Then-B"))),
        )

        assertEquals("Old Spread Name", restored.spread.name)
        assertEquals(listOf("Then-A", "Then-B"), restored.spread.positions.map { it.label })
        assertNotEquals(Spreads.ANGLES.name, restored.spread.name)
    }

    @Test
    fun `a legacy record without a stored sky still restores`() {
        val restored = EngineReadingBuilder.restore(record(sky = null, spread = null))

        assertEquals(2, restored.reading.size)
        // Recomputed, so it must be a real sky rather than the fixture's.
        assertTrue(restored.positions.isNotEmpty())
        assertNotEquals(storedSky.positions, restored.positions)
    }

    @Test
    fun `a legacy record whose spread no longer fits still labels every card`() {
        // Three saved cards under an id that resolves to a 1-position spread.
        val restored = EngineReadingBuilder.restore(
            record(
                sky = null, spread = null, spreadId = "single",
                cards = listOf(
                    SavedCard("The Fool", 1.0, reversed = false, primaryInfluence = null, reversalMarker = null),
                    SavedCard("The Magician", 2.0, reversed = false, primaryInfluence = null, reversalMarker = null),
                    SavedCard("The Empress", 3.0, reversed = true, primaryInfluence = null, reversalMarker = "□"),
                ),
            ),
        )

        assertEquals(3, restored.reading.size)
        assertTrue(
            "every drawn card needs a position slot",
            restored.spread.positions.size >= restored.reading.size,
        )
    }

    @Test
    fun `a reading saved and reopened comes back identical`() {
        val built = EngineReadingBuilder.build(40.2338, -111.6585, 1720000000000L, Spreads.ANGLES)
        val reopened = EngineReadingBuilder.restore(built.toRecord())

        assertEquals(built.reading, reopened.reading)
        assertEquals(built.positions, reopened.positions)
        assertEquals(built.aspects, reopened.aspects)
        assertEquals(built.ascendantDegree, reopened.ascendantDegree, 0.0)
        assertEquals(built.midheavenDegree, reopened.midheavenDegree, 0.0)
        assertEquals(built.spread.name, reopened.spread.name)
        assertEquals(
            built.spread.positions.map { it.label },
            reopened.spread.positions.map { it.label },
        )
    }
}
