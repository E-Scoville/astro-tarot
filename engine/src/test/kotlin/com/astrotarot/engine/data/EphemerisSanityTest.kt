package com.astrotarot.engine.data

import com.astrotarot.engine.domain.model.CelestialBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EphemerisSanityTest {

    private fun utc(iso: String): Long = Instant.parse(iso).toEpochMilli()

    /** Angular distance handling wraparound near 0/360. */
    private fun angularDiff(a: Double, b: Double): Double {
        val diff = Math.abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }

    private fun sunLongitude(timestamp: Long): Double {
        val response = LocalEphemerisCalculator.calculate(0.0, 0.0, timestamp)
        return response.positions.first { it.planet == CelestialBody.SUN }.longitude
    }

    @Test
    fun `sun longitude near 0 at 2000 vernal equinox`() {
        val lon = sunLongitude(utc("2000-03-20T07:35:00Z"))
        assertTrue("expected ~0 got $lon", angularDiff(lon, 0.0) <= 1.0)
    }

    @Test
    fun `sun longitude near 90 at 2000 summer solstice`() {
        val lon = sunLongitude(utc("2000-06-21T01:48:00Z"))
        assertTrue("expected ~90 got $lon", angularDiff(lon, 90.0) <= 1.0)
    }

    @Test
    fun `sun longitude near 180 at 2000 autumnal equinox`() {
        val lon = sunLongitude(utc("2000-09-22T17:28:00Z"))
        assertTrue("expected ~180 got $lon", angularDiff(lon, 180.0) <= 1.0)
    }

    @Test
    fun `sun longitude near 270 at 2000 winter solstice`() {
        val lon = sunLongitude(utc("2000-12-21T13:37:00Z"))
        assertTrue("expected ~270 got $lon", angularDiff(lon, 270.0) <= 1.0)
    }

    @Test
    fun `sun longitude near 0 at 2024 vernal equinox`() {
        val lon = sunLongitude(utc("2024-03-20T03:06:00Z"))
        assertTrue("expected ~0 got $lon", angularDiff(lon, 0.0) <= 1.0)
    }

    @Test
    fun `sign matches longitude bucket for all positions`() {
        val response = LocalEphemerisCalculator.calculate(40.0, -74.0, utc("2024-06-15T12:00:00Z"))
        for (p in response.positions) {
            val expectedIndex = (p.longitude / 30.0).toInt().coerceIn(0, 11)
            val expectedSign = com.astrotarot.engine.domain.model.ZodiacSign.entries[expectedIndex]
            assertEquals("mismatch for ${p.planet}", expectedSign.name, p.sign)
        }
    }

    @Test
    fun `all longitudes in range 0 to 360`() {
        val response = LocalEphemerisCalculator.calculate(51.5, -0.1, utc("2010-01-01T00:00:00Z"))
        for (p in response.positions) {
            assertTrue("${p.planet} longitude ${p.longitude} out of range", p.longitude >= 0.0 && p.longitude < 360.0)
        }
    }

    @Test
    fun `all houses in range 1 to 12`() {
        val response = LocalEphemerisCalculator.calculate(51.5, -0.1, utc("2010-01-01T00:00:00Z"))
        for (p in response.positions) {
            assertTrue("${p.planet} house ${p.house} out of range", p.house in 1..12)
        }
    }

    @Test
    fun `exactly ten bodies returned one per CelestialBody no duplicates`() {
        val response = LocalEphemerisCalculator.calculate(0.0, 0.0, utc("1999-05-05T05:05:00Z"))
        assertEquals(10, response.positions.size)
        val bodies = response.positions.map { it.planet }.toSet()
        assertEquals(CelestialBody.entries.toSet(), bodies)
        assertEquals(10, response.positions.map { it.planet }.distinct().size)
    }

    @Test
    fun `sun and moon are never retrograde`() {
        // Sample across several dates
        val timestamps = listOf(
            utc("2000-01-01T00:00:00Z"),
            utc("2010-06-15T12:00:00Z"),
            utc("2020-12-25T00:00:00Z"),
            utc("2024-03-20T03:06:00Z"),
        )
        for (ts in timestamps) {
            val response = LocalEphemerisCalculator.calculate(10.0, 10.0, ts)
            val sun = response.positions.first { it.planet == CelestialBody.SUN }
            val moon = response.positions.first { it.planet == CelestialBody.MOON }
            assertFalse("Sun retrograde at $ts", sun.isRetrograde)
            assertFalse("Moon retrograde at $ts", moon.isRetrograde)
        }
    }

    @Test
    fun `identical inputs produce identical results (determinism)`() {
        val ts = utc("2023-07-04T15:30:00Z")
        val r1 = LocalEphemerisCalculator.calculate(37.7749, -122.4194, ts)
        val r2 = LocalEphemerisCalculator.calculate(37.7749, -122.4194, ts)
        assertEquals(r1, r2)
    }

    @Test
    fun `ascendant differs at same instant for very different longitudes`() {
        val ts = utc("2024-01-01T00:00:00Z")
        val r1 = LocalEphemerisCalculator.calculate(0.0, 0.0, ts)
        val r2 = LocalEphemerisCalculator.calculate(0.0, 120.0, ts)
        assertNotEquals(r1.ascendantDegree, r2.ascendantDegree, 0.0001)
    }
}
