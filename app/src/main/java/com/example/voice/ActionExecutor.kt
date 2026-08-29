package com.example.voice

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.util.Log
import com.example.data.local.JarvisRepository
import com.example.data.local.entities.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

data class ActionResult(
    val spokenResponse: String,
    val displaySummary: String,
    val isSuccess: Boolean,
    val navTarget: String? = null
)

class ActionExecutor(
    private val context: Context,
    private val repository: JarvisRepository
) {

    suspend fun execute(command: ParsedCommand): ActionResult {
        return try {
            when (command.actionType) {
                ActionType.TIME -> executeTime()
                ActionType.DATE -> executeDate()
                ActionType.CHECK_BATTERY -> executeBatteryCheck()
                ActionType.SEARCH_WEB -> executeSearch(command.payload)
                ActionType.OPEN_APP -> executeOpenApp(command.payload)
                ActionType.CREATE_NOTE -> executeCreateNote(command.payload)
                ActionType.REMEMBER_FACT -> executeRememberFact(command.payload)
                ActionType.CREATE_TASK -> executeCreateTask(command.payload)
                ActionType.CREATE_REMINDER -> executeCreateReminder(command.payload)
                ActionType.CUSTOM_COMMAND -> executeCustomCommand(command)
                ActionType.STOP_SPEAKING -> ActionResult("Voice output stopped.", "Voice output halted.", true)
                ActionType.GO_SLEEP -> ActionResult("Entering sleep mode. Say 'Jarvis wake up' or tap to engage.", "Sleep mode engaged.", true)
                ActionType.WAKE_UP -> ActionResult("Neural core online. Ready for commands, operative.", "Systems online.", true)
                ActionType.TOGGLE_PRIVACY -> ActionResult("Privacy mode updated.", "Privacy mode toggled.", true)
                ActionType.SYSTEM_NAV -> ActionResult("Navigating to ${command.payload.lowercase()}.", "Navigating to ${command.payload}.", true, navTarget = command.payload)
                ActionType.ASK_AI -> ActionResult("", "", true) // Handled via AiService in ViewModel
                ActionType.UNKNOWN -> ActionResult("Command not recognized. You can ask a question, search the web, or check commands.", "Unknown command.", false)
            }
        } catch (e: Exception) {
            Log.e("ActionExecutor", "Execution error for command: ${command.rawText}", e)
            ActionResult(
                spokenResponse = "I couldn't complete that action due to an unexpected error.",
                displaySummary = "Error: ${e.localizedMessage ?: "Unknown execution failure"}",
                isSuccess = false
            )
        }
    }

    private fun executeTime(): ActionResult {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        val response = "It is $currentTime."
        return ActionResult(spokenResponse = response, displaySummary = "Current time: $currentTime", isSuccess = true)
    }

    private fun executeDate(): ActionResult {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val response = "Today is $currentDate."
        return ActionResult(spokenResponse = response, displaySummary = "Current date: $currentDate", isSuccess = true)
    }

    private fun executeBatteryCheck(): ActionResult {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val chargingStatus = if (isCharging) "and currently charging" else "on battery power"

        val response = "Power cells are at $batteryPct percent, $chargingStatus."
        return ActionResult(
            spokenResponse = response,
            displaySummary = "Battery: $batteryPct% (${if (isCharging) "Charging" else "Discharging"})",
            isSuccess = true
        )
    }

    private fun executeSearch(query: String): ActionResult {
        if (query.isBlank()) {
            return ActionResult("Please specify what you would like me to search.", "Empty search query.", false)
        }

        val encoded = try {
            URLEncoder.encode(query, "UTF-8")
        } catch (e: Exception) {
            query
        }

        val webUrl = "https://www.google.com/search?q=$encoded"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            ActionResult(
                spokenResponse = "Searching for $query.",
                displaySummary = "Searched web: \"$query\"",
                isSuccess = true
            )
        } catch (e: Exception) {
            ActionResult(
                spokenResponse = "Unable to open browser for search.",
                displaySummary = "Search dispatch failed: ${e.message}",
                isSuccess = false
            )
        }
    }

    private suspend fun executeOpenApp(target: String): ActionResult {
        val lowerTarget = target.lowercase(Locale.ROOT).trim()

        // Check custom shortcuts and standard known apps
        val shortcuts = repository.getShortcutsSync()
        val matchedShortcut = shortcuts.find {
            it.name.lowercase(Locale.ROOT) == lowerTarget ||
            it.name.lowercase(Locale.ROOT).contains(lowerTarget) ||
            lowerTarget.contains(it.name.lowercase(Locale.ROOT))
        }

        val packageName = when {
            matchedShortcut != null && matchedShortcut.packageName.isNotBlank() -> matchedShortcut.packageName
            lowerTarget.contains("youtube") -> "com.google.android.youtube"
            lowerTarget.contains("chrome") -> "com.android.chrome"
            lowerTarget.contains("gmail") || lowerTarget.contains("mail") -> "com.google.android.gm"
            lowerTarget.contains("maps") -> "com.google.android.apps.maps"
            lowerTarget.contains("spotify") -> "com.spotify.music"
            lowerTarget.contains("whatsapp") -> "com.whatsapp"
            lowerTarget.contains("facebook") -> "com.facebook.katana"
            lowerTarget.contains("google") -> "com.google.android.googlequicksearchbox"
            else -> null
        }

        val webFallbackUrl = when {
            matchedShortcut != null && matchedShortcut.webUrl.isNotBlank() -> matchedShortcut.webUrl
            lowerTarget.contains("youtube") -> "https://www.youtube.com"
            lowerTarget.contains("chrome") || lowerTarget.contains("browser") || lowerTarget.contains("google") -> "https://www.google.com"
            lowerTarget.contains("gmail") || lowerTarget.contains("mail") -> "https://mail.google.com"
            lowerTarget.contains("maps") -> "https://maps.google.com"
            lowerTarget.contains("spotify") -> "https://open.spotify.com"
            lowerTarget.contains("whatsapp") -> "https://web.whatsapp.com"
            lowerTarget.contains("facebook") -> "https://www.facebook.com"
            lowerTarget.startsWith("http://") || lowerTarget.startsWith("https://") -> lowerTarget
            else -> "https://www.google.com/search?q=${URLEncoder.encode(target, "UTF-8")}"
        }

        // Try launching native app package first if installed
        var launchedNative = false
        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(launchIntent)
                    launchedNative = true
                } catch (e: Exception) {
                    Log.w("ActionExecutor", "Failed to launch package $packageName", e)
                }
            }
        }

        if (launchedNative) {
            val appDisplayName = matchedShortcut?.name ?: target.replaceFirstChar { it.uppercase() }
            return ActionResult(
                spokenResponse = "Opening $appDisplayName.",
                displaySummary = "Launched application: $appDisplayName",
                isSuccess = true
            )
        } else {
            // Fallback to web link as per prompt mandate:
            // "I cannot directly access that application from the current environment, but I can open its supported web link or shortcut."
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallbackUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(webIntent)
                val name = matchedShortcut?.name ?: target.replaceFirstChar { it.uppercase() }
                ActionResult(
                    spokenResponse = "Opening web shortcut for $name.",
                    displaySummary = "Opened web link: $webFallbackUrl",
                    isSuccess = true
                )
            } catch (e: Exception) {
                ActionResult(
                    spokenResponse = "I cannot launch that application or web shortcut on this device.",
                    displaySummary = "Application launch unavailable.",
                    isSuccess = false
                )
            }
        }
    }

    private suspend fun executeCreateNote(content: String): ActionResult {
        val title = if (content.length > 25) content.take(25) + "..." else content
        val note = NoteEntity(title = title, content = content)
        repository.saveNote(note)
        return ActionResult(
            spokenResponse = "I've created your note: $content.",
            displaySummary = "Saved note: \"$content\"",
            isSuccess = true
        )
    }

    private suspend fun executeRememberFact(content: String): ActionResult {
        val memory = MemoryEntity(category = "Preference", content = content)
        repository.saveMemory(memory)
        return ActionResult(
            spokenResponse = "Understood. I've saved that to neural memory.",
            displaySummary = "Stored memory: \"$content\"",
            isSuccess = true
        )
    }

    private suspend fun executeCreateTask(title: String): ActionResult {
        val task = TaskEntity(title = title)
        repository.saveTask(task)
        return ActionResult(
            spokenResponse = "Task added: $title.",
            displaySummary = "Created task: \"$title\"",
            isSuccess = true
        )
    }

    private suspend fun executeCreateReminder(title: String): ActionResult {
        val formattedTime = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())
        val reminder = ReminderEntity(title = title, scheduledTime = formattedTime)
        repository.saveReminder(reminder)
        return ActionResult(
            spokenResponse = "Reminder set for $title.",
            displaySummary = "Created reminder: \"$title\"",
            isSuccess = true
        )
    }

    private suspend fun executeCustomCommand(command: ParsedCommand): ActionResult {
        return when (command.secondaryPayload) {
            "OPEN_URL" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(command.payload)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                ActionResult("Executing shortcut link.", "Opened custom link: ${command.payload}", true)
            }
            "SEARCH" -> executeSearch(command.payload)
            "SAY_TEXT" -> ActionResult(command.payload, "Spoke custom voice payload.", true)
            else -> ActionResult("Executed custom command.", "Custom action dispatched.", true)
        }
    }
}
