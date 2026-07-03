package com.astrotarot.engine.data

import com.astrotarot.engine.domain.model.AstroTransitResponse
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.ZodiacSign
import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.*

/**
 * Offline ephemeris calculator based on Jean Meeus "Astronomical Algorithms" (2nd ed).
 * Accuracy: ~0.5–1° for planets, ~0.3° for Moon — sufficient for sign and decan mapping.
 * Uses Whole Sign house system.
 */
object LocalEphemerisCalculator {

    // Orbital elements at J2000.0 from Meeus Table 31.a
    // L0: mean longitude (°), n: mean motion (°/Julian century),
    // e0: eccentricity, eDot: eccentricity rate (/cy),
    // w0: longitude of perihelion (°), wDot: perihelion rate (°/cy),
    // a: semi-major axis (AU)
    private data class OrbitalElements(
        val L0: Double, val n: Double,
        val e0: Double, val eDot: Double,
        val w0: Double, val wDot: Double,
        val a: Double
    )

    private val EARTH   = OrbitalElements(100.464457,  35999.372851,  0.01671022, -0.00004204, 102.93734808,  0.32111, 1.000001018)
    private val MERCURY = OrbitalElements(252.250906, 149472.6746358, 0.20563175,  0.00002527,  77.45779628,  0.15940, 0.387098310)
    private val VENUS   = OrbitalElements(181.979801,  58517.8156760, 0.00677323, -0.00004938, 131.56370300,  0.00268, 0.723329820)
    private val MARS    = OrbitalElements(355.433275,  19140.2993313, 0.09340062,  0.00009048, 336.04084185,  0.44441, 1.523679342)
    private val JUPITER = OrbitalElements( 34.351519,   3034.9056606, 0.04849485,  0.00016322,  14.33120687,  0.18930, 5.202545)
    private val SATURN  = OrbitalElements( 50.077444,   1222.1137943, 0.05550862, -0.00032044,  93.05723748,  0.54549, 9.554909)
    private val URANUS  = OrbitalElements(314.055005,    428.4669983, 0.04629590, -0.00002668, 173.00529820,  0.09099, 19.218140)
    private val NEPTUNE = OrbitalElements(304.348665,    218.4862002, 0.00898809,  0.00000602,  48.12027554,  0.01649, 30.110387)
    private val PLUTO   = OrbitalElements(238.928881,    145.1809475, 0.24882730,  0.0,        224.06891629,  0.0,    39.543828)

    fun calculate(lat: Double, lon: Double, utcTimestamp: Long): AstroTransitResponse {
        val jd = toJulianDay(utcTimestamp)
        val T  = (jd - 2451545.0) / 36525.0
        val D  = jd - 2451545.0

        val obliquity = 23.439291111 - 0.013004167 * T
        val earthXY   = heliocentricXY(T, EARTH)

        // Sun is directly opposite Earth's heliocentric position
        val sunLon = atan2(-earthXY.second, -earthXY.first).toDeg().norm360()

        val ascendant = calculateAscendant(jd, lat, lon, obliquity)
        val mc        = calculateMC(jd, lon, obliquity)

        val positions = mutableListOf<PlanetPosition>()

        fun add(body: CelestialBody, longitude: Double, retrograde: Boolean = false) {
            val sign  = signOf(longitude)
            val house = wholeSignHouse(longitude, ascendant)
            positions.add(PlanetPosition(body, sign.name, longitude, house, retrograde))
        }

        add(CelestialBody.SUN,  sunLon)
        add(CelestialBody.MOON, moonLongitude(D, T))

        listOf(
            CelestialBody.MERCURY to MERCURY,
            CelestialBody.VENUS   to VENUS,
            CelestialBody.MARS    to MARS,
            CelestialBody.JUPITER to JUPITER,
            CelestialBody.SATURN  to SATURN,
            CelestialBody.URANUS  to URANUS,
            CelestialBody.NEPTUNE to NEPTUNE,
            CelestialBody.PLUTO   to PLUTO
        ).forEach { (body, el) ->
            val geoLon   = geocentricLongitude(T, el, earthXY)
            val retrograde = isRetrograde(T, el)
            add(body, geoLon, retrograde)
        }

        return AstroTransitResponse(
            status = "ok",
            ascendantDegree = ascendant,
            midheavenDegree = mc,
            positions = positions
        )
    }

    // ── Core math ────────────────────────────────────────────────────────────

    /** Iteratively solve Kepler's equation M = E − e·sin(E). Converges in ~5 steps. */
    private fun keplerSolve(M: Double, e: Double): Double {
        var E = M
        repeat(10) { E = M + e * sin(E) }
        return E
    }

