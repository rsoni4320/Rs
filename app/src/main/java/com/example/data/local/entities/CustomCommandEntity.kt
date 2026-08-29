package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_commands")
data class CustomCommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phrase: String, // e.g. "deploy status", "play matrix vibe"
    val actionType: String, // OPEN_URL, SEARCH, SAY_TEXT, SYSTEM
    val targetPayload: String, // e.g. URL, query, or text
    val description: String = ""
)
