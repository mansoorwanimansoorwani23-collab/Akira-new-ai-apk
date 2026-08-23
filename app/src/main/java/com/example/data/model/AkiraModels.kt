package com.example.data.model

import android.graphics.Bitmap

enum class AkiraState(val label: String, val description: String) {
    DISCONNECTED("Offline", "Tap the power orb to wake Akira"),
    CONNECTING("Connecting", "Establishing neural link to Gemini..."),
    LISTENING("Listening", "Akira is listening to your voice..."),
    THINKING("Thinking", "Akira is crafting a witty thought..."),
    SPEAKING("Speaking", "Akira is responding..."),
    RECONNECTING("Reconnecting", "Restoring Gemini Live stream..."),
    ERROR("Link Interrupted", "Tap to retry connection")
}

data class AkiraVoice(
    val id: String,
    val name: String,
    val characterName: String,
    val personalityDescription: String,
    val geminiVoiceName: String,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val previewQuote: String = "Hey there! Ready to build something crazy together?"
)

data class PersonalitySettings(
    val sassLevel: Int = 3, // 1: Gentle, 2: Playful, 3: Sassy & Witty, 4: Extra Spicy, 5: Ultra Sarcastic
    val wittyBanter: Boolean = true,
    val thinkingLevel: String = "low", // "low", "medium", "high"
    val responseSpeed: String = "Fast",
    val gamingCompanionAutoTips: Boolean = true,
    val lowEndDeviceMode: Boolean = false,
    val voiceId: String = "akira_nova"
)

data class VoiceExchange(
    val id: String = System.currentTimeMillis().toString(),
    val userSpeech: String,
    val akiraSpeech: String,
    val timestamp: Long = System.currentTimeMillis(),
    val state: AkiraState = AkiraState.SPEAKING,
    val toolCall: ToolCallInfo? = null,
    val attachmentSummary: String? = null
)

data class ToolCallInfo(
    val toolName: String,
    val actionSummary: String,
    val url: String? = null,
    val isSuccess: Boolean = true
)

data class MultimodalData(
    val id: String = System.currentTimeMillis().toString(),
    val type: MultimodalType,
    val title: String,
    val mimeType: String,
    val base64Content: String? = null,
    val rawText: String? = null,
    val bitmap: Bitmap? = null
)

enum class MultimodalType {
    IMAGE,
    DOCUMENT,
    SCREEN_SNIP
}

data class GamingCompanionData(
    val isGamingMode: Boolean = false,
    val currentGameTitle: String = "Cyberpunk Odyssey",
    val recentTacticalHint: String = "Watch your flank! Boost shield cooldown with energy cells.",
    val hintsHistory: List<String> = listOf(
        "Akira: Found a secret cache behind the neon waterfall!",
        "Akira: Boss charging ultimate - dodge left in 2 seconds!"
    ),
    val strategicPill: String = "Tactical Audio Companion Active"
)