    /** Heliocentric Cartesian (x, y) in AU for the ecliptic plane. */
    private fun heliocentricXY(T: Double, el: OrbitalElements): Pair<Double, Double> {
        val e = el.e0 + el.eDot * T
        val w = (el.w0 + el.wDot * T).norm360().toRad()   // longitude of perihelion
        val L = (el.L0 + el.n * T).norm360().toRad()       // mean longitude
        val M = (L - w + TWO_PI).mod(TWO_PI)               // mean anomaly
        val E = keplerSolve(M, e)                           // eccentric anomaly
        val v = 2 * atan2(sqrt(1 + e) * sin(E / 2), sqrt(1 - e) * cos(E / 2))  // true anomaly
        val r = el.a * (1 - e * cos(E))                    // radius vector (AU)
        val trueLon = (v + w)
        return r * cos(trueLon) to r * sin(trueLon)
    }

    /** Geocentric ecliptic longitude by subtracting Earth's position vector. */
    private fun geocentricLongitude(T: Double, el: OrbitalElements, earthXY: Pair<Double, Double>): Double {
        val (xp, yp) = heliocentricXY(T, el)
        val (xe, ye) = earthXY
        return atan2(yp - ye, xp - xe).toDeg().norm360()
    }

    /** Simplified Moon longitude (Meeus Ch. 47, top-10 terms). Accurate to ~0.3°. */
    private fun moonLongitude(D: Double, T: Double): Double {
        val Lp = (218.3165 + 13.17639648 * D).norm360()
        val M  = (357.52911 + 35999.05029 * T).norm360().toRad()
        val Mp = (134.9634  + 13.06499295 * D).norm360().toRad()
        val Dm = (297.8502  + 12.19074912 * D).norm360().toRad()
        val F  = ( 93.2721  + 13.22935020 * D).norm360().toRad()

        return (Lp
                + 6.2886 * sin(Mp)
                + 1.2740 * sin(2 * Dm - Mp)
                + 0.6583 * sin(2 * Dm)
                + 0.2136 * sin(2 * Mp)
                - 0.1851 * sin(M)
                - 0.1143 * sin(2 * F)
                + 0.0588 * sin(2 * Dm - 2 * Mp)
                + 0.0572 * sin(2 * Dm - M - Mp)
                + 0.0533 * sin(2 * Dm + Mp)
                ).norm360()
    }

    /** Retrograde: compare geocentric longitude 1 day forward. */
    private fun isRetrograde(T: Double, el: OrbitalElements): Boolean {
        val T2 = T + 1.0 / 36525.0
        val earth1 = heliocentricXY(T, EARTH)
        val earth2 = heliocentricXY(T2, EARTH)
        val lon1 = geocentricLongitude(T, el, earth1)
        val lon2 = geocentricLongitude(T2, el, earth2)
        return ((lon2 - lon1 + 360) % 360) > 180
    }

    // ── House & sign ─────────────────────────────────────────────────────────

    /** Whole Sign houses: sign containing the Ascendant = House 1. */
    private fun wholeSignHouse(planetLon: Double, ascendant: Double): Int {
        val ascSign    = (ascendant / 30).toInt()
        val planetSign = (planetLon / 30).toInt()
        return ((planetSign - ascSign + 12) % 12) + 1
    }

    private fun signOf(lon: Double): ZodiacSign =
        ZodiacSign.entries[(lon.norm360() / 30.0).toInt().coerceIn(0, 11)]

    // ── Angles (Ascendant / MC) ───────────────────────────────────────────────

    /** Greenwich Mean Sidereal Time → Local Sidereal Time → Ascendant. */
    private fun calculateAscendant(jd: Double, lat: Double, lon: Double, obliquity: Double): Double {
        val RAMC = ((280.46061837 + 360.98564736629 * (jd - 2451545.0)).norm360() + lon).norm360().toRad()
        val e    = obliquity.toRad()
        val phi  = lat.toRad()
        return atan2(-cos(RAMC), sin(RAMC) * cos(e) + tan(phi) * sin(e)).toDeg().norm360()
    }

    /** Midheaven (MC) from Local Sidereal Time. */
    private fun calculateMC(jd: Double, lon: Double, obliquity: Double): Double {
        val RAMC = ((280.46061837 + 360.98564736629 * (jd - 2451545.0)).norm360() + lon).norm360().toRad()
        val e    = obliquity.toRad()
        return atan2(sin(RAMC), cos(RAMC) * cos(e)).toDeg().norm360()
    }

    // ── Julian Day ───────────────────────────────────────────────────────────

    fun toJulianDay(utcTimestamp: Long): Double {
        val dt = Instant.ofEpochMilli(utcTimestamp).atOffset(ZoneOffset.UTC)
        var year  = dt.year.toDouble()
        var month = dt.monthValue.toDouble()
        val day   = dt.dayOfMonth + dt.hour / 24.0 + dt.minute / 1440.0 + dt.second / 86400.0
        if (month <= 2) { year -= 1; month += 12 }
        val A = floor(year / 100)
        val B = 2 - A + floor(A / 4)
        return floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + B - 1524.5
    }

    // ── Extension helpers ─────────────────────────────────────────────────────

    private fun Double.toRad()   = this * PI / 180
    private fun Double.toDeg()   = this * 180 / PI
    private fun Double.norm360() = ((this % 360) + 360) % 360
    private val TWO_PI           = 2 * PI
}
