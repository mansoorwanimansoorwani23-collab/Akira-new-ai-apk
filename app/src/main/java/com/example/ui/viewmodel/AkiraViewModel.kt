package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AkiraAudioEngine
import com.example.data.model.AkiraState
import com.example.data.model.AkiraVoice
import com.example.data.model.GamingCompanionData
import com.example.data.model.MultimodalData
import com.example.data.model.MultimodalType
import com.example.data.model.PersonalitySettings
import com.example.data.model.ToolCallInfo
import com.example.data.model.VoiceExchange
import com.example.data.repository.AkiraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AkiraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AkiraRepository(application)
    private val audioEngine = AkiraAudioEngine(application, viewModelScope)

    private val _akiraState = MutableStateFlow(AkiraState.DISCONNECTED)
    val akiraState: StateFlow<AkiraState> = _akiraState.asStateFlow()

    val settings: StateFlow<PersonalitySettings> = repository.settings
    val conversationHistory: StateFlow<List<VoiceExchange>> = repository.conversationHistory
    val gamingState: StateFlow<GamingCompanionData> = repository.gamingState

    val isListening: StateFlow<Boolean> = audioEngine.isListening
    val isSpeaking: StateFlow<Boolean> = audioEngine.isSpeaking
    val audioAmplitude: StateFlow<Float> = audioEngine.audioAmplitude
    val liveTranscript: StateFlow<String> = audioEngine.liveTranscript

    private val _currentAttachment = MutableStateFlow<MultimodalData?>(null)
    val currentAttachment: StateFlow<MultimodalData?> = _currentAttachment.asStateFlow()

    private val _activeToolNotification = MutableStateFlow<ToolCallInfo?>(null)
    val activeToolNotification: StateFlow<ToolCallInfo?> = _activeToolNotification.asStateFlow()

    private val _lastSpokenResponse = MutableStateFlow("Hey there! Tap the voice orb and let's talk.")
    val lastSpokenResponse: StateFlow<String> = _lastSpokenResponse.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val availableVoices: List<AkiraVoice> = repository.availableVoices

    private var thinkingTimeoutJob: Job? = null

    init {
        audioEngine.onSpeechRecognized = { transcript ->
            handleUserSpokenInput(transcript)
        }

        audioEngine.onSpeechError = { error ->
            _errorMessage.value = error
            _akiraState.value = AkiraState.ERROR
        }
    }

    fun toggleConnection() {
        when (_akiraState.value) {
            AkiraState.DISCONNECTED, AkiraState.ERROR -> {
                connectToAkira()
            }
            else -> {
                disconnectAkira()
            }
        }
    }

    fun connectToAkira() {
        viewModelScope.launch {
            _akiraState.value = AkiraState.CONNECTING
            _errorMessage.value = null
            audioEngine.playCyberChime()
            delay(400) // Aesthetic neural link connection sequence
            _akiraState.value = AkiraState.LISTENING
            audioEngine.startListening()
        }
    }

    fun disconnectAkira() {
        audioEngine.stopSpeaking()
        audioEngine.stopListening()
        _akiraState.value = AkiraState.DISCONNECTED
        _errorMessage.value = null
    }

    fun onVoiceOrbTapped() {
        when (_akiraState.value) {
            AkiraState.DISCONNECTED -> {
                connectToAkira()
            }
            AkiraState.LISTENING -> {
                // User finished speaking, send what was captured or stop
                audioEngine.stopListening()
            }
            AkiraState.SPEAKING -> {
                // Barge-in: interrupt Akira and listen immediately
                audioEngine.stopSpeaking()
                _akiraState.value = AkiraState.LISTENING
                audioEngine.startListening()
            }
            AkiraState.THINKING -> {
                // Cancel thinking
                thinkingTimeoutJob?.cancel()
                _akiraState.value = AkiraState.LISTENING
                audioEngine.startListening()
            }
            AkiraState.ERROR -> {
                connectToAkira()
            }
            else -> {
                _akiraState.value = AkiraState.LISTENING
                audioEngine.startListening()
            }
        }
    }

    fun handleUserSpokenInput(spokenText: String) {
        if (spokenText.isBlank()) {
            _akiraState.value = AkiraState.LISTENING
            return
        }

        _akiraState.value = AkiraState.THINKING
        _errorMessage.value = null

        thinkingTimeoutJob = viewModelScope.launch(Dispatchers.IO) {
            val attachment = _currentAttachment.value
            val result = repository.processUserSpeech(
                userPrompt = spokenText,
                attachment = attachment
            )

            // Handle tool execution
            result.toolCall?.let { tool ->
                _activeToolNotification.value = tool
                executeLocalToolAction(tool)
            }

            val akiraReply = result.textResponse
            _lastSpokenResponse.value = akiraReply

            // Record exchange in memory
            val exchange = VoiceExchange(
                userSpeech = spokenText,
                akiraSpeech = akiraReply,
                toolCall = result.toolCall,
                attachmentSummary = attachment?.title
            )
            repository.addExchange(exchange)

            // Clear single-turn attachment after processing
            _currentAttachment.value = null

            // Speak response via audio engine
            _akiraState.value = AkiraState.SPEAKING
            val currentVoice = repository.getSelectedVoice()
            audioEngine.speak(akiraReply, currentVoice)
        }
    }

    private fun executeLocalToolAction(tool: ToolCallInfo) {
        viewModelScope.launch(Dispatchers.Main) {
            when (tool.toolName) {
                "openWebsite" -> {
                    tool.url?.let { urlStr ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            getApplication<Application>().startActivity(intent)
                        } catch (e: Exception) {
                            _errorMessage.value = "Unable to launch link: ${e.message}"
                        }
                    }
                }
                "toggleGamingMode" -> {
                    val currentMode = repository.gamingState.value.isGamingMode
                    repository.setGamingMode(!currentMode)
                }
            }

            // Auto dismiss tool notification after 5 seconds
            delay(5000)
            _activeToolNotification.value = null
        }
    }

    fun dismissToolNotification() {
        _activeToolNotification.value = null
    }

    fun attachImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

                    _currentAttachment.value = MultimodalData(
                        type = MultimodalType.IMAGE,
                        title = "Captured Visual",
                        mimeType = "image/jpeg",
                        base64Content = base64,
                        bitmap = bitmap
                    )
                    audioEngine.playCyberChime()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load image: ${e.message}"
            }
        }
    }

    fun attachDocumentText(title: String, content: String) {
        _currentAttachment.value = MultimodalData(
            type = MultimodalType.DOCUMENT,
            title = title,
            mimeType = "text/plain",
            rawText = content
        )
        audioEngine.playCyberChime()
    }

    fun clearAttachment() {
        _currentAttachment.value = null
    }

    fun toggleGamingMode(enabled: Boolean? = null) {
        val current = repository.gamingState.value.isGamingMode
        val next = enabled ?: !current
        repository.setGamingMode(next)
        audioEngine.playCyberChime()
    }

    fun selectGame(title: String) {
        repository.setGamingMode(true, title)
    }

    fun requestTacticalAdvice() {
        handleUserSpokenInput("Akira, analyze my current gaming situation and give me a tactical strategy tip!")
    }

    fun selectVoice(voiceId: String) {
        repository.setVoice(voiceId)
        val selected = repository.getSelectedVoice()
        audioEngine.speak("Voice frequency calibrated to ${selected.name}.", selected)
    }

    fun previewVoice(voice: AkiraVoice) {
        audioEngine.speak(voice.previewQuote, voice)
    }

    fun updatePersonalitySettings(settings: PersonalitySettings) {
        repository.updateSettings(settings)
    }

    fun clearConversationHistory() {
        repository.clearHistory()
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
