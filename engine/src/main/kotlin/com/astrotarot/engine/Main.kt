package com.astrotarot.engine

import com.astrotarot.engine.data.FULL_DECK
import com.astrotarot.engine.domain.TarotAstrologyEngine
import com.astrotarot.engine.domain.model.PlanetPosition

fun main() {
    val engine = TarotAstrologyEngine(FULL_DECK)

    println("=====================================================")
    println("  REAL-TIME ASTRO-TAROT ENGINE v0.1 (CLI MVP)")
    println("=====================================================")
    println()

    print("Enter Local Latitude  (e.g., 40.04): ")
    val lat = readLine()?.toDoubleOrNull() ?: 0.0

    print("Enter Local Longitude (e.g., -111.73): ")
    val lon = readLine()?.toDoubleOrNull() ?: 0.0

    println()
    println("Coordinates received: $lat, $lon")
    println("Calculating weighted reading from mock transit data...")
    println("(Live ephemeris API will replace this in Phase 2)")
    println()

    // Phase 1: mock transits. Replace with real API call in Phase 2.
    val transits = listOf(
        PlanetPosition("SUN",     "GEMINI",      85.1,  3),
        PlanetPosition("MOON",    "CANCER",       4.5,  4),
        PlanetPosition("MERCURY", "GEMINI",      68.2,  3),
        PlanetPosition("VENUS",   "TAURUS",      42.8,  2),
        PlanetPosition("MARS",    "ARIES",       12.4,  1),
        PlanetPosition("JUPITER", "GEMINI",      71.4,  3),
        PlanetPosition("SATURN",  "PISCES",     335.7, 12),
        PlanetPosition("URANUS",  "TAURUS",      54.9,  2),
        PlanetPosition("NEPTUNE", "PISCES",     357.1, 12),
        PlanetPosition("PLUTO",   "AQUARIUS",   301.8, 11)
    )

    val reading = engine.generateWeightedReading(transits, cardsToDraw = 3)

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
