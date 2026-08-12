package com.astrotarot

import com.astrotarot.data.FileReadingHistoryStore
import com.astrotarot.data.ReadingRecord
import com.astrotarot.data.SavedCard
import com.astrotarot.data.SavedSky
import com.astrotarot.data.SavedSpread
import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.PlanetPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        sky = SavedSky(
            positions = listOf(
                PlanetPosition(CelestialBody.SUN, "ARIES", 5.5, 1, false),
                PlanetPosition(CelestialBody.MERCURY, "GEMINI", 65.25, 3, true),
            ),
            aspects = listOf(
                Aspect(CelestialBody.SUN, CelestialBody.MERCURY, AspectType.SEXTILE, 1.75),
            ),
            ascendantDegree = 123.5,
            midheavenDegree = 33.25,
        ),
        spread = SavedSpread("The Three Angles", listOf("I — Ascendant", "II — Midheaven", "III — Root")),
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

    @Test
    fun `a record written before the sky was persisted still loads`() {
        // Exactly the shape the old store wrote: no "sky" or "spread" keys.
        val legacy = """
            [{"savedAt":9,"timestamp":1720000000000,"lat":40.2338,"lon":-111.6585,
              "spreadId":"angles",
              "cards":[{"name":"The Fool","weight":2.0,"reversed":false}]}]
        """.trimIndent()
        val store = FileReadingHistoryStore(tmp.newFile("h.json").apply { writeText(legacy) })

        val loaded = store.load()
        assertEquals(1, loaded.size)
        assertEquals(9L, loaded.first().savedAt)
        assertNull("legacy record has no stored sky", loaded.first().sky)
        assertNull("legacy record has no stored spread", loaded.first().spread)
        assertEquals("The Fool", loaded.first().cards.single().name)
    }

    @Test
    fun `an unreadable sky block degrades to null without losing the record`() {
        val damaged = """
            [{"savedAt":3,"timestamp":1720000000000,"lat":1.0,"lon":2.0,
              "spreadId":"angles",
              "cards":[{"name":"The Fool","weight":2.0,"reversed":false}],
              "sky":{"positions":[{"planet":"NOT_A_PLANET","sign":"ARIES","longitude":1.0,
                     "house":1,"isRetrograde":false}],"aspects":[],
                     "ascendantDegree":0.0,"midheavenDegree":0.0}}]
        """.trimIndent()
        val store = FileReadingHistoryStore(tmp.newFile("h.json").apply { writeText(damaged) })

        val loaded = store.load()
        assertEquals(1, loaded.size)
        assertNull(loaded.first().sky)
        assertEquals("The Fool", loaded.first().cards.single().name)
    }

    @Test
    fun `one malformed record is dropped and its siblings survive`() {
        // Middle entry is missing the required "lat" field.
        val mixed = """
            [{"savedAt":3,"timestamp":100,"lat":1.0,"lon":2.0,"spreadId":"angles",
              "cards":[{"name":"The Fool","weight":2.0,"reversed":false}]},
             {"savedAt":2,"timestamp":100,"lon":2.0,"spreadId":"angles",
              "cards":[{"name":"The Magician","weight":2.0,"reversed":false}]},
             {"savedAt":1,"timestamp":100,"lat":1.0,"lon":2.0,"spreadId":"angles",
              "cards":[{"name":"The Empress","weight":2.0,"reversed":true}]}]
        """.trimIndent()
        val store = FileReadingHistoryStore(tmp.newFile("h.json").apply { writeText(mixed) })

        val loaded = store.load()
        assertEquals(listOf(3L, 1L), loaded.map { it.savedAt })
    }

    @Test
    fun `a record with an unreadable card is dropped rather than silently shortened`() {
        // Second entry's card is missing "weight"; a partial reading would misrepresent
        // what was actually drawn, so the whole record goes.
        val mixed = """
            [{"savedAt":2,"timestamp":100,"lat":1.0,"lon":2.0,"spreadId":"angles",
              "cards":[{"name":"The Fool","weight":2.0,"reversed":false},
                       {"name":"The Star","reversed":false}]},
             {"savedAt":1,"timestamp":100,"lat":1.0,"lon":2.0,"spreadId":"angles",
              "cards":[{"name":"The Empress","weight":2.0,"reversed":true}]}]
        """.trimIndent()
        val store = FileReadingHistoryStore(tmp.newFile("h.json").apply { writeText(mixed) })

        val loaded = store.load()
        assertEquals(listOf(1L), loaded.map { it.savedAt })
    }

    @Test
    fun `a record with no cards is dropped`() {
        val empty = """
            [{"savedAt":1,"timestamp":100,"lat":1.0,"lon":2.0,"spreadId":"angles","cards":[]}]
        """.trimIndent()
        val store = FileReadingHistoryStore(tmp.newFile("h.json").apply { writeText(empty) })

        assertTrue(store.load().isEmpty())
    }

    @Test
    fun `saving after a bad record prunes it and keeps the good ones`() {
        val mixed = """
            [{"savedAt":3,"timestamp":100,"lat":1.0,"lon":2.0,"spreadId":"angles",
              "cards":[{"name":"The Fool","weight":2.0,"reversed":false}]},
             {"savedAt":2,"timestamp":100,"spreadId":"angles","cards":[]}]
        """.trimIndent()
        val store = FileReadingHistoryStore(tmp.newFile("h.json").apply { writeText(mixed) })

        store.save(record(savedAt = 4L))

        val loaded = store.load()
        assertEquals(listOf(4L, 3L), loaded.map { it.savedAt })
    }

    @Test
    fun `an interrupted save leaves no stray temp file behind`() {
        val file = tmp.newFile("h.json")
        val store = FileReadingHistoryStore(file)
        store.save(record(savedAt = 1L))

        assertTrue(tmp.root.listFiles()!!.none { it.name.endsWith(".tmp") })
        assertEquals(1, store.load().size)
    }

    @Test
    fun `sky and spread survive a save and load cycle unchanged`() {
        val store = FileReadingHistoryStore(tmp.newFile("h.json"))
        val original = record(savedAt = 11L)
        store.save(original)

        val loaded = store.load().first()
        assertEquals(original.sky, loaded.sky)
        assertEquals(original.spread, loaded.spread)
    }
}
