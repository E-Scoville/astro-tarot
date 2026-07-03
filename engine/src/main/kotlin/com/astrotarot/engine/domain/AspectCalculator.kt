package com.astrotarot.engine.domain

import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.PlanetPosition
import kotlin.math.abs

object AspectCalculator {

    fun calculate(positions: List<PlanetPosition>): List<Aspect> {
        val aspects = mutableListOf<Aspect>()
        for (i in positions.indices) {
            for (j in i + 1 until positions.size) {
                val p1 = positions[i]
                val p2 = positions[j]
                val sep = angularSeparation(p1.longitude, p2.longitude)
                // Check each type in priority order; take the first (closest) match
                AspectType.entries
                    .map { type -> type to abs(sep - type.angle) }
                    .filter { (type, orb) -> orb <= type.orb }
                    .minByOrNull { (_, orb) -> orb }
                    ?.let { (type, orb) -> aspects.add(Aspect(p1.planet, p2.planet, type, orb)) }
            }
        }
        return aspects.sortedBy { it.orb }  // tightest aspects first
    }

    /** Shortest arc between two ecliptic longitudes (0–180°). */
    fun angularSeparation(lon1: Double, lon2: Double): Double {
        val diff = abs(lon1 - lon2) % 360
        return if (diff > 180) 360 - diff else diff
    }
}
