package com.astrotarot.ui

import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.CelestialBody

/**
 * Plain-language lore shown when the user taps a planetary influence or aspect.
 * Voice: attributed to "the tradition" rather than asserted as fact,
 * matching the InfoScreen's preserve-the-mystery philosophy.
 */

data class Lore(val glyph: String, val title: String, val text: String)

val PLANET_LORE = mapOf(
    CelestialBody.SUN to Lore("☉", "The Sun",
        "The Sun is the center of the old cosmology and of the self: vitality, identity, " +
        "and the will made visible. When the Sun calls a card forth, the matter touches who " +
        "you are in the open — the life you live in daylight, where nothing can stay hidden."),
    CelestialBody.MOON to Lore("☽", "The Moon",
        "The Moon governs the tides, and the tradition holds she governs everything else " +
        "that ebbs and flows: memory, mood, instinct, the life of the night mind. A card " +
        "drawn by the Moon speaks to what moves beneath the surface — felt long before it " +
        "is understood."),
    CelestialBody.MERCURY to Lore("☿", "Mercury",
        "Mercury is the messenger: language, thought, cleverness, commerce, and every " +
        "crossing of a threshold. When Mercury summons a card, the matter turns on words " +
        "spoken or unspoken — a message, a decision, an idea in motion."),
    CelestialBody.VENUS to Lore("♀", "Venus",
        "Venus rules attraction in all its forms: love, beauty, pleasure, harmony, and the " +
        "ties that bind people to one another. A card under her influence concerns what the " +
        "heart is drawn toward — and what it costs to have it."),
    CelestialBody.MARS to Lore("♂", "Mars",
        "Mars is the old god of war and the planet of force: courage, desire, conflict, and " +
        "the will to act. When Mars drives a card into your reading, something demands to be " +
        "fought for, defended, or finally confronted."),
    CelestialBody.JUPITER to Lore("♃", "Jupiter",
        "Jupiter is the greater benefic, the planet of expansion: fortune, generosity, " +
        "growth, and doors swinging open. A card called by Jupiter carries the question of " +
        "opportunity — what is offered, and whether you are large enough to receive it."),
    CelestialBody.SATURN to Lore("♄", "Saturn",
        "Saturn is the planet of limits: time, discipline, duty, and the walls we build or " +
        "break against. The old astrologers feared him and respected him in equal measure. " +
        "A card under Saturn concerns what must be endured, structured, or paid for in full."),
    CelestialBody.URANUS to Lore("♅", "Uranus",
        "Uranus was unknown to the ancients — fittingly, for it is the planet of the " +
        "unforeseen: sudden change, rebellion, lightning insight, the breaking of patterns. " +
        "A card it summons arrives to disrupt what had seemed settled."),
    CelestialBody.NEPTUNE to Lore("♆", "Neptune",
        "Neptune rules the boundless: dreams, illusions, faith, dissolution, the sea without " +
        "a shore. A card under Neptune's influence asks what is real and what is longed " +
        "for — and warns how easily the two are confused."),
    CelestialBody.PLUTO to Lore("♇", "Pluto",
        "Pluto, lord of the underworld, governs what happens in the dark: endings, power, " +
        "obsession, and the slow transformation that destroys one form to make another. " +
        "A card Pluto brings does not concern small things."),
)

val ASPECT_LORE = mapOf(
    AspectType.CONJUNCTION to Lore("☌", "Conjunction",
        "Two planets standing at the same degree of the sky, their natures fused into a " +
        "single force. Whatever they touch is intensified — this card was not whispered " +
        "into your reading but pushed."),
    AspectType.SEXTILE to Lore("✶", "Sextile",
        "Two planets set sixty degrees apart, a spacing the tradition reads as opportunity: " +
        "energies that cooperate willingly when invited. This card arrives on friendly " +
        "terms — an opening, if you take it."),
    AspectType.TRINE to Lore("△", "Trine",
        "Two planets a third of the sky apart, the most harmonious angle the heavens make. " +
        "Their natures flow into each other without resistance, and the card they favor " +
        "comes to you with ease — perhaps too much ease to be examined closely."),
    AspectType.SQUARE to Lore("□", "Square",
        "Two planets at right angles, each blocking the other's road. The old astrologers " +
        "read the square as friction — energy that grinds and forces a choice. A card marked " +
        "by a square appears against the current of the sky: it fights its way into your " +
        "reading, and turns reversed in the struggle."),
    AspectType.OPPOSITION to Lore("☍", "Opposition",
        "Two planets facing each other across the whole of the heavens, pulling in contrary " +
        "directions like a rope drawn taut. A card under opposition stands at the center of " +
        "that tension — reversed, because the sky itself is divided about it."),
)

val RETROGRADE_LORE = Lore("℞", "Retrograde",
    "A planet appearing to move backward against the stars — an illusion of orbit, but the " +
    "tradition has always read it as a power turned inward, delayed, or under revision. " +
    "A card whose ruler is retrograde arrives reversed: its energy is present but withheld, " +
    "working beneath the surface until the planet turns forward again.")

/** Lore for the marker attached to a reversed card ("℞" or an aspect symbol). */
fun markerLore(marker: String?): Lore? = when (marker) {
    null -> null
    "℞"  -> RETROGRADE_LORE
    else -> AspectType.entries.find { it.symbol == marker }?.let { ASPECT_LORE[it] }
}
