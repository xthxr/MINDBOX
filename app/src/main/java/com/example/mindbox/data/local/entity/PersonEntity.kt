package com.example.mindbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "people",
    indices = [Index("name")]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "aliases")
    val aliases: String = "[]", // JSON array of strings
    @ColumnInfo(name = "linked_event_ids")
    val linkedEventIds: String = "[]", // JSON array of Long
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced")
    val synced: Boolean = false
)
