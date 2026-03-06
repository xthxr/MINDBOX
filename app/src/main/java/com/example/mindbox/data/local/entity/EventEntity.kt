package com.example.mindbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = RawEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["raw_entry_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("raw_entry_id"),
        Index("date"),
        Index("event_type"),
        Index("org_id")
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "raw_entry_id")
    val rawEntryId: Long? = null,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "event_type")
    val eventType: String = "OTHER",
    @ColumnInfo(name = "date")
    val date: Long? = null,
    @ColumnInfo(name = "date_raw")
    val dateRaw: String? = null,
    @ColumnInfo(name = "org_id")
    val orgId: Long? = null,
    @ColumnInfo(name = "tags")
    val tags: String = "[]", // JSON array
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    @ColumnInfo(name = "people_ids")
    val peopleIds: String = "[]", // JSON array of Long
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced")
    val synced: Boolean = false
)
