package com.example.voice

import com.example.data.local.entities.CustomCommandEntity
import java.util.Locale

class CommandParser {

    fun parse(input: String, customCommands: List<CustomCommandEntity> = emptyList()): ParsedCommand {
        val trimmed = input.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // Strip leading wake words
        val cleaned = lower
            .replace(Regex("^(hey\\s+)?jarvis[,\\s]*"), "")
            .replace(Regex("^(ok\\s+)?jarvis[,\\s]*"), "")
            .trim()

        if (cleaned.isEmpty()) {
            return ParsedCommand(ActionType.ASK_AI, trimmed, "Hello! How may I assist you?")
        }

        // 1. Check custom commands first
        for (custom in customCommands) {
            val customPhrase = custom.phrase.trim().lowercase(Locale.ROOT)
            if (cleaned == customPhrase || cleaned.contains(customPhrase)) {
                return ParsedCommand(
                    actionType = ActionType.CUSTOM_COMMAND,
                    rawText = trimmed,
                    payload = custom.targetPayload,
                    secondaryPayload = custom.actionType,
                    matchedCustomCommandId = custom.id
                )
            }
        }

        // 2. Stop speaking / silence
        if (cleaned == "stop speaking" || cleaned == "stop" || cleaned == "be quiet" || cleaned == "shut up" || cleaned == "silence") {
            return ParsedCommand(ActionType.STOP_SPEAKING, trimmed)
        }

        // 3. Sleep / Wake
        if (cleaned == "go to sleep" || cleaned == "sleep" || cleaned == "stand down") {
            return ParsedCommand(ActionType.GO_SLEEP, trimmed)
        }
        if (cleaned == "wake up" || cleaned == "activate" || cleaned == "online") {
            return ParsedCommand(ActionType.WAKE_UP, trimmed)
        }

        // 4. Privacy Mode
        if (cleaned == "privacy mode" || cleaned == "enable privacy" || cleaned == "toggle privacy" || cleaned == "disable privacy") {
            return ParsedCommand(ActionType.TOGGLE_PRIVACY, trimmed)
        }

        // 5. Time & Date
        if (cleaned.contains("what time") || cleaned.contains("current time") || cleaned.contains("tell me the time") || cleaned == "time") {
            return ParsedCommand(ActionType.TIME, trimmed)
        }
        if (cleaned.contains("what is today's date") || cleaned.contains("what is the date") || cleaned.contains("what day is it") || cleaned.contains("today's date") || cleaned == "date") {
            return ParsedCommand(ActionType.DATE, trimmed)
        }

        // 6. Battery / System status
        if (cleaned.contains("battery") || cleaned.contains("power level") || cleaned.contains("system status")) {
            return ParsedCommand(ActionType.CHECK_BATTERY, trimmed)
        }

        // 7. Navigation
        if (cleaned == "go home" || cleaned == "open home" || cleaned == "show dashboard") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "HOME")
        }
        if (cleaned == "open settings" || cleaned == "show settings") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "SETTINGS")
        }
        if (cleaned == "open command center" || cleaned == "open commands") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "COMMANDS")
        }
        if (cleaned == "open ai chat" || cleaned == "open chat") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "CHAT")
        }
        if (cleaned == "open api center" || cleaned == "open api") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "API")
        }
        if (cleaned == "open memory" || cleaned == "open notes" || cleaned == "open tasks" || cleaned == "open reminders") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "PRODUCTIVITY")
        }
        if (cleaned == "open history" || cleaned == "activity log") {
            return ParsedCommand(ActionType.SYSTEM_NAV, trimmed, "HISTORY")
        }

        // 8. Open App / Shortcut
        // e.g. "open youtube", "open google", "launch spotify", "open chrome"
        val openMatch = Regex("^(open|launch|start|goto|go to)\\s+(.+)$").find(cleaned)
        if (openMatch != null) {
            val target = openMatch.groupValues[2].trim()
            return ParsedCommand(ActionType.OPEN_APP, trimmed, target)
        }

        // 9. Search
        // e.g. "search how to make a game", "google javascript tutorial", "search for latest news"
        val searchMatch = Regex("^(search for|search|google|lookup|look up|find)\\s+(.+)$").find(cleaned)
        if (searchMatch != null) {
            val query = searchMatch.groupValues[2].trim()
            return ParsedCommand(ActionType.SEARCH_WEB, trimmed, query)
        }

        // 10. Notes
        // e.g. "create a note buy groceries", "take a note project review tomorrow", "new note meeting notes"
        val noteMatch = Regex("^(create a note|create note|take a note|take note|new note|add note|note)\\s*(that\\s+|about\\s+|:\\s*)?(.*)$").find(cleaned)
        if (noteMatch != null) {
            val noteContent = noteMatch.groupValues[3].trim()
            if (noteContent.isNotEmpty()) {
                return ParsedCommand(ActionType.CREATE_NOTE, trimmed, noteContent)
            }
        }

        // 11. Memory
        // e.g. "remember that I prefer concise answers", "remember my car is in slot B4"
        val memoryMatch = Regex("^(remember that|remember|save memory|store fact)\\s+(.+)$").find(cleaned)
        if (memoryMatch != null) {
            val memoryContent = memoryMatch.groupValues[2].trim()
            return ParsedCommand(ActionType.REMEMBER_FACT, trimmed, memoryContent)
        }

        // 12. Tasks
        // e.g. "create a task finalize presentation", "add task fix layout bug"
        val taskMatch = Regex("^(create a task|create task|add a task|add task|new task|todo)\\s*(that\\s+|:\\s*)?(.*)$").find(cleaned)
        if (taskMatch != null) {
            val taskTitle = taskMatch.groupValues[3].trim()
            if (taskTitle.isNotEmpty()) {
                return ParsedCommand(ActionType.CREATE_TASK, trimmed, taskTitle)
            }
        }

        // 13. Reminders
        // e.g. "create a reminder call John at 5pm", "remind me to take medication"
        val reminderMatch = Regex("^(create a reminder|create reminder|remind me to|set a reminder for|reminder)\\s*(that\\s+|:\\s*)?(.*)$").find(cleaned)
        if (reminderMatch != null) {
            val reminderTitle = reminderMatch.groupValues[3].trim()
            if (reminderTitle.isNotEmpty()) {
                return ParsedCommand(ActionType.CREATE_REMINDER, trimmed, reminderTitle)
            }
        }

        // Default: Treat as AI query or natural language conversation
        return ParsedCommand(ActionType.ASK_AI, trimmed, trimmed)
    }
}
