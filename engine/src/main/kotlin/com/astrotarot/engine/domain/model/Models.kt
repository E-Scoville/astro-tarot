package com.astrotarot.engine.domain.model

import kotlinx.serialization.Serializable

enum class CelestialBody {
    SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN, URANUS, NEPTUNE, PLUTO
}

enum class Element { FIRE, EARTH, AIR, WATER }

enum class ZodiacSign(val startDegree: Double, val element: Element) {
    ARIES(0.0, Element.FIRE),
    TAURUS(30.0, Element.EARTH),
    GEMINI(60.0, Element.AIR),
    CANCER(90.0, Element.WATER),
    LEO(120.0, Element.FIRE),
    VIRGO(150.0, Element.EARTH),
    LIBRA(180.0, Element.AIR),
    SCORPIO(210.0, Element.WATER),
    SAGITTARIUS(240.0, Element.FIRE),
    CAPRICORN(270.0, Element.EARTH),
    AQUARIUS(300.0, Element.AIR),
    PISCES(330.0, Element.WATER)
}

enum class Suit(val element: Element) {
    WANDS(Element.FIRE),
    CUPS(Element.WATER),
    SWORDS(Element.AIR),
    PENTACLES(Element.EARTH)
}

enum class ArcanaType { MAJOR, MINOR_NUMBERED, MINOR_ACE, MINOR_COURT }

@Serializable
data class PlanetPosition(
    val planet: String,
    val sign: String,
    val longitude: Double,
    val house: Int,
    val isRetrograde: Boolean = false
)

@Serializable
data class AstroTransitResponse(
    val status: String,
    val ascendantDegree: Double = 0.0,
    val midheavenDegree: Double = 0.0,
    val positions: List<PlanetPosition>
)

data class TarotCard(
    val name: String,
    val type: ArcanaType,
    val suit: Suit? = null,
    val associatedBody: CelestialBody? = null,
    val associatedSign: ZodiacSign? = null,
    val longitudeRangeStart: Double? = null,
    val longitudeRangeEnd: Double? = null,
    val baseDescription: String,
    val reversedDescription: String
)

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val utcTimestamp: Long = System.currentTimeMillis()
)

data class WeightedCard(
    val card: TarotCard,
    val weight: Double,
    val reversed: Boolean
)

enum class AspectType(
    val angle: Double,
    val orb: Double,
    val weightBonus: Double,
    val symbol: String,
    val label: String,
    val isHarmonious: Boolean
) {
    CONJUNCTION( 0.0,  8.0, 2.00, "☌", "Conjunction", true),
    SEXTILE    (60.0,  6.0, 0.75, "✶", "Sextile",     true),
    SQUARE     (90.0,  8.0, 1.25, "□", "Square",      false),
    TRINE      (120.0, 8.0, 1.00, "△", "Trine",       true),
    OPPOSITION (180.0, 8.0, 1.50, "☍", "Opposition",  false)
}

data class Aspect(
    val planet1: String,
    val planet2: String,
    val type: AspectType,
    val orb: Double   // degrees from exact
)
