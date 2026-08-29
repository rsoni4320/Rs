package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiService
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisRepository
import com.example.data.local.entities.*
import com.example.voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisRepository
    val voiceEngine: VoiceEngine
    private val commandParser = CommandParser()
    private val actionExecutor: ActionExecutor
    private val aiService = AiService()

    // Visual & Operational States
    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _currentScreen = MutableStateFlow("HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _productivityTab = MutableStateFlow(0)
    val productivityTab: StateFlow<Int> = _productivityTab.asStateFlow()

    private val _liveSpokenText = MutableStateFlow("")
    val liveSpokenText: StateFlow<String> = _liveSpokenText.asStateFlow()

    private val _lastUnderstoodIntent = MutableStateFlow("")
    val lastUnderstoodIntent: StateFlow<String> = _lastUnderstoodIntent.asStateFlow()

    private val _lastActionSummary = MutableStateFlow("Neural core active. Say 'Jarvis' or tap microphone.")
    val lastActionSummary: StateFlow<String> = _lastActionSummary.asStateFlow()

    private val _lastSpokenResponse = MutableStateFlow("At your service, operative.")
    val lastSpokenResponse: StateFlow<String> = _lastSpokenResponse.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _networkStatus = MutableStateFlow("ONLINE")
    val networkStatus: StateFlow<String> = _networkStatus.asStateFlow()

    private val _testingApiConfigId = MutableStateFlow<Long?>(null)
    val testingApiConfigId: StateFlow<Long?> = _testingApiConfigId.asStateFlow()

    private val _apiTestResult = MutableStateFlow<Pair<Long, String>?>(null)
    val apiTestResult: StateFlow<Pair<Long, String>?> = _apiTestResult.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // User settings
    private val _glowIntensity = MutableStateFlow(1.0f)
    val glowIntensity: StateFlow<Float> = _glowIntensity.asStateFlow()

    private val _reduceMotion = MutableStateFlow(false)
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    // Database Flows
    val memories: StateFlow<List<MemoryEntity>>
    val notes: StateFlow<List<NoteEntity>>
    val tasks: StateFlow<List<TaskEntity>>
    val reminders: StateFlow<List<ReminderEntity>>
    val customCommands: StateFlow<List<CustomCommandEntity>>
    val activityLogs: StateFlow<List<ActivityLogEntity>>
    val shortcuts: StateFlow<List<AppShortcutEntity>>
    val apiConfigs: StateFlow<List<ApiConfigEntity>>
    val activeApiConfig: StateFlow<ApiConfigEntity?>
    val chatMessages: StateFlow<List<ChatMessageEntity>>

    init {
        val db = JarvisDatabase.getDatabase(application)
        repository = JarvisRepository(db.jarvisDao())
        voiceEngine = VoiceEngine(application)
        actionExecutor = ActionExecutor(application, repository)

        memories = repository.memories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        notes = repository.notes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        reminders = repository.reminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        customCommands = repository.customCommands.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        activityLogs = repository.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        shortcuts = repository.shortcuts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        apiConfigs = repository.apiConfigs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        activeApiConfig = repository.activeConfig.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        chatMessages = repository.chatMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Monitor speech speaking state for JARVIS state
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { speaking ->
                if (speaking) {
                    if (_jarvisState.value != JarvisState.PRIVACY_MODE && _jarvisState.value != JarvisState.SLEEP) {
                        _jarvisState.value = JarvisState.SPEAKING
                    }
                } else if (_jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        }

        updateTelemetry()
    }

    fun updateTelemetry() {
        try {
            val context = getApplication<Application>()
            // Battery
            val batteryIntent = context.registerReceiver(null, IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            if (level >= 0 && scale > 0) {
                _batteryLevel.value = (level * 100 / scale)
            }
            _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            // Network
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNet)
            _networkStatus.value = when {
                caps == null -> "OFFLINE"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WI-FI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "5G/LTE"
                else -> "ONLINE"
            }
        } catch (e: Exception) {
            // Ignore telemetry read errors
        }
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun setProductivityTab(index: Int) {
        _productivityTab.value = index
    }

    fun togglePrivacyMode() {
        val newMode = !_isPrivacyMode.value
        _isPrivacyMode.value = newMode
        if (newMode) {
            voiceEngine.stopListening()
            voiceEngine.stopSpeaking()
            _jarvisState.value = JarvisState.PRIVACY_MODE
            _lastActionSummary.value = "Privacy mode active. Sensors and audio input disabled."
            _lastSpokenResponse.value = "Privacy mode engaged."
        } else {
            _jarvisState.value = JarvisState.IDLE
            _lastActionSummary.value = "Neural core online and listening."
            _lastSpokenResponse.value = "Sensors reactivated."
            voiceEngine.speak("Sensors reactivated.")
        }
        viewModelScope.launch {
            repository.logActivity(
                ActivityLogEntity(
                    type = "SYSTEM_EVENT",
                    spokenText = if (newMode) "Privacy mode ON" else "Privacy mode OFF",
                    understoodIntent = "PRIVACY_TOGGLE",
                    resultSummary = if (newMode) "Privacy active" else "Privacy disabled",
                    isSuccess = true
                )
            )
        }
    }

    fun startListening() {
        if (_isPrivacyMode.value) {
            _lastActionSummary.value = "Microphone disabled in Privacy Mode. Disable privacy mode to listen."
            return
        }

        _liveSpokenText.value = ""
        _jarvisState.value = JarvisState.LISTENING

        voiceEngine.startListening(
            onResult = { spokenText ->
                _liveSpokenText.value = spokenText
                processVoiceCommand(spokenText)
            },
            onPartial = { partial ->
                _liveSpokenText.value = partial
            }
        )
    }

    fun stopListening() {
        voiceEngine.stopListening()
        if (_jarvisState.value == JarvisState.LISTENING) {
            _jarvisState.value = JarvisState.IDLE
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
        _jarvisState.value = JarvisState.IDLE
    }

    fun processVoiceCommand(input: String) {
        if (input.isBlank()) {
            _jarvisState.value = JarvisState.IDLE
            return
        }

        viewModelScope.launch {
            _jarvisState.value = JarvisState.THINKING
            _liveSpokenText.value = input

            val customList = repository.getCustomCommandsSync()
            val parsed = commandParser.parse(input, customList)
            _lastUnderstoodIntent.value = parsed.actionType.name

            if (parsed.actionType == ActionType.ASK_AI) {
                executeAiQuestion(input, isVoice = true)
            } else {
                _jarvisState.value = JarvisState.EXECUTING
                val result = actionExecutor.execute(parsed)

                _lastActionSummary.value = result.displaySummary
                _lastSpokenResponse.value = result.spokenResponse

                if (result.navTarget != null) {
                    _currentScreen.value = result.navTarget
                }

                if (result.isSuccess) {
                    _jarvisState.value = JarvisState.SUCCESS
                } else {
                    _jarvisState.value = JarvisState.ERROR
                }

                // Log activity
                repository.logActivity(
                    ActivityLogEntity(
                        type = "VOICE_COMMAND",
                        spokenText = input,
                        understoodIntent = parsed.actionType.name,
                        resultSummary = result.displaySummary,
                        isSuccess = result.isSuccess
                    )
                )

                // Voice Response
                if (result.spokenResponse.isNotBlank() && !_isPrivacyMode.value) {
                    voiceEngine.speak(result.spokenResponse) {
                        _jarvisState.value = JarvisState.IDLE
                    }
                } else {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        }
    }

    fun sendChatMessage(text: String, isVoice: Boolean = false) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Save user message
            repository.saveChatMessage(
                ChatMessageEntity(sender = "USER", content = text, isVoice = isVoice)
            )

            // Check if local command
            val customList = repository.getCustomCommandsSync()
            val parsed = commandParser.parse(text, customList)

            if (parsed.actionType != ActionType.ASK_AI) {
                // Execute local command and output response to chat
                val result = actionExecutor.execute(parsed)
                val reply = if (result.spokenResponse.isNotBlank()) result.spokenResponse else result.displaySummary
                repository.saveChatMessage(
                    ChatMessageEntity(sender = "JARVIS", content = reply, isVoice = isVoice)
                )
                if (isVoice && !_isPrivacyMode.value) {
                    voiceEngine.speak(reply)
                }
            } else {
                executeAiQuestion(text, isVoice = isVoice, recordInChat = true)
            }
        }
    }

    private suspend fun executeAiQuestion(
        prompt: String,
        isVoice: Boolean,
        recordInChat: Boolean = false
    ) {
        _isAiThinking.value = true
        _jarvisState.value = JarvisState.THINKING
        _lastUnderstoodIntent.value = "AI_QUERY"
        _lastActionSummary.value = "Querying neural AI model..."

        val config = repository.getActiveConfigSync()
        val memoryList = repository.memories.firstOrNull()?.map { "${it.category}: ${it.content}" } ?: emptyList()

        val result = aiService.generateResponse(
            config = config,
            prompt = prompt,
            memoryContext = memoryList
        )

        _isAiThinking.value = false

        if (result.isSuccess) {
            val responseText = result.getOrNull() ?: "I have processed your query."
            _lastActionSummary.value = "AI Response generated."
            _lastSpokenResponse.value = responseText
            _jarvisState.value = JarvisState.SUCCESS

            if (recordInChat) {
                repository.saveChatMessage(
                    ChatMessageEntity(sender = "JARVIS", content = responseText, isVoice = isVoice)
                )
            }

            repository.logActivity(
                ActivityLogEntity(
                    type = "AI_RESPONSE",
                    spokenText = prompt,
                    understoodIntent = "AI_CHAT",
                    resultSummary = responseText.take(120),
                    isSuccess = true
                )
            )

            if (isVoice && !_isPrivacyMode.value) {
                voiceEngine.speak(responseText) {
                    _jarvisState.value = JarvisState.IDLE
                }
            } else {
                _jarvisState.value = JarvisState.IDLE
            }
        } else {
            val errorMsg = result.exceptionOrNull()?.message ?: "AI Generation failed"
            val displayErr = if (config == null || config.apiKey.isBlank()) {
                "No active AI provider configured. Please configure OpenRouter, Gemini, OpenAI, or DeepSeek in API Center."
            } else {
                errorMsg
            }
            _lastActionSummary.value = displayErr
            _lastSpokenResponse.value = "I couldn't reach the AI provider. $displayErr"
            _jarvisState.value = JarvisState.ERROR

            if (recordInChat) {
                repository.saveChatMessage(
                    ChatMessageEntity(sender = "JARVIS", content = displayErr, isVoice = isVoice)
                )
            }

            repository.logActivity(
                ActivityLogEntity(
                    type = "ERROR",
                    spokenText = prompt,
                    understoodIntent = "AI_QUERY",
                    resultSummary = displayErr,
                    isSuccess = false
                )
            )

            if (isVoice && !_isPrivacyMode.value) {
                val voiceErr = if (config == null || config.apiKey.isBlank()) {
                    "Please configure an AI provider in API Center."
                } else {
                    "Your selected AI provider returned an error."
                }
                voiceEngine.speak(voiceErr) {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        }
    }

    // Settings actions
    fun setVoicePitch(pitch: Float) {
        voiceEngine.voicePitch = pitch
    }

    fun setVoiceSpeed(speed: Float) {
        voiceEngine.voiceSpeed = speed
    }

    fun setVoiceResponseEnabled(enabled: Boolean) {
        voiceEngine.isVoiceResponseEnabled = enabled
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        voiceEngine.isWakeWordEnabled = enabled
    }

    fun setContinuousListening(enabled: Boolean) {
        voiceEngine.isContinuousListening = enabled
    }

    fun setGlowIntensity(intensity: Float) {
        _glowIntensity.value = intensity
    }

    fun setReduceMotion(reduce: Boolean) {
        _reduceMotion.value = reduce
    }

    // CRUD Methods
    fun saveApiConfig(config: ApiConfigEntity) {
        viewModelScope.launch {
            if (config.id == 0L) {
                repository.saveApiConfig(config)
            } else {
                repository.updateApiConfig(config)
            }
        }
    }

    fun selectApiConfig(id: Long) {
        viewModelScope.launch {
            repository.selectApiConfig(id)
        }
    }

    fun deleteApiConfig(config: ApiConfigEntity) {
        viewModelScope.launch {
            repository.deleteApiConfig(config)
        }
    }

    fun testApiConnection(config: ApiConfigEntity) {
        viewModelScope.launch {
            _testingApiConfigId.value = config.id
            _apiTestResult.value = null
            val result = aiService.testConnection(config)
            _testingApiConfigId.value = null
            if (result.isSuccess) {
                _apiTestResult.value = Pair(config.id, "SUCCESS: Connected successfully.")
                repository.updateApiConfig(config.copy(isConnected = true, lastTestedMs = System.currentTimeMillis()))
            } else {
                val err = result.exceptionOrNull()?.message ?: "Connection test failed."
                _apiTestResult.value = Pair(config.id, "FAILED: $err")
                repository.updateApiConfig(config.copy(isConnected = false, lastTestedMs = System.currentTimeMillis()))
            }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(NoteEntity(title = title, content = content))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addTask(title: String, priority: String = "Normal") {
        viewModelScope.launch {
            repository.saveTask(TaskEntity(title = title, priority = priority))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addReminder(title: String, scheduledTime: String) {
        viewModelScope.launch {
            repository.saveReminder(ReminderEntity(title = title, scheduledTime = scheduledTime))
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun addMemory(category: String, content: String) {
        viewModelScope.launch {
            repository.saveMemory(MemoryEntity(category = category, content = content))
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun addShortcut(name: String, pkg: String, webUrl: String, iconCategory: String) {
        viewModelScope.launch {
            repository.saveShortcut(AppShortcutEntity(name = name, packageName = pkg, webUrl = webUrl, iconCategory = iconCategory))
        }
    }

    fun deleteShortcut(shortcut: AppShortcutEntity) {
        viewModelScope.launch {
            repository.deleteShortcut(shortcut)
        }
    }

    fun addCustomCommand(phrase: String, actionType: String, targetPayload: String, description: String) {
        viewModelScope.launch {
            repository.saveCustomCommand(CustomCommandEntity(phrase = phrase, actionType = actionType, targetPayload = targetPayload, description = description))
        }
    }

    fun deleteCustomCommand(command: CustomCommandEntity) {
        viewModelScope.launch {
            repository.deleteCustomCommand(command)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun resetApplication() {
        viewModelScope.launch {
            repository.clearChat()
            repository.clearLogs()
            repository.clearMemories()
            _lastActionSummary.value = "JARVIS system reset completed."
            _lastSpokenResponse.value = "All temporary caches cleared."
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
