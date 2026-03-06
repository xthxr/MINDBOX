package com.example.mindbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_entries")
data class RawEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "raw_text")
    val rawText: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_processed")
    val isProcessed: Boolean = false,
    @ColumnInfo(name = "input_source")
    val inputSource: String = "TEXT", // TEXT | VOICE
    @ColumnInfo(name = "synced")
    val synced: Boolean = false
)
