package com.example.data.local

import com.example.data.local.dao.JarvisDao
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val dao: JarvisDao) {
    // Memories
    val memories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    suspend fun saveMemory(memory: MemoryEntity) = dao.insertMemory(memory)
    suspend fun deleteMemory(memory: MemoryEntity) = dao.deleteMemory(memory)
    suspend fun clearMemories() = dao.clearAllMemories()

    // Notes
    val notes: Flow<List<NoteEntity>> = dao.getAllNotes()
    suspend fun saveNote(note: NoteEntity) = dao.insertNote(note)
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)

    // Tasks
    val tasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    suspend fun saveTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    // Reminders
    val reminders: Flow<List<ReminderEntity>> = dao.getAllReminders()
    suspend fun saveReminder(reminder: ReminderEntity) = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = dao.deleteReminder(reminder)

    // Custom Commands
    val customCommands: Flow<List<CustomCommandEntity>> = dao.getAllCustomCommands()
    suspend fun getCustomCommandsSync() = dao.getCustomCommandsSync()
    suspend fun saveCustomCommand(command: CustomCommandEntity) = dao.insertCustomCommand(command)
    suspend fun deleteCustomCommand(command: CustomCommandEntity) = dao.deleteCustomCommand(command)

    // Activity Logs
    val logs: Flow<List<ActivityLogEntity>> = dao.getAllLogs()
    suspend fun logActivity(log: ActivityLogEntity) = dao.insertLog(log)
    suspend fun clearLogs() = dao.clearLogs()

    // Shortcuts
    val shortcuts: Flow<List<AppShortcutEntity>> = dao.getAllShortcuts()
    suspend fun getShortcutsSync() = dao.getShortcutsSync()
    suspend fun saveShortcut(shortcut: AppShortcutEntity) = dao.insertShortcut(shortcut)
    suspend fun deleteShortcut(shortcut: AppShortcutEntity) = dao.deleteShortcut(shortcut)

    // API Configs
    val apiConfigs: Flow<List<ApiConfigEntity>> = dao.getAllApiConfigs()
    val activeConfig: Flow<ApiConfigEntity?> = dao.getActiveApiConfig()
    suspend fun getActiveConfigSync() = dao.getActiveApiConfigSync()
    suspend fun saveApiConfig(config: ApiConfigEntity) = dao.insertApiConfig(config)
    suspend fun updateApiConfig(config: ApiConfigEntity) = dao.updateApiConfig(config)
    suspend fun deleteApiConfig(config: ApiConfigEntity) = dao.deleteApiConfig(config)
    suspend fun selectApiConfig(id: Long) = dao.selectApiConfig(id)

    // Chat Messages
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    suspend fun saveChatMessage(message: ChatMessageEntity) = dao.insertChatMessage(message)
    suspend fun clearChat() = dao.clearChatMessages()
}
