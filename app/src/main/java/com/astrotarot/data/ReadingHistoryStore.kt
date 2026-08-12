package com.astrotarot.data

import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.CelestialBody
import com.astrotarot.engine.domain.model.PlanetPosition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** One card as it appeared in a saved reading. */
data class SavedCard(
    val name: String,
    val weight: Double,
    val reversed: Boolean,
    val primaryInfluence: String?,   // CelestialBody name, or null
    val reversalMarker: String?,
)

/** The sky as it stood when the reading was taken. */
data class SavedSky(
    val positions: List<PlanetPosition>,
    val aspects: List<Aspect>,
    val ascendantDegree: Double,
    val midheavenDegree: Double,
)

/** The spread's display text as it read at the time, independent of later edits. */
data class SavedSpread(
    val name: String,
    val positionLabels: List<String>,
)

/**
 * A completed reading, stored whole. Nothing about a past reading is derived on
 * read-back: the cards, the sky they were drawn against, and the labels they were
 * shown under are all recorded, so a saved reading always displays exactly what it
 * displayed the day it was drawn — even after the engine or the spreads change.
 *
 * [sky] and [spread] are null only for records written before they were persisted;
 * those fall back to recomputing from (lat, lon, timestamp).
 */
data class ReadingRecord(
    val savedAt: Long,
    val timestamp: Long,
    val lat: Double,
    val lon: Double,
    val spreadId: String,
    val cards: List<SavedCard>,
    val sky: SavedSky? = null,
    val spread: SavedSpread? = null,
)

interface ReadingHistoryStore {
    fun load(): List<ReadingRecord>
    fun save(record: ReadingRecord)
}

/**
 * Stores reading history as a JSON array in a single file, newest first,
 * capped at [maxEntries].
 *
 * Damage is contained to the smallest unit that can absorb it: an unreadable sky
 * or spread block costs only itself (the reading falls back to recomputation), an
 * unreadable record costs only that reading, and only a file that is not a JSON
 * array at all reads as empty history. Writes are atomic, so an interrupted save
 * leaves the previous history intact rather than a half-written file.
 */
