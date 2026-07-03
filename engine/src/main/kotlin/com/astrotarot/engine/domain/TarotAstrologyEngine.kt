package com.astrotarot.engine.domain

import com.astrotarot.engine.domain.model.*
import kotlin.random.Random

class TarotAstrologyEngine(private val deck: List<TarotCard>) {

    fun generateWeightedReading(
        transits: List<PlanetPosition>,
        cardsToDraw: Int,
        random: Random = Random.Default
    ): List<WeightedCard> {
        val aspects = AspectCalculator.calculate(transits)
        val planetLookup = transits.associateBy { it.planet }

        val weights = deck.map { card ->
            val transitWeight = calculateTransitWeight(card, transits)
            val aspectBonus   = calculateAspectBonus(card, aspects, planetLookup)
            card to (transitWeight + aspectBonus)
        }

        val avgWeight = weights.map { it.second }.average()
        val drawn = weightedRandomSample(weights, cardsToDraw, random)
        return drawn.map { (card, weight) ->
            WeightedCard(card, weight, reversed = weight < avgWeight)
        }
    }

    // ── Transit weighting (unchanged from Phase 1) ────────────────────────────

    private fun calculateTransitWeight(card: TarotCard, transits: List<PlanetPosition>): Double {
        var weight = 1.0

        for (transit in transits) {
            val body = transit.planet.uppercase()
            val sign = runCatching { ZodiacSign.valueOf(transit.sign.uppercase()) }.getOrNull()
            val inAngular = transit.house in ANGULAR_HOUSES

            when (card.type) {
                ArcanaType.MAJOR -> {
                    if (card.associatedBody?.name == body) {
                        weight += 1.5
                        if (inAngular) weight += 1.0
                        if (transit.isRetrograde) weight += 0.5
                    }
                    if (sign != null && card.associatedSign == sign) {
                        weight += 0.75
                        if (inAngular) weight += 0.5
                    }
                }
                ArcanaType.MINOR_NUMBERED -> {
                    val inRange = card.longitudeRangeStart != null &&
                            inLongitudeRange(transit.longitude, card.longitudeRangeStart, card.longitudeRangeEnd!!)
                    val bodyMatches = card.associatedBody?.name == body
                    val signMatches = sign != null && card.associatedSign == sign

                    weight += when {
                        inRange && bodyMatches -> 3.0
                        bodyMatches && signMatches -> 1.5
                        signMatches -> 0.5
                        else -> 0.0
                    }
                }
                ArcanaType.MINOR_ACE -> {
                    if (sign != null && card.suit?.element == sign.element) {
                        weight += 0.75
                        if (inAngular) weight += 0.5
                    }
                }
                ArcanaType.MINOR_COURT -> {
                    if (card.longitudeRangeStart != null && card.longitudeRangeEnd != null) {
                        if (inLongitudeRange(transit.longitude, card.longitudeRangeStart, card.longitudeRangeEnd)) {
                            weight += 2.0
                            if (inAngular) weight += 0.5
                        }
                    } else if (sign != null && card.suit?.element == sign.element) {
                        weight += 0.5
                    }
                }
            }
        }

        return weight
    }

    // ── Aspect weighting ──────────────────────────────────────────────────────

    private fun calculateAspectBonus(
        card: TarotCard,
        aspects: List<Aspect>,
        planetLookup: Map<String, PlanetPosition>
    ): Double {
        var bonus = 0.0

        for (aspect in aspects) {
            val p1 = planetLookup[aspect.planet1] ?: continue
            val p2 = planetLookup[aspect.planet2] ?: continue

            val involved = cardInvolvesPlanet(card, p1) || cardInvolvesPlanet(card, p2)
            if (!involved) continue

            // Tighter orb = stronger bonus (linear falloff to 0 at max orb)
            val orbFactor = 1.0 - (aspect.orb / aspect.type.orb)
            bonus += aspect.type.weightBonus * orbFactor
        }

        return bonus
    }

    /**
     * True if the given planet directly resonates with this card.
     * Major Arcana: ruled by a planet or sign.
     * Minor Numbered / Court: planet occupies the card's decan or degree range.
     * Aces: planet's sign shares the card's elemental suit.
     */
    private fun cardInvolvesPlanet(card: TarotCard, planet: PlanetPosition): Boolean {
        val sign = runCatching { ZodiacSign.valueOf(planet.sign.uppercase()) }.getOrNull()
        return when (card.type) {
            ArcanaType.MAJOR ->
                card.associatedBody?.name == planet.planet ||
                (sign != null && card.associatedSign == sign)

            ArcanaType.MINOR_NUMBERED ->
                card.associatedBody?.name == planet.planet ||
                (sign != null && card.associatedSign == sign) ||
                (card.longitudeRangeStart != null &&
                        inLongitudeRange(planet.longitude, card.longitudeRangeStart, card.longitudeRangeEnd!!))

            ArcanaType.MINOR_COURT ->
                card.longitudeRangeStart != null &&
                inLongitudeRange(planet.longitude, card.longitudeRangeStart, card.longitudeRangeEnd!!)

            ArcanaType.MINOR_ACE ->
                sign != null && card.suit?.element == sign.element
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun inLongitudeRange(longitude: Double, start: Double, end: Double): Boolean =
        if (start <= end) longitude >= start && longitude < end
        else longitude >= start || longitude < end

    private fun weightedRandomSample(
        weightedCards: List<Pair<TarotCard, Double>>,
        n: Int,
        random: Random
    ): List<Pair<TarotCard, Double>> {
        val result = mutableListOf<Pair<TarotCard, Double>>()
        val pool = weightedCards.toMutableList()

        repeat(n.coerceAtMost(pool.size)) {
            val totalWeight = pool.sumOf { it.second }
            var threshold = random.nextDouble() * totalWeight
            val selected = pool.first { (_, w) -> threshold -= w; threshold <= 0 }
            result.add(selected)
            pool.remove(selected)
        }

        return result
    }

    companion object {
        private val ANGULAR_HOUSES = setOf(1, 4, 7, 10)
    }
}
