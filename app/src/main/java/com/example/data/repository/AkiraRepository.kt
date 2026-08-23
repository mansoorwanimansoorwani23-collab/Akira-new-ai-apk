package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.gemini.GeminiClient
import com.example.data.model.AkiraVoice
import com.example.data.model.GamingCompanionData
import com.example.data.model.MultimodalData
import com.example.data.model.PersonalitySettings
import com.example.data.model.VoiceExchange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AkiraRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("akira_ai_prefs", Context.MODE_PRIVATE)

    private val geminiClient = GeminiClient(context)

    val availableVoices = listOf(
        AkiraVoice(
            id = "akira_aura",
            name = "Akira Aura",
            characterName = "Aura",
            personalityDescription = "Warm, composed, and soothingly intelligent.",
            geminiVoiceName = "Aoede",
            pitch = 1.02f,
            speed = 0.95f,
            previewQuote = "Take a breath. I'm right here whenever you need me."
        ),
        AkiraVoice(
            id = "akira_nova",
            name = "Akira Nova",
            characterName = "Nova",
            personalityDescription = "Confident, sharp-witted, and delightfully sassy.",
            geminiVoiceName = "Kore",
            pitch = 1.15f,
            speed = 1.05f,
            previewQuote = "Hey there! Ready to build something crazy together?"
        ),
        AkiraVoice(
            id = "akira_luna",
            name = "Akira Luna",
            characterName = "Luna",
            personalityDescription = "Soft, gentle, and emotionally empathetic.",
            geminiVoiceName = "Zephyr",
            pitch = 1.25f,
            speed = 0.98f,
            previewQuote = "You've got this! I'm here to back you up every step of the way."
        ),
        AkiraVoice(
            id = "akira_pulse",
            name = "Akira Pulse",
            characterName = "Pulse",
            personalityDescription = "Energetic, playful, and full of spirited banter.",
            geminiVoiceName = "Puck",
            pitch = 1.10f,
            speed = 1.15f,
            previewQuote = "Let's go! Time to show the world what we're made of!"
        ),
        AkiraVoice(
            id = "akira_velvet",
            name = "Akira Velvet",
            characterName = "Velvet",
            personalityDescription = "Smooth, deep, elegant, and effortlessly cool.",
            geminiVoiceName = "Charon",
            pitch = 0.92f,
            speed = 0.95f,
            previewQuote = "Calm precision. Let's solve this effortlessly."
        )
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<PersonalitySettings> = _settings.asStateFlow()

    private val _conversationHistory = MutableStateFlow<List<VoiceExchange>>(emptyList())
    val conversationHistory: StateFlow<List<VoiceExchange>> = _conversationHistory.asStateFlow()

    private val _gamingState = MutableStateFlow(GamingCompanionData())
    val gamingState: StateFlow<GamingCompanionData> = _gamingState.asStateFlow()

    private fun loadSettings(): PersonalitySettings {
        return PersonalitySettings(
            sassLevel = prefs.getInt("sass_level", 3),
            wittyBanter = prefs.getBoolean("witty_banter", true),
            thinkingLevel = prefs.getString("thinking_level", "low") ?: "low",
            responseSpeed = prefs.getString("response_speed", "Fast") ?: "Fast",
            gamingCompanionAutoTips = prefs.getBoolean("gaming_auto_tips", true),
            lowEndDeviceMode = prefs.getBoolean("low_end_mode", false),
            voiceId = prefs.getString("voice_id", "akira_nova") ?: "akira_nova"
        )
    }

    fun updateSettings(newSettings: PersonalitySettings) {
        _settings.value = newSettings
        prefs.edit()
            .putInt("sass_level", newSettings.sassLevel)
            .putBoolean("witty_banter", newSettings.wittyBanter)
            .putString("thinking_level", newSettings.thinkingLevel)
            .putString("response_speed", newSettings.responseSpeed)
            .putBoolean("gaming_auto_tips", newSettings.gamingCompanionAutoTips)
            .putBoolean("low_end_mode", newSettings.lowEndDeviceMode)
            .putString("voice_id", newSettings.voiceId)
            .apply()
    }

    fun getSelectedVoice(): AkiraVoice {
        val voiceId = _settings.value.voiceId
        return availableVoices.firstOrNull { it.id == voiceId } ?: availableVoices[1]
    }

    fun setVoice(voiceId: String) {
        val current = _settings.value
        updateSettings(current.copy(voiceId = voiceId))
    }

    fun addExchange(exchange: VoiceExchange) {
        _conversationHistory.value = (_conversationHistory.value + exchange).takeLast(30)
    }

    fun clearHistory() {
        _conversationHistory.value = emptyList()
    }

    fun setGamingMode(enabled: Boolean, gameTitle: String? = null) {
        _gamingState.value = _gamingState.value.copy(
            isGamingMode = enabled,
            currentGameTitle = gameTitle ?: _gamingState.value.currentGameTitle
        )
    }

    fun addGamingHint(hint: String) {
        val current = _gamingState.value
        _gamingState.value = current.copy(
            recentTacticalHint = hint,
            hintsHistory = (listOf("Akira: $hint") + current.hintsHistory).take(10)
        )
    }

    suspend fun processUserSpeech(
        userPrompt: String,
        attachment: MultimodalData? = null
    ): GeminiClient.AkiraGenerationResult {
        val historyPairs = _conversationHistory.value.map { it.userSpeech to it.akiraSpeech }
        val currentVoice = getSelectedVoice()

        return geminiClient.converseWithAkira(
            userPrompt = userPrompt,
            conversationHistory = historyPairs,
            attachment = attachment,
            settings = _settings.value,
            isGamingMode = _gamingState.value.isGamingMode,
            geminiVoiceName = currentVoice.geminiVoiceName
        )
    }
}
