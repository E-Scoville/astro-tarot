package com.astrotarot.engine

import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.data.LocalEphemerisCalculator
import com.astrotarot.engine.domain.TarotAstrologyEngine
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val INPUT_FORMAT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")

fun main() {
    val engine = TarotAstrologyEngine(FULL_DECK)

    println("=====================================================")
    println("  ASTRO-TAROT ENGINE v0.3")
    println("=====================================================")
    println()

    print("Enter Local Latitude  (e.g., 40.04): ")
    val lat = readLine()?.toDoubleOrNull() ?: 0.0

    print("Enter Local Longitude (e.g., -111.73): ")
    val lon = readLine()?.toDoubleOrNull() ?: 0.0

    println()
    print("Use current time? (Y/n): ")
    val useNow = readLine()?.trim()?.lowercase()?.let { it == "" || it == "y" } ?: true

    val timestamp: Long
    val utcLabel: String

    if (useNow) {
        timestamp = System.currentTimeMillis()
        utcLabel  = Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).format(DISPLAY_FORMAT)
    } else {
        println("Enter date and time in UTC (format: yyyy-MM-dd HH:mm)")
        println("  Historical example: 1969-07-20 20:17")
        println("  Future example:     2033-01-01 00:00")
        print("Date/time: ")
        val input = readLine()?.trim() ?: ""
        timestamp = try {
            LocalDateTime.parse(input, INPUT_FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli()
        } catch (e: DateTimeParseException) {
            println("Could not parse \"$input\" — falling back to current time.")
            System.currentTimeMillis()
        }
        utcLabel = Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).format(DISPLAY_FORMAT)
    }

    println()
    println("Coordinates: $lat°, $lon°   |   $utcLabel")
    println("Computing ephemeris (Meeus algorithms, J2000.0 elements)...")
    println()

    val astroData = LocalEphemerisCalculator.calculate(lat, lon, timestamp)

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
