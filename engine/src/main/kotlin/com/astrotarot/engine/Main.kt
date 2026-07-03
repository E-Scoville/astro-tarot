package com.astrotarot.engine

import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.data.LocalEphemerisCalculator
import com.astrotarot.engine.domain.TarotAstrologyEngine
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun main() {
    val engine = TarotAstrologyEngine(FULL_DECK)

    println("=====================================================")
    println("  REAL-TIME ASTRO-TAROT ENGINE v0.2 (Live Ephemeris)")
    println("=====================================================")
    println()

    print("Enter Local Latitude  (e.g., 40.04): ")
    val lat = readLine()?.toDoubleOrNull() ?: 0.0

    print("Enter Local Longitude (e.g., -111.73): ")
    val lon = readLine()?.toDoubleOrNull() ?: 0.0

    val now = System.currentTimeMillis()
    val utcTime = Instant.ofEpochMilli(now)
        .atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'"))

    println()
    println("Coordinates: $lat°, $lon°   |   $utcTime")
    println("Computing live ephemeris (Meeus algorithms, J2000.0 elements)...")
    println()

    val astroData = LocalEphemerisCalculator.calculate(lat, lon, now)

    println("── Planetary Positions ──────────────────────────────")
    astroData.positions.forEach { pos ->
        val retro = if (pos.isRetrograde) " ℞" else ""
        println("  %-8s  %-12s  %6.1f°  House %2d%s"
            .format(pos.planet, pos.sign, pos.longitude, pos.house, retro))
    }
    println("  Ascendant: ${"%.1f".format(astroData.ascendantDegree)}°   MC: ${"%.1f".format(astroData.midheavenDegree)}°")
    println()

    val reading = engine.generateWeightedReading(astroData.positions, cardsToDraw = 3)

    println("======================= YOUR SPREAD =======================")
    reading.forEachIndexed { index, weightedCard ->
        val position = when (index) {
            0 -> "POSITION 1 — THE ASCENDANT  (Self & Present Path)"
            1 -> "POSITION 2 — THE MIDHEAVEN  (Action & Direction)"
            else -> "POSITION 3 — THE IC        (Foundation & Hidden Roots)"
        }
        val orientation = if (weightedCard.reversed) " [REVERSED]" else ""
        val meaning = if (weightedCard.reversed) weightedCard.card.reversedDescription
                      else weightedCard.card.baseDescription

        println()
        println(position)
        println("Card:    ${weightedCard.card.name}$orientation")
        println("Weight:  ${"%.2f".format(weightedCard.weight)}x")
        println("Meaning: $meaning")
        println("-----------------------------------------------------------")
    }
    println()
}
