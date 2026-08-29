package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String = "Preference", // Preference, Fact, Context, Custom
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
