package com.example.voice

enum class JarvisState(val label: String, val description: String) {
    IDLE("STANDBY", "Neural core online and awaiting voice prompt"),
    LISTENING("LISTENING", "Acoustic sensor active — capture in progress"),
    THINKING("PROCESSING", "Analyzing speech and evaluating intent matrix"),
    SPEAKING("SPEAKING", "Synthesizing vocal response"),
    EXECUTING("EXECUTING", "Dispatching system action"),
    SUCCESS("CONFIRMED", "Action executed successfully"),
    ERROR("WARNING", "Action encountered an error or permission restriction"),
    SLEEP("SLEEP MODE", "Microphone paused. Say 'Jarvis wake up' or tap to engage"),
    PRIVACY_MODE("PRIVACY ACTIVE", "Microphone and telemetry strictly offline")
}

data class ParsedCommand(
    val actionType: ActionType,
    val rawText: String,
    val payload: String = "",
    val secondaryPayload: String = "",
    val confidence: Float = 1.0f,
    val matchedCustomCommandId: Long? = null
)

enum class ActionType {
    TIME,
    DATE,
    OPEN_APP,
    SEARCH_WEB,
    CREATE_NOTE,
    REMEMBER_FACT,
    CREATE_TASK,
    CREATE_REMINDER,
    SYSTEM_NAV,
    STOP_SPEAKING,
    GO_SLEEP,
    WAKE_UP,
    TOGGLE_PRIVACY,
    CHECK_BATTERY,
    CUSTOM_COMMAND,
    ASK_AI,
    UNKNOWN
}
