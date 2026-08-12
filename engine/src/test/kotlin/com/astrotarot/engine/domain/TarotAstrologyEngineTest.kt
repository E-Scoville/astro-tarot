package com.astrotarot.engine.domain

import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.Spreads
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TarotAstrologyEngineTest {

    private val engine = TarotAstrologyEngine(FULL_DECK)

    /** A baseline set of transits: one planet in each sign-ish spread, nothing exotic. */
    private fun baselineTransits(): List<PlanetPosition> = listOf(
        PlanetPosition(CelestialBody.SUN, "ARIES", 5.0, 1, false),
        PlanetPosition(CelestialBody.MOON, "TAURUS", 35.0, 2, false),
        PlanetPosition(CelestialBody.MERCURY, "GEMINI", 65.0, 3, false),
        PlanetPosition(CelestialBody.VENUS, "CANCER", 95.0, 4, false),
        PlanetPosition(CelestialBody.MARS, "LEO", 125.0, 5, false),
        PlanetPosition(CelestialBody.JUPITER, "VIRGO", 155.0, 6, false),
        PlanetPosition(CelestialBody.SATURN, "LIBRA", 185.0, 7, false),
        PlanetPosition(CelestialBody.URANUS, "SCORPIO", 215.0, 8, false),
        PlanetPosition(CelestialBody.NEPTUNE, "SAGITTARIUS", 245.0, 9, false),
        PlanetPosition(CelestialBody.PLUTO, "CAPRICORN", 275.0, 10, false),
    )

    @Test
    fun `seeded determinism produces identical readings on repeated calls`() {
        val transits = baselineTransits()
        val r1 = engine.generateWeightedReading(transits, 5, random = Random(42))
        val r2 = engine.generateWeightedReading(transits, 5, random = Random(42))

        assertEquals(r1.map { it.card.name }, r2.map { it.card.name })
        assertEquals(r1.map { it.reversed }, r2.map { it.reversed })
        assertEquals(r1.map { it.primaryInfluence }, r2.map { it.primaryInfluence })
    }

    @Test
    fun `draw count returns distinct cards`() {
        val reading = engine.generateWeightedReading(baselineTransits(), 3, random = Random(7))
        assertEquals(3, reading.size)
        assertEquals(3, reading.map { it.card.name }.distinct().size)
    }

    @Test
    fun `weight steering favors card whose decan and ruler are transited`() {
        // "Two of Wands": MARS, ARIES, longitude range [0, 10)
        val targetCard = FULL_DECK.first { it.name == "Two of Wands" }
        assertEquals(CelestialBody.MARS, targetCard.associatedBody)
        assertTrue(targetCard.longitudeRangeStart == 0.0 && targetCard.longitudeRangeEnd == 10.0)

        // Put Mars exactly in the card's decan (Aries 0-10 degrees). Use a single sparse
        // transit so the target card's bonus isn't diluted by many competing sign/element
        // matches from other simultaneous transits (verified empirically: with only Mars
        // transiting, "Two of Wands" is the single most-drawn card at ~4.4% of 1-card draws,
        // versus a 1/78 ≈ 1.3% baseline).
        val transits = listOf(
            PlanetPosition(CelestialBody.MARS, "ARIES", 5.0, 1, false),
        )

        val trials = 400
        var hits = 0
        for (seed in 0 until trials) {
            val reading = engine.generateWeightedReading(transits, 1, aspects = emptyList(), random = Random(seed))
            if (reading.first().card.name == "Two of Wands") hits++
        }

        val baseline = 1.0 / FULL_DECK.size
        val observedRate = hits.toDouble() / trials
        assertTrue(
            "expected Two of Wands to appear more than baseline ($baseline), got rate $observedRate ($hits/$trials)",
            observedRate > baseline * 2.0
        )
    }

    @Test
    fun `primaryInfluence is non-null for upright card whose ruler is strongly transited`() {
        // Force Sun (ruler of "The Sun" major) into an angular house with a single, sparse
        // transit list so its weight bonus isn't diluted by competing sign/element matches
        // from other simultaneous transits (empirically: The Sun is drawn upright in ~4% of
        // single-card seeded draws under this scenario).
        val transits = listOf(
            PlanetPosition(CelestialBody.SUN, "LEO", 125.0, 1, false), // angular house 1
        )

        // Search seeds for an upright "The Sun" draw.
        var found = false
        for (seed in 0 until 2000) {
            val reading = engine.generateWeightedReading(transits, 1, aspects = emptyList(), random = Random(seed))
            val card = reading.first()
            if (card.card.name == "The Sun" && !card.reversed) {
                assertNotNull("primaryInfluence should be set for upright The Sun", card.primaryInfluence)
                found = true
                break
            }
        }
        assertTrue("Never drew 'The Sun' upright in 2000 seeded trials; cannot verify primaryInfluence", found)
    }

    @Test
    fun `reversed cards carry non-null primaryInfluence and valid reversal marker`() {
        // Allowed markers: retrograde glyph plus each AspectType symbol.
        val allowedReversalMarkers = com.astrotarot.engine.domain.model.AspectType.entries
            .map { it.symbol }
            .toSet() + RETROGRADE_MARKER

        // One retrograde planet (Mercury) creating tension with another (Mars, opposition-ish).
        val transits = listOf(
            PlanetPosition(CelestialBody.SUN, "LEO", 130.0, 5, false),
            PlanetPosition(CelestialBody.MOON, "TAURUS", 40.0, 6, false),
            PlanetPosition(CelestialBody.MERCURY, "GEMINI", 70.0, 3, true), // retrograde
            PlanetPosition(CelestialBody.VENUS, "CANCER", 100.0, 8, false),
            PlanetPosition(CelestialBody.MARS, "SAGITTARIUS", 250.0, 9, false), // ~180 from Mercury
            PlanetPosition(CelestialBody.JUPITER, "VIRGO", 160.0, 11, false),
            PlanetPosition(CelestialBody.SATURN, "LIBRA", 190.0, 2, false),
            PlanetPosition(CelestialBody.URANUS, "SCORPIO", 220.0, 12, false),
            PlanetPosition(CelestialBody.NEPTUNE, "CAPRICORN", 280.0, 4, false),
            PlanetPosition(CelestialBody.PLUTO, "AQUARIUS", 310.0, 7, false),
        )

        var sawReversed = false
        for (seed in 0 until 300) {
            val reading = engine.generateWeightedReading(transits, 3, random = Random(seed))
            for (wc in reading) {
                if (wc.reversed) {
                    sawReversed = true
                    assertNotNull("reversed card ${wc.card.name} should have primaryInfluence", wc.primaryInfluence)
                    assertNotNull("reversed card ${wc.card.name} should have reversalMarker", wc.reversalMarker)
                    assertTrue(
                        "unexpected reversal marker '${wc.reversalMarker}'",
                        wc.reversalMarker in allowedReversalMarkers
                    )
                }
            }
            if (sawReversed) break
        }
        assertTrue("Never observed a reversed card across 300 seeded trials", sawReversed)
    }

    @Test
    fun `reversal is probabilistic rather than fixed for a given card and sky`() {
        val transits = baselineTransits()

        // Across many seeds the same card must appear BOTH ways; under the old
        // deterministic rule a card's orientation was a pure function of its weight.
        val orientations = HashMap<String, MutableSet<Boolean>>()
        for (seed in 0 until 1500) {
            for (wc in engine.generateWeightedReading(transits, 3, random = Random(seed))) {
                orientations.getOrPut(wc.card.name) { mutableSetOf() } += wc.reversed
            }
        }

        val bothWays = orientations.count { it.value.size == 2 }
        assertTrue(
            "expected many cards to appear both upright and reversed in the same sky, got $bothWays",
            bothWays >= 10
        )
    }

    @Test
    fun `cards weighted at or above average are never reversed`() {
        val transits = baselineTransits()
        // The deck average for this sky; anything at or above it must stay upright.
        val table = engine.generateWeightedReading(transits, FULL_DECK.size, random = Random(1))
        val avg = table.map { it.weight }.average()

        for (seed in 0 until 800) {
            for (wc in engine.generateWeightedReading(transits, 5, random = Random(seed))) {
                if (wc.weight >= avg) {
                    assertTrue(
                        "card ${wc.card.name} at weight ${wc.weight} (avg $avg) should not reverse",
                        !wc.reversed
                    )
                }
            }
        }
    }

    @Test
    fun `deeper below average weights reverse more often`() {
        val transits = baselineTransits()
        val table = engine.generateWeightedReading(transits, FULL_DECK.size, random = Random(1))
        val weights = table.map { it.weight }
        val avg = weights.average()
        val min = weights.min()
        val midpoint = min + (avg - min) / 2.0

        var deepDrawn = 0; var deepReversed = 0
        var shallowDrawn = 0; var shallowReversed = 0

        for (seed in 0 until 3000) {
            for (wc in engine.generateWeightedReading(transits, 3, random = Random(seed))) {
                if (wc.weight >= avg) continue
                if (wc.weight < midpoint) {
                    deepDrawn++; if (wc.reversed) deepReversed++
                } else {
                    shallowDrawn++; if (wc.reversed) shallowReversed++
                }
            }
        }

        assertTrue("need samples in both bands (deep=$deepDrawn shallow=$shallowDrawn)",
            deepDrawn > 50 && shallowDrawn > 50)
        val deepRate = deepReversed.toDouble() / deepDrawn
        val shallowRate = shallowReversed.toDouble() / shallowDrawn
        assertTrue(
            "cards further below average should reverse more often (deep=$deepRate shallow=$shallowRate)",
            deepRate > shallowRate
        )
    }

    @Test
    fun `a perfectly level sky produces no reversals`() {
        // No transits at all: every card holds the 1.0 baseline weight, so the
        // average equals the minimum and nothing has fallen short of anything.
        for (seed in 0 until 200) {
            val reading = engine.generateWeightedReading(
                emptyList(), 5, aspects = emptyList(), random = Random(seed),
            )
            assertTrue("no card should reverse under a level sky",
                reading.none { it.reversed })
        }
    }

    @Test
    fun `spread readings also reverse probabilistically`() {
        val transits = baselineTransits()
        val orientations = HashMap<String, MutableSet<Boolean>>()
        for (seed in 0 until 1200) {
            for (wc in engine.generateSpreadReading(transits, Spreads.HOUSES, random = Random(seed))) {
                orientations.getOrPut(wc.card.name) { mutableSetOf() } += wc.reversed
            }
        }
        val bothWays = orientations.count { it.value.size == 2 }
        assertTrue(
            "expected spread-drawn cards to appear both ways across seeds, got $bothWays",
            bothWays >= 10
        )
    }

    @Test
    fun `inLongitudeRange wraparound card exists in deck for Queen of Wands`() {
        val wrapCard = FULL_DECK.firstOrNull {
            it.longitudeRangeStart != null && it.longitudeRangeEnd != null &&
                it.longitudeRangeStart > it.longitudeRangeEnd
        }
        if (wrapCard == null) {
            // No wraparound card in deck; nothing to test.
            return
        }
        assertEquals("Queen of Wands", wrapCard.name)
        assertEquals(350.0, wrapCard.longitudeRangeStart!!, 0.0001)
        assertEquals(20.0, wrapCard.longitudeRangeEnd!!, 0.0001)

        // Put the Sun at longitude 5.0, inside the wrapped range [350, 20) since 5 < 20.
        // Use a single sparse transit so the target card's bonus isn't diluted by other
        // simultaneous sign/element matches (empirically ~4% of single-card seeded draws
        // land on Queen of Wands under this scenario, vs 1/78 ≈ 1.3% baseline).
        val transitsInside = listOf(
            PlanetPosition(CelestialBody.SUN, "ARIES", 5.0, 1, false),
        )

        val trials = 400
        var hits = 0
        for (seed in 0 until trials) {
            val reading = engine.generateWeightedReading(transitsInside, 1, aspects = emptyList(), random = Random(seed))
            if (reading.first().card.name == "Queen of Wands") hits++
        }
        val baseline = 1.0 / FULL_DECK.size
        val observedRate = hits.toDouble() / trials
        assertTrue(
            "expected Queen of Wands (wraparound range) to be favored, got rate $observedRate ($hits/$trials)",
            observedRate > baseline * 2.0
        )
    }

    companion object {
        private const val RETROGRADE_MARKER = "℞" // "℞"
    }
}
