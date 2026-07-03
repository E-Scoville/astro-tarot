package com.astrotarot.engine.domain

import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.PlanetPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AspectCalculatorTest {

    private fun pos(body: CelestialBody, longitude: Double) =
        PlanetPosition(body, "ARIES", longitude, 1, false)

    @Test
    fun `angularSeparation basic cases`() {
        assertEquals(20.0, AspectCalculator.angularSeparation(10.0, 350.0), 0.0001)
        assertEquals(180.0, AspectCalculator.angularSeparation(0.0, 180.0), 0.0001)
        assertEquals(2.0, AspectCalculator.angularSeparation(359.0, 1.0), 0.0001)
        assertEquals(0.0, AspectCalculator.angularSeparation(90.0, 90.0), 0.0001)
    }

    @Test
    fun `90 degrees apart yields square with zero orb`() {
        val positions = listOf(pos(CelestialBody.SUN, 0.0), pos(CelestialBody.MOON, 90.0))
        val aspects = AspectCalculator.calculate(positions)
        assertEquals(1, aspects.size)
        assertEquals(AspectType.SQUARE, aspects[0].type)
        assertEquals(0.0, aspects[0].orb, 0.0001)
    }

    @Test
    fun `84 degrees apart yields square with orb 6`() {
        val positions = listOf(pos(CelestialBody.SUN, 0.0), pos(CelestialBody.MOON, 84.0))
        val aspects = AspectCalculator.calculate(positions)
        assertEquals(1, aspects.size)
        assertEquals(AspectType.SQUARE, aspects[0].type)
        assertEquals(6.0, aspects[0].orb, 0.0001)
    }

    @Test
    fun `60 degrees apart yields sextile`() {
        val positions = listOf(pos(CelestialBody.SUN, 0.0), pos(CelestialBody.MOON, 60.0))
        val aspects = AspectCalculator.calculate(positions)
        assertEquals(1, aspects.size)
        assertEquals(AspectType.SEXTILE, aspects[0].type)
        assertEquals(0.0, aspects[0].orb, 0.0001)
    }

    @Test
    fun `0 degrees apart yields conjunction`() {
        val positions = listOf(pos(CelestialBody.SUN, 15.0), pos(CelestialBody.MOON, 15.0))
        val aspects = AspectCalculator.calculate(positions)
        assertEquals(1, aspects.size)
        assertEquals(AspectType.CONJUNCTION, aspects[0].type)
        assertEquals(0.0, aspects[0].orb, 0.0001)
    }

    @Test
    fun `180 degrees apart yields opposition`() {
        val positions = listOf(pos(CelestialBody.SUN, 10.0), pos(CelestialBody.MOON, 190.0))
        val aspects = AspectCalculator.calculate(positions)
        assertEquals(1, aspects.size)
        assertEquals(AspectType.OPPOSITION, aspects[0].type)
        assertEquals(0.0, aspects[0].orb, 0.0001)
    }

    @Test
    fun `separation matching no aspect type yields no aspect`() {
        val positions = listOf(pos(CelestialBody.SUN, 0.0), pos(CelestialBody.MOON, 40.0))
        val aspects = AspectCalculator.calculate(positions)
        assertTrue(aspects.isEmpty())
    }

    @Test
    fun `results are sorted by orb ascending`() {
        // SUN-MOON: 0 apart -> conjunction orb 0
        // SUN-MERCURY: 65 apart -> sextile orb 5
        // SUN-VENUS: 95 apart -> square orb 5 (tie is fine, just verify non-decreasing)
        val positions = listOf(
            pos(CelestialBody.SUN, 0.0),
            pos(CelestialBody.MOON, 0.0),
            pos(CelestialBody.MERCURY, 65.0),
            pos(CelestialBody.VENUS, 95.0),
        )
        val aspects = AspectCalculator.calculate(positions)
        val orbs = aspects.map { it.orb }
        assertEquals(orbs.sorted(), orbs)
    }

    @Test
    fun `boundary separation exactly at max orb is included`() {
        // 90 + 8 = 98 degrees apart -> square, orb exactly 8.0, orb <= 8.0 so included
        val positions = listOf(pos(CelestialBody.SUN, 0.0), pos(CelestialBody.MOON, 98.0))
        val aspects = AspectCalculator.calculate(positions)
        assertEquals(1, aspects.size)
        assertEquals(AspectType.SQUARE, aspects[0].type)
        assertEquals(8.0, aspects[0].orb, 0.0001)
    }

    @Test
    fun `separation just beyond max orb yields no aspect`() {
        // 90 + 8.01 = 98.01 -> orb 8.01 > 8.0, excluded from square;
        // also not within range of any other aspect type
        val positions = listOf(pos(CelestialBody.SUN, 0.0), pos(CelestialBody.MOON, 98.5))
        val aspects = AspectCalculator.calculate(positions)
        assertTrue(aspects.isEmpty())
    }
}
