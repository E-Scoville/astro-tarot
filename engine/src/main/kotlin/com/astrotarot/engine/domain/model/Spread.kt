package com.astrotarot.engine.domain.model

data class SpreadPosition(
    val label: String,          // e.g. "I — The Ascendant · Self & Present"
    val house: Int? = null,     // optional Whole Sign house binding (1..12)
)

data class Spread(
    val id: String,             // stable key: "single", "angles", "houses"
    val name: String,
    val tagline: String,        // one elegant line for the picker
    val positions: List<SpreadPosition>,
)

object Spreads {
    val SINGLE = Spread("single", "The Card of the Moment",
        "A single card, drawn by the sky as it stands right now.",
        listOf(SpreadPosition("The Moment")))

    val ANGLES = Spread("angles", "The Three Angles",
        "Ascendant, Midheaven, and the root of the sky — the classic reading.",
        listOf(
            SpreadPosition("I — The Ascendant · Self & Present", house = 1),
            SpreadPosition("II — The Midheaven · Action & Calling", house = 10),
            SpreadPosition("III — The Root · Foundation & Hidden Things", house = 4),
        ))

    val HOUSES = Spread("houses", "The Twelve Houses",
        "One card for every house of the heavens — the full portrait.",
        listOf(
            SpreadPosition("I — The House of Self", house = 1),
            SpreadPosition("II — The House of Worth", house = 2),
            SpreadPosition("III — The House of Words", house = 3),
            SpreadPosition("IV — The House of Roots", house = 4),
            SpreadPosition("V — The House of Joy", house = 5),
            SpreadPosition("VI — The House of Labor", house = 6),
            SpreadPosition("VII — The House of Others", house = 7),
            SpreadPosition("VIII — The House of Depths", house = 8),
            SpreadPosition("IX — The House of the Journey", house = 9),
            SpreadPosition("X — The House of the Summit", house = 10),
            SpreadPosition("XI — The House of Allies", house = 11),
            SpreadPosition("XII — The House of the Hidden", house = 12),
        ))

    val ALL = listOf(SINGLE, ANGLES, HOUSES)
    fun byId(id: String): Spread = ALL.find { it.id == id } ?: ANGLES
}
