package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val scheduledTime: String, // Formatted or human time
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
