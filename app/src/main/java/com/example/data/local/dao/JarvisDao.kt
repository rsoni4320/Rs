package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Memories
    @Query("SELECT * FROM memories WHERE isActive = 1 ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    // Notes
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    // Custom Commands
    @Query("SELECT * FROM custom_commands ORDER BY id DESC")
    fun getAllCustomCommands(): Flow<List<CustomCommandEntity>>

    @Query("SELECT * FROM custom_commands")
    suspend fun getCustomCommandsSync(): List<CustomCommandEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCommand(command: CustomCommandEntity): Long

    @Delete
    suspend fun deleteCustomCommand(command: CustomCommandEntity)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()

    // App Shortcuts
    @Query("SELECT * FROM app_shortcuts ORDER BY name ASC")
    fun getAllShortcuts(): Flow<List<AppShortcutEntity>>

    @Query("SELECT * FROM app_shortcuts")
    suspend fun getShortcutsSync(): List<AppShortcutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: AppShortcutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcuts(shortcuts: List<AppShortcutEntity>)

    @Delete
    suspend fun deleteShortcut(shortcut: AppShortcutEntity)

    // API Configs
    @Query("SELECT * FROM api_configs ORDER BY id ASC")
    fun getAllApiConfigs(): Flow<List<ApiConfigEntity>>

    @Query("SELECT * FROM api_configs WHERE isSelected = 1 LIMIT 1")
    fun getActiveApiConfig(): Flow<ApiConfigEntity?>

    @Query("SELECT * FROM api_configs WHERE isSelected = 1 LIMIT 1")
    suspend fun getActiveApiConfigSync(): ApiConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiConfig(config: ApiConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiConfigs(configs: List<ApiConfigEntity>)

    @Update
    suspend fun updateApiConfig(config: ApiConfigEntity)

    @Delete
    suspend fun deleteApiConfig(config: ApiConfigEntity)

    @Query("UPDATE api_configs SET isSelected = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun selectApiConfig(selectedId: Long)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
