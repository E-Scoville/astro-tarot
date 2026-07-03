package com.astrotarot.engine.data

import com.astrotarot.engine.domain.model.ArcanaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FullDeckTest {

    @Test
    fun `deck has exactly 78 cards`() {
        assertEquals(78, FULL_DECK.size)
    }

    @Test
    fun `arcana type counts match expectations`() {
        val counts = FULL_DECK.groupingBy { it.type }.eachCount()
        assertEquals(22, counts[ArcanaType.MAJOR] ?: 0)
        // 40 = 4 aces + 36 numbered (2-10 x 4 suits)
        val numberedAndAces = (counts[ArcanaType.MINOR_NUMBERED] ?: 0) + (counts[ArcanaType.MINOR_ACE] ?: 0)
        assertEquals(40, numberedAndAces)
        assertEquals(16, counts[ArcanaType.MINOR_COURT] ?: 0)
    }

    @Test
    fun `all card names are unique`() {
        val names = FULL_DECK.map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `minor numbered cards have well-formed longitude ranges`() {
        val numbered = FULL_DECK.filter { it.type == ArcanaType.MINOR_NUMBERED }
        assertTrue("expected some MINOR_NUMBERED cards", numbered.isNotEmpty())
        for (card in numbered) {
            val start = card.longitudeRangeStart
            val end = card.longitudeRangeEnd
            assertTrue("${card.name} missing longitude range", start != null && end != null)
            assertTrue("${card.name} start out of range: $start", start!! >= 0.0 && start < 360.0)
            assertTrue("${card.name} end out of range: $end", end!! >= 0.0 && end <= 360.0)
            assertTrue("${card.name} degenerate range ($start, $end)", start != end)
        }
    }

    @Test
    fun `every major card has an associatedBody or associatedSign`() {
        val majors = FULL_DECK.filter { it.type == ArcanaType.MAJOR }
        assertEquals(22, majors.size)
        val missing = majors.filter { it.associatedBody == null && it.associatedSign == null }
        assertTrue(
            "Majors missing both associatedBody and associatedSign: ${missing.map { it.name }}",
            missing.isEmpty()
        )
    }
}