class FileReadingHistoryStore(
    private val file: File,
    private val maxEntries: Int = 20,
) : ReadingHistoryStore {

    override fun load(): List<ReadingRecord> {
        if (!file.exists()) return emptyList()
        val entries = runCatching {
            Json.parseToJsonElement(file.readText()).jsonArray
        }.getOrNull() ?: return emptyList()

        // One malformed entry drops itself and nothing else.
        return entries.mapNotNull { el ->
            runCatching { parseRecord(el.jsonObject) }.getOrNull()
        }
    }

    private fun parseRecord(o: JsonObject): ReadingRecord {
        val cards = o.getValue("cards").jsonArray.map { c ->
            val card = c.jsonObject
            SavedCard(
                name             = card.getValue("name").jsonPrimitive.content,
                weight           = card.getValue("weight").jsonPrimitive.double,
                reversed         = card.getValue("reversed").jsonPrimitive.boolean,
                primaryInfluence = card["primaryInfluence"]?.jsonPrimitive?.takeIf { it.isString }?.content,
                reversalMarker   = card["reversalMarker"]?.jsonPrimitive?.takeIf { it.isString }?.content,
            )
        }
        // A reading with no cards cannot be reopened, so it is not worth keeping.
        check(cards.isNotEmpty()) { "reading record has no cards" }

        return ReadingRecord(
            savedAt   = o.getValue("savedAt").jsonPrimitive.long,
            timestamp = o.getValue("timestamp").jsonPrimitive.long,
            lat       = o.getValue("lat").jsonPrimitive.double,
            lon       = o.getValue("lon").jsonPrimitive.double,
            spreadId  = o.getValue("spreadId").jsonPrimitive.content,
            cards     = cards,
            // A record whose sky or spread block is absent or unreadable falls
            // back to recomputation rather than costing the whole reading.
            sky    = o["sky"]?.let { runCatching { parseSky(it.jsonObject) }.getOrNull() },
            spread = o["spread"]?.let { runCatching { parseSpread(it.jsonObject) }.getOrNull() },
        )
    }

    private fun parseSky(o: JsonObject) = SavedSky(
        positions = o.getValue("positions").jsonArray.map { p ->
            val pos = p.jsonObject
            PlanetPosition(
                planet       = CelestialBody.valueOf(pos.getValue("planet").jsonPrimitive.content),
                sign         = pos.getValue("sign").jsonPrimitive.content,
                longitude    = pos.getValue("longitude").jsonPrimitive.double,
                house        = pos.getValue("house").jsonPrimitive.int,
                isRetrograde = pos.getValue("isRetrograde").jsonPrimitive.boolean,
            )
        },
        aspects = o.getValue("aspects").jsonArray.map { a ->
            val asp = a.jsonObject
            Aspect(
                planet1 = CelestialBody.valueOf(asp.getValue("planet1").jsonPrimitive.content),
                planet2 = CelestialBody.valueOf(asp.getValue("planet2").jsonPrimitive.content),
                type    = AspectType.valueOf(asp.getValue("type").jsonPrimitive.content),
                orb     = asp.getValue("orb").jsonPrimitive.double,
            )
        },
        ascendantDegree = o.getValue("ascendantDegree").jsonPrimitive.double,
        midheavenDegree = o.getValue("midheavenDegree").jsonPrimitive.double,
    )

    private fun parseSpread(o: JsonObject) = SavedSpread(
        name           = o.getValue("name").jsonPrimitive.content,
        positionLabels = o.getValue("positionLabels").jsonArray.map { it.jsonPrimitive.content },
    )

    override fun save(record: ReadingRecord) {
        val updated = (listOf(record) + load()).take(maxEntries)
        val json = buildJsonArray {
            for (r in updated) {
                add(buildJsonObject {
                    put("savedAt", r.savedAt)
                    put("timestamp", r.timestamp)
                    put("lat", r.lat)
                    put("lon", r.lon)
                    put("spreadId", r.spreadId)
                    put("cards", buildJsonArray {
                        for (c in r.cards) {
                            add(buildJsonObject {
                                put("name", c.name)
                                put("weight", c.weight)
                                put("reversed", c.reversed)
                                c.primaryInfluence?.let { put("primaryInfluence", it) }
                                c.reversalMarker?.let { put("reversalMarker", it) }
                            })
                        }
                    })
                    r.sky?.let { sky ->
                        put("sky", buildJsonObject {
                            put("positions", buildJsonArray {
                                for (p in sky.positions) add(buildJsonObject {
                                    put("planet", p.planet.name)
                                    put("sign", p.sign)
                                    put("longitude", p.longitude)
                                    put("house", p.house)
                                    put("isRetrograde", p.isRetrograde)
                                })
                            })
                            put("aspects", buildJsonArray {
                                for (a in sky.aspects) add(buildJsonObject {
                                    put("planet1", a.planet1.name)
                                    put("planet2", a.planet2.name)
                                    put("type", a.type.name)
                                    put("orb", a.orb)
                                })
                            })
                            put("ascendantDegree", sky.ascendantDegree)
                            put("midheavenDegree", sky.midheavenDegree)
                        })
                    }
                    r.spread?.let { s ->
                        put("spread", buildJsonObject {
                            put("name", s.name)
                            put("positionLabels", buildJsonArray { for (l in s.positionLabels) add(l) })
                        })
                    }
                })
            }
        }
        file.parentFile?.mkdirs()
        writeAtomically(json.toString())
    }

    /**
     * Writes via a sibling temp file and a rename, so a save interrupted midway
     * cannot leave a truncated history behind. Falls back to a direct write if the
     * filesystem will not do an atomic move.
     */
    private fun writeAtomically(text: String) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        try {
            tmp.writeText(text)
            try {
                Files.move(tmp.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            tmp.delete()
        }
    }
}
