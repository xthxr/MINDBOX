package com.example.mindbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "embeddings",
    indices = [
        Index("entry_id"),
        Index("entry_type")
    ]
)
data class EmbeddingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entry_id")
    val entryId: Long,
    @ColumnInfo(name = "entry_type")
    val entryType: String, // RAW_ENTRY | EVENT | NOTE
    @ColumnInfo(name = "vector", typeAffinity = ColumnInfo.BLOB)
    val vector: ByteArray, // serialized FloatArray
    @ColumnInfo(name = "model_version")
    val modelVersion: String = "mobilebert_v1",
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmbeddingEntity) return false
        return id == other.id && entryId == other.entryId && entryType == other.entryType
    }

    override fun hashCode(): Int = 31 * id.hashCode() + entryId.hashCode()
}
