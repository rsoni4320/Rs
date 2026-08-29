package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerActive = false

    // State flows for audio telemetry
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    var voicePitch: Float = 1.05f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    var voiceSpeed: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var isVoiceResponseEnabled: Boolean = true
    var isWakeWordEnabled: Boolean = false
    var isContinuousListening: Boolean = false

    private var onSpeechResultListener: ((String) -> Unit)? = null
    private var onPartialResultListener: ((String) -> Unit)? = null
    private var onTtsDoneListener: (() -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceEngine", "TTS initialization failed", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("VoiceEngine", "US English voice not supported, falling back to default")
                tts?.language = Locale.getDefault()
            }
            tts?.setPitch(voicePitch)
            tts?.setSpeechRate(voiceSpeed)

            // Select female voice if available
            try {
                val voices = tts?.voices
                if (voices != null) {
                    val femaleVoice = voices.find { voice ->
                        voice.name.lowercase(Locale.ROOT).contains("female") ||
                        voice.name.lowercase(Locale.ROOT).contains("en-us-x-sfg") ||
                        voice.name.lowercase(Locale.ROOT).contains("en-us-x-iog")
                    }
                    if (femaleVoice != null) {
                        tts?.voice = femaleVoice
                    }
                }
            } catch (e: Exception) {
                Log.d("VoiceEngine", "Could not customize TTS voice: ${e.message}")
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onTtsDoneListener?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onTtsDoneListener?.invoke()
                }
            })
            isTtsReady = true
        } else {
            isTtsReady = false
            Log.e("VoiceEngine", "TTS Init error code: $status")
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isVoiceResponseEnabled) {
            onDone?.invoke()
            return
        }

        if (!isTtsReady || tts == null) {
            Log.w("VoiceEngine", "TTS not ready yet")
            onDone?.invoke()
            return
        }

        stopSpeaking()
        onTtsDoneListener = onDone
        _isSpeaking.value = true

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_RESPONSE_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_RESPONSE")
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error stopping TTS", e)
        }
        _isSpeaking.value = false
        onTtsDoneListener = null
    }

    fun startListening(
        onResult: (String) -> Unit,
        onPartial: (String) -> Unit = {}
    ) {
        if (isSpeaking.value) {
            stopSpeaking()
        }

        onSpeechResultListener = onResult
        onPartialResultListener = onPartial
        _speechError.value = null

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
                isRecognizerActive = true
                _isListening.value = true
            } catch (e: Exception) {
                Log.e("VoiceEngine", "Error starting speech recognition", e)
                _speechError.value = "Speech recognition start failed: ${e.message}"
                _isListening.value = false
            }
        } else {
            _speechError.value = "Speech recognition service is not available on this device."
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error stopping listening", e)
        }
        _isListening.value = false
        _rmsLevel.value = 0f
        isRecognizerActive = false
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _speechError.value = null
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize rmsdB for wave animation (typical range -2dB to 10dB)
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _rmsLevel.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _rmsLevel.value = 0f
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Speech server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                    else -> "Speech recognition code: $error"
                }

                // If continuous or wake word mode is on and error is no match or timeout, restart gracefully
                if (isContinuousListening && !isSpeaking.value && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    // Ignore silently and restart listener if appropriate
                } else {
                    _speechError.value = message
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _rmsLevel.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    onSpeechResultListener?.invoke(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partialMatches.isNullOrEmpty()) {
                    val partialText = partialMatches[0]
                    onPartialResultListener?.invoke(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error shutting down TTS", e)
        }
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error destroying speech recognizer", e)
        }
    }
}
