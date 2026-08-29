package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.JarvisDao
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemoryEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        ReminderEntity::class,
        CustomCommandEntity::class,
        ActivityLogEntity::class,
        AppShortcutEntity::class,
        ApiConfigEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database.jarvisDao())
                    }
                }
            }

            private suspend fun seedInitialData(dao: JarvisDao) {
                // Seed default shortcuts
                val defaultShortcuts = listOf(
                    AppShortcutEntity(name = "YouTube", packageName = "com.google.android.youtube", webUrl = "https://www.youtube.com", iconCategory = "video"),
                    AppShortcutEntity(name = "Google", packageName = "com.google.android.googlequicksearchbox", webUrl = "https://www.google.com", iconCategory = "browser"),
                    AppShortcutEntity(name = "Gmail", packageName = "com.google.android.gm", webUrl = "https://mail.google.com", iconCategory = "mail"),
                    AppShortcutEntity(name = "Chrome", packageName = "com.android.chrome", webUrl = "https://www.google.com", iconCategory = "browser"),
                    AppShortcutEntity(name = "WhatsApp", packageName = "com.whatsapp", webUrl = "https://web.whatsapp.com", iconCategory = "social"),
                    AppShortcutEntity(name = "Spotify", packageName = "com.spotify.music", webUrl = "https://open.spotify.com", iconCategory = "music"),
                    AppShortcutEntity(name = "Facebook", packageName = "com.facebook.katana", webUrl = "https://www.facebook.com", iconCategory = "social")
                )
                dao.insertShortcuts(defaultShortcuts)

                // Seed default provider profiles
                val defaultConfigs = listOf(
                    ApiConfigEntity(
                        provider = "GEMINI",
                        name = "Google Gemini",
                        apiKey = "",
                        model = "gemini-2.5-flash",
                        baseUrl = "https://generativelanguage.googleapis.com",
                        temperature = 0.7f,
                        maxTokens = 1024,
                        isSelected = true
                    ),
                    ApiConfigEntity(
                        provider = "OPENROUTER",
                        name = "OpenRouter",
                        apiKey = "",
                        model = "google/gemini-2.5-flash",
                        baseUrl = "https://openrouter.ai/api/v1",
                        temperature = 0.7f,
                        maxTokens = 1024,
                        isSelected = false
                    ),
                    ApiConfigEntity(
                        provider = "OPENAI",
                        name = "OpenAI",
                        apiKey = "",
                        model = "gpt-4o-mini",
                        baseUrl = "https://api.openai.com/v1",
                        temperature = 0.7f,
                        maxTokens = 1024,
                        isSelected = false
                    ),
                    ApiConfigEntity(
                        provider = "DEEPSEEK",
                        name = "DeepSeek",
                        apiKey = "",
                        model = "deepseek-chat",
                        baseUrl = "https://api.deepseek.com/v1",
                        temperature = 0.7f,
                        maxTokens = 1024,
                        isSelected = false
                    ),
                    ApiConfigEntity(
                        provider = "CUSTOM",
                        name = "Custom Compatible API",
                        apiKey = "",
                        model = "custom-model",
                        baseUrl = "https://api.openai.com/v1",
                        temperature = 0.7f,
                        maxTokens = 1024,
                        isSelected = false
                    )
                )
                dao.insertApiConfigs(defaultConfigs)

                // Seed initial custom commands
                dao.insertCustomCommand(
                    CustomCommandEntity(
                        phrase = "check tech news",
                        actionType = "SEARCH",
                        targetPayload = "latest artificial intelligence and technology news",
                        description = "Searches web for today's breakthrough tech news"
                    )
                )
                dao.insertCustomCommand(
                    CustomCommandEntity(
                        phrase = "matrix vibe",
                        actionType = "SAY_TEXT",
                        targetPayload = "All systems nominal, operative. Welcome to the neural core.",
                        description = "Voice status affirmation"
                    )
                )

                // Initial welcome activity log
                dao.insertLog(
                    ActivityLogEntity(
                        type = "SYSTEM_EVENT",
                        spokenText = "System initialization",
                        understoodIntent = "INIT",
                        resultSummary = "J.A.R.V.I.S. neural voice interface initialized successfully.",
                        isSuccess = true
                    )
                )
            }
        }
    }
}
