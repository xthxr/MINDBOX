package com.example.mindbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "organizations",
    indices = [Index("name")]
)
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "aliases")
    val aliases: String = "[]",
    @ColumnInfo(name = "org_type")
    val orgType: String = "UNKNOWN", // COMPANY | UNIVERSITY | HOSPITAL | GOV | UNKNOWN
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced")
    val synced: Boolean = false
)
