package com.example.mindbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = RawEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["raw_entry_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("raw_entry_id")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "raw_entry_id")
    val rawEntryId: Long? = null,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "tags")
    val tags: String = "[]",
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced")
    val synced: Boolean = false
)
