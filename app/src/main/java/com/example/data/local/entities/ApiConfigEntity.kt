package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val provider: String, // GEMINI, OPENROUTER, OPENAI, DEEPSEEK, CUSTOM
    val name: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String = "",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024,
    val isSelected: Boolean = false,
    val isConnected: Boolean = false,
    val lastTestedMs: Long = 0
)
