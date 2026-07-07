package com.astrotarot

import com.astrotarot.data.FileReadingHistoryStore
import com.astrotarot.data.ReadingRecord
import com.astrotarot.data.SavedCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileReadingHistoryStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun record(savedAt: Long, spreadId: String = "angles") = ReadingRecord(
        savedAt = savedAt,
        timestamp = 1720000000000L,
        lat = 40.2338,
        lon = -111.6585,
        spreadId = spreadId,
        cards = listOf(
            SavedCard("The Magician", 3.25, reversed = false, primaryInfluence = "MERCURY", reversalMarker = null),
            SavedCard("Two of Wands", 1.5, reversed = true, primaryInfluence = "MARS", reversalMarker = "℞"),
        ),
    )

    @Test
    fun `round-trips a record with all fields intact`() {
        val store = FileReadingHistoryStore(tmp.newFile("h.json"))
        store.save(record(savedAt = 42L))

        val loaded = store.load()
        assertEquals(1, loaded.size)
        assertEquals(record(savedAt = 42L), loaded.first())
    }

    @Test
    fun `newest record comes first and history is capped`() {
        val store = FileReadingHistoryStore(tmp.newFile("h.json"), maxEntries = 3)
        for (i in 1L..5L) store.save(record(savedAt = i))

        val loaded = store.load()
        assertEquals(3, loaded.size)
        assertEquals(listOf(5L, 4L, 3L), loaded.map { it.savedAt })
    }

    @Test
    fun `missing file loads as empty history`() {
        val store = FileReadingHistoryStore(tmp.root.resolve("does-not-exist.json"))
        assertTrue(store.load().isEmpty())
    }

    @Test
    fun `corrupt file loads as empty history and can be overwritten`() {
        val file = tmp.newFile("h.json").apply { writeText("{not json[") }
        val store = FileReadingHistoryStore(file)

        assertTrue(store.load().isEmpty())
        store.save(record(savedAt = 7L))
        assertEquals(1, store.load().size)
    }

    @Test
    fun `null influence and marker survive the round trip`() {
        val store = FileReadingHistoryStore(tmp.newFile("h.json"))
        val bare = record(savedAt = 1L).copy(
            cards = listOf(SavedCard("The Fool", 1.0, reversed = false, primaryInfluence = null, reversalMarker = null)),
        )
        store.save(bare)
        assertEquals(bare, store.load().first())
    }
}
