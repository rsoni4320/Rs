package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // VOICE_COMMAND, ACTION_EXECUTION, AI_RESPONSE, SYSTEM_EVENT, ERROR
    val spokenText: String = "",
    val understoodIntent: String = "",
    val resultSummary: String = "",
    val isSuccess: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
