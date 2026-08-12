package com.astrotarot

import com.astrotarot.ui.historyRowText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class HistoryRowTextTest {

    private val zone = ZoneId.of("America/Denver")

    private fun millis(text: String): Long =
        LocalDateTime.parse(text).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `a present-time reading shows only the moment it was cast for`() {
        val row = historyRowText(
            castFor   = millis("2026-08-12T14:30:00"),
            drawnAt   = millis("2026-08-12T14:30:02"),
            cardCount = 3,
            zone      = zone,
        )

        assertEquals("2026-08-12  14:30  ·  3 cards", row.castLine)
        assertNull("same day needs no separate drawn line", row.drawnLine)
    }

    @Test
    fun `a historical reading also shows the day it was drawn`() {
        val row = historyRowText(
            castFor   = millis("1885-03-04T18:30:00"),
            drawnAt   = millis("2026-08-12T09:15:00"),
            cardCount = 3,
            zone      = zone,
        )

        assertEquals("1885-03-04  18:30  ·  3 cards", row.castLine)
        assertEquals("drawn 2026-08-12", row.drawnLine)
    }

    @Test
    fun `a future-dated reading is labelled the same way`() {
        val row = historyRowText(
            castFor   = millis("2030-01-01T00:00:00"),
            drawnAt   = millis("2026-08-12T09:15:00"),
            cardCount = 12,
            zone      = zone,
        )

        assertTrue(row.castLine.startsWith("2030-01-01  00:00"))
        assertEquals("drawn 2026-08-12", row.drawnLine)
    }

    @Test
    fun `same day at a different hour still counts as same day`() {
        val row = historyRowText(
            castFor   = millis("2026-08-12T06:00:00"),
            drawnAt   = millis("2026-08-12T23:45:00"),
            cardCount = 1,
            zone      = zone,
        )

        assertNull(row.drawnLine)
    }

    @Test
    fun `a reading cast just past midnight is dated by the day it belongs to`() {
        // Drawn 23:50, cast for 00:10 the next day: different days, so the drawn
        // line appears and the two dates read distinctly.
        val row = historyRowText(
            castFor   = millis("2026-08-13T00:10:00"),
            drawnAt   = millis("2026-08-12T23:50:00"),
            cardCount = 3,
            zone      = zone,
        )

        assertEquals("2026-08-13  00:10  ·  3 cards", row.castLine)
        assertEquals("drawn 2026-08-12", row.drawnLine)
    }

    @Test
    fun `a single card is not pluralised`() {
        val row = historyRowText(
            castFor   = millis("2026-08-12T14:30:00"),
            drawnAt   = millis("2026-08-12T14:30:00"),
            cardCount = 1,
            zone      = zone,
        )

        assertTrue(row.castLine.endsWith("·  1 card"))
    }

    @Test
    fun `day boundaries are judged in the display zone, not UTC`() {
        // 2026-08-12 20:00 Denver is 2026-08-13 02:00 UTC. Both moments fall on the
        // same Denver day, so no drawn line should appear.
        val row = historyRowText(
            castFor   = millis("2026-08-12T20:00:00"),
            drawnAt   = millis("2026-08-12T21:00:00"),
            cardCount = 3,
            zone      = zone,
        )

        assertNull("same local day despite spanning UTC midnight", row.drawnLine)
    }
}
