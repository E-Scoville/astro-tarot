package com.astrotarot.engine.domain

import com.astrotarot.engine.domain.model.*
import kotlin.random.Random

class TarotAstrologyEngine(private val deck: List<TarotCard>) {

    fun generateWeightedReading(
        transits: List<PlanetPosition>,
        cardsToDraw: Int,
        random: Random = Random.Default
    ): List<WeightedCard> {
        val weights = deck.map { card -> card to calculateWeight(card, transits) }
        val avgWeight = weights.map { it.second }.average()
        val drawn = weightedRandomSample(weights, cardsToDraw, random)
        return drawn.map { (card, weight) ->
            WeightedCard(card, weight, reversed = weight < avgWeight)
        }
    }

    private fun calculateWeight(card: TarotCard, transits: List<PlanetPosition>): Double {
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
                        // Pages: no longitude range, activate on elemental match
                        weight += 0.5
                    }
                }
            }
        }

        return weight
    }

    private fun inLongitudeRange(longitude: Double, start: Double, end: Double): Boolean =
        if (start <= end) longitude >= start && longitude < end
        else longitude >= start || longitude < end  // wraps around 0° (e.g., 350°–20°)

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
