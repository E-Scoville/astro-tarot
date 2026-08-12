package com.astrotarot.ui

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CAST_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm")
private val DRAWN_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/** The one or two subtitle lines shown under a saved reading's spread name. */
data class HistoryRowText(
    /** The moment the reading was cast for, plus the card count. */
    val castLine: String,
    /** When the reading was actually drawn; null when that was the same day. */
    val drawnLine: String?,
)

/**
 * Describes a saved reading in the history list.
 *
 * The moment a reading was cast for is its identity — it names the sky the cards
 * were drawn against — so it leads. When the reading was drawn is a separate fact,
 * and only worth stating when it differs: choosing a historical or future date
 * detaches the two, and without this the list would date a reading centuries from
 * the day you actually sat down with it. For an ordinary present-time reading the
 * two fall on the same day and the second line is dropped rather than repeated.
 */
fun historyRowText(
    castFor: Long,
    drawnAt: Long,
    cardCount: Int,
    zone: ZoneId,
): HistoryRowText {
    val cast  = LocalDateTime.ofInstant(Instant.ofEpochMilli(castFor), zone)
    val drawn = LocalDateTime.ofInstant(Instant.ofEpochMilli(drawnAt), zone)

    val cards = "$cardCount card" + if (cardCount == 1) "" else "s"
    return HistoryRowText(
        castLine  = "${cast.format(CAST_FMT)}  ·  $cards",
        drawnLine = if (cast.toLocalDate() == drawn.toLocalDate()) null
                    else "drawn ${drawn.format(DRAWN_FMT)}",
    )
}
