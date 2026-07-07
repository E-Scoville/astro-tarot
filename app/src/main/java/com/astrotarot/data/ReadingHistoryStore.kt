package com.astrotarot.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import java.io.File

/** One card as it appeared in a saved reading. */
data class SavedCard(
    val name: String,
    val weight: Double,
    val reversed: Boolean,
    val primaryInfluence: String?,   // CelestialBody name, or null
    val reversalMarker: String?,
)

/**
 * A completed reading, reduced to what cannot be recomputed. Planet positions
 * and aspects are deterministic given (lat, lon, timestamp), so only the drawn
 * cards — the random part — are stored alongside the inputs.
 */
data class ReadingRecord(
    val savedAt: Long,
    val timestamp: Long,
    val lat: Double,
    val lon: Double,
    val spreadId: String,
    val cards: List<SavedCard>,
)

interface ReadingHistoryStore {
    fun load(): List<ReadingRecord>
    fun save(record: ReadingRecord)
}

/**
 * Stores reading history as a JSON array in a single file, newest first,
 * capped at [maxEntries]. A missing or corrupt file reads as empty history.
 */
class FileReadingHistoryStore(
    private val file: File,
    private val maxEntries: Int = 20,
) : ReadingHistoryStore {

    override fun load(): List<ReadingRecord> {
        if (!file.exists()) return emptyList()
        return try {
            Json.parseToJsonElement(file.readText()).jsonArray.map { el ->
                val o = el.jsonObject
                ReadingRecord(
                    savedAt   = o.getValue("savedAt").jsonPrimitive.long,
                    timestamp = o.getValue("timestamp").jsonPrimitive.long,
                    lat       = o.getValue("lat").jsonPrimitive.double,
                    lon       = o.getValue("lon").jsonPrimitive.double,
                    spreadId  = o.getValue("spreadId").jsonPrimitive.content,
                    cards     = o.getValue("cards").jsonArray.map { c ->
                        val card = c.jsonObject
                        SavedCard(
                            name             = card.getValue("name").jsonPrimitive.content,
                            weight           = card.getValue("weight").jsonPrimitive.double,
                            reversed         = card.getValue("reversed").jsonPrimitive.boolean,
                            primaryInfluence = card["primaryInfluence"]?.jsonPrimitive?.takeIf { it.isString }?.content,
                            reversalMarker   = card["reversalMarker"]?.jsonPrimitive?.takeIf { it.isString }?.content,
                        )
                    },
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

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
                })
            }
        }
        file.parentFile?.mkdirs()
        file.writeText(json.toString())
    }
}
