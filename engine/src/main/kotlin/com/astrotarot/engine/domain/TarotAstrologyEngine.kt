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
            val (transitWeight, topPlanet) = calculateTransitWeight(card, transits)
            val aspectBonus               = calculateAspectBonus(card, aspects, planetLookup)
            Triple(card, transitWeight + aspectBonus, topPlanet)
        }

        val avgWeight = weights.map { it.second }.average()
        val drawn = weightedRandomSample(
            weights.map { (card, w, _) -> card to w },
            cardsToDraw, random,
        )
        val influenceMap = weights.associate { (card, _, planet) -> card to planet }
        return drawn.map { (card, weight) ->
            val reversed = weight < avgWeight
            if (reversed) {
                val (rPlanet, rMarker) = findReversalInfluence(card, transits, aspects)
                WeightedCard(card, weight, reversed = true,
                    primaryInfluence = rPlanet, reversalMarker = rMarker)
            } else {
                WeightedCard(card, weight, reversed = false,
                    primaryInfluence = influenceMap[card])
            }
        }
    }

    // ── Transit weighting ─────────────────────────────────────────────────────
    // Returns total weight and the name of the planet that contributed most.

    private fun calculateTransitWeight(
        card: TarotCard,
        transits: List<PlanetPosition>,
    ): Pair<Double, String?> {
        var totalWeight = 1.0
        var topPlanet: String? = null
        var topContrib = 0.0

        for (transit in transits) {
            val body = transit.planet.uppercase()
            val sign = runCatching { ZodiacSign.valueOf(transit.sign.uppercase()) }.getOrNull()
            val inAngular = transit.house in ANGULAR_HOUSES

            val contrib = when (card.type) {
                ArcanaType.MAJOR -> {
                    var c = 0.0
                    if (card.associatedBody?.name == body) {
                        c += 1.5
                        if (inAngular) c += 1.0
                        if (transit.isRetrograde) c += 0.5
                    }
                    if (sign != null && card.associatedSign == sign) {
                        c += 0.75
                        if (inAngular) c += 0.5
                    }
                    c
                }
                ArcanaType.MINOR_NUMBERED -> {
                    val inRange = card.longitudeRangeStart != null &&
                            inLongitudeRange(transit.longitude, card.longitudeRangeStart, card.longitudeRangeEnd!!)
                    val bodyMatches = card.associatedBody?.name == body
                    val signMatches = sign != null && card.associatedSign == sign
                    when {
                        inRange && bodyMatches -> 3.0
                        bodyMatches && signMatches -> 1.5
                        signMatches -> 0.5
                        else -> 0.0
                    }
                }
                ArcanaType.MINOR_ACE -> {
                    if (sign != null && card.suit?.element == sign.element) {
                        if (inAngular) 1.25 else 0.75
                    } else 0.0
                }
                ArcanaType.MINOR_COURT -> {
                    if (card.longitudeRangeStart != null && card.longitudeRangeEnd != null &&
                        inLongitudeRange(transit.longitude, card.longitudeRangeStart, card.longitudeRangeEnd)) {
                        if (inAngular) 2.5 else 2.0
                    } else if (sign != null && card.suit?.element == sign.element) {
                        0.5
                    } else 0.0
                }
            }

            totalWeight += contrib
            if (contrib > topContrib) {
                topContrib = contrib
                topPlanet = transit.planet
            }
        }

        return Pair(totalWeight, topPlanet)
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

    // ── Reversal influence ────────────────────────────────────────────────────
    // A reversed card emerged despite low planetary weight. Find what in the
    // current sky best explains the resistance, in priority order:
    //   1. The card's own ruling planet is retrograde
    //   2. The card's ruling planet is in a tension aspect
    //   3. Any retrograde planet in an angular house
    //   4. The strongest tension aspect in the sky
    //   5. Any retrograde planet

    private fun findReversalInfluence(
        card: TarotCard,
        transits: List<PlanetPosition>,
        aspects: List<Aspect>,
    ): Pair<String?, String?> {
        fun aspectStrength(a: Aspect) = a.type.weightBonus * (1.0 - a.orb / a.type.orb)

        card.associatedBody?.name?.let { cardPlanet ->
            // 1. Card's ruling planet is retrograde
            transits.find { it.planet.uppercase() == cardPlanet && it.isRetrograde }
                ?.let { return it.planet to "℞" }

            // 2. Card's ruling planet is in a tension aspect
            aspects.filter { !it.type.isHarmonious }
                .filter { it.planet1.uppercase() == cardPlanet || it.planet2.uppercase() == cardPlanet }
                .maxByOrNull { aspectStrength(it) }
                ?.let { asp ->
                    val other = if (asp.planet1.uppercase() == cardPlanet) asp.planet2 else asp.planet1
                    return other to asp.type.symbol
                }
        }

        // 3. Retrograde planet in an angular house
        transits.filter { it.isRetrograde && it.house in ANGULAR_HOUSES }
            .firstOrNull()?.let { return it.planet to "℞" }

        // 4. Strongest tension aspect overall
        aspects.filter { !it.type.isHarmonious }
            .maxByOrNull { aspectStrength(it) }
            ?.let { return it.planet1 to it.type.symbol }

        // 5. Any retrograde
        transits.firstOrNull { it.isRetrograde }?.let { return it.planet to "℞" }

        return null to null
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
