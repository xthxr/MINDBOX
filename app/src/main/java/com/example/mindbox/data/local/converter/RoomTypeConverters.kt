package com.example.mindbox.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer

class RoomTypeConverters {

    private val json = Json { ignoreUnknownKeys = true }


    // ---- String List (JSON) ----
    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())

    // ---- Long List (JSON) ----
    @TypeConverter
    fun fromLongList(value: List<Long>): String = json.encodeToString(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        runCatching { json.decodeFromString<List<Long>>(value) }.getOrDefault(emptyList())

    // ---- FloatArray <-> ByteArray ----
    @TypeConverter
    fun fromFloatArray(value: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(value.size * 4)
        value.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    @TypeConverter
    fun toFloatArray(value: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(value)
        return FloatArray(value.size / 4) { buffer.getFloat() }
    }
}
