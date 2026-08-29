package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_shortcuts")
data class AppShortcutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val packageName: String,
    val webUrl: String,
    val iconCategory: String = "web" // video, browser, mail, social, music, custom
)
