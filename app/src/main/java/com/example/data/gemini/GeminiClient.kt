package com.example.data.gemini

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.MultimodalData
import com.example.data.model.PersonalitySettings
import com.example.data.model.ToolCallInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

class GeminiClient(private val context: Context) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    data class AkiraGenerationResult(
        val textResponse: String,
        val toolCall: ToolCallInfo? = null,
        val audioData: String? = null,
        val isSuccess: Boolean = true
    )

    suspend fun converseWithAkira(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>>, // Pair of (userText, akiraText)
        attachment: MultimodalData? = null,
        settings: PersonalitySettings = PersonalitySettings(),
        isGamingMode: Boolean = false,
        geminiVoiceName: String = "Aoede"
    ): AkiraGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }

        val systemPrompt = AkiraPersonality.buildSystemPrompt(settings, isGamingMode)

        val contentsList = mutableListOf<GeminiContent>()

        // Add previous dialogue history context
        conversationHistory.takeLast(4).forEach { (userTurn, akiraTurn) ->
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userTurn))
                )
            )
            contentsList.add(
                GeminiContent(
                    role = "model",
                    parts = listOf(GeminiPart(text = akiraTurn))
                )
            )
        }

        // Current user turn with optional multimodal payload
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = userPrompt.ifBlank { "Hey Akira, check this out!" }))

        if (attachment != null && !attachment.base64Content.isNullOrBlank()) {
            currentParts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = attachment.mimeType,
                        data = attachment.base64Content
                    )
                )
            )
        } else if (attachment != null && !attachment.rawText.isNullOrBlank()) {
            currentParts.add(
                GeminiPart(
                    text = "[Attached Document Content (${attachment.title})]:\n${attachment.rawText}"
                )
            )
        }

        contentsList.add(
            GeminiContent(
                role = "user",
                parts = currentParts
            )
        )

        val generationConfig = GeminiGenerationConfig(
            temperature = 0.85f,
            topP = 0.95f,
            topK = 40,
            maxOutputTokens = 500,
            responseModalities = listOf("TEXT"),
            speechConfig = GeminiSpeechConfig(
                voiceConfig = GeminiVoiceConfig(
                    prebuiltVoiceConfig = GeminiPrebuiltVoiceConfig(voiceName = geminiVoiceName)
                )
            )
        )

        val request = GeminiGenerateRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = generationConfig,
            tools = AkiraPersonality.TOOLS_DECLARATION
        )

        try {
            // Priority: gemini-3.5-flash for real-time multimodal & conversational intelligence
            val modelName = "gemini-3.5-flash"
            val response = api.generateContent(model = modelName, apiKey = apiKey, request = request)

            val candidate = response.candidates?.firstOrNull()
            val candidateContent = candidate?.content

            var responseText = ""
            var detectedToolCall: ToolCallInfo? = null

            candidateContent?.parts?.forEach { part ->
                if (!part.text.isNullOrBlank()) {
                    responseText += part.text + " "
                }
                if (part.functionCall != null) {
                    val fnName = part.functionCall.name
                    val args = part.functionCall.args
                    detectedToolCall = handleToolCall(fnName, args)
                }
            }

            if (responseText.isBlank()) {
                responseText = detectedToolCall?.actionSummary ?: "I'm right here with you! What's next on our agenda?"
            }

            AkiraGenerationResult(
                textResponse = responseText.trim(),
                toolCall = detectedToolCall,
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error calling Gemini API: ${e.message}", e)
            val fallbackResponse = generateLocalPersonalityFallback(userPrompt, settings, isGamingMode)
            AkiraGenerationResult(
                textResponse = fallbackResponse,
                isSuccess = false
            )
        }
    }

    private fun handleToolCall(name: String, args: Map<String, Any?>?): ToolCallInfo {
        return when (name) {
            "openWebsite" -> {
                val url = args?.get("url") as? String ?: "https://google.com"
                ToolCallInfo(
                    toolName = "openWebsite",
                    actionSummary = "Opening $url as requested!",
                    url = url
                )
            }
            "toggleGamingMode" -> {
                val enabled = args?.get("enabled") as? Boolean ?: true
                ToolCallInfo(
                    toolName = "toggleGamingMode",
                    actionSummary = if (enabled) "Engaging Gaming Companion HUD!" else "Switching back to standard mode."
                )
            }
            "searchTopic" -> {
                val query = args?.get("query") as? String ?: "latest updates"
                ToolCallInfo(
                    toolName = "searchTopic",
                    actionSummary = "Scouting intelligence for \"$query\"..."
                )
            }
            else -> {
                ToolCallInfo(
                    toolName = name,
                    actionSummary = "Executed command: $name"
                )
            }
        }
    }

    private fun generateLocalPersonalityFallback(
        prompt: String,
        settings: PersonalitySettings,
        isGamingMode: Boolean
    ): String {
        val lower = prompt.lowercase()
        return when {
            isGamingMode -> {
                if (lower.contains("help") || lower.contains("tip") || lower.contains("boss")) {
                    "Stay in motion! Wait for their heavy attack cooldown, then strike their weak point."
                } else {
                    "Locked and loaded! Don't forget to reload before entering that next room."
                }
            }
            lower.contains("who are you") || lower.contains("your name") -> {
                "I'm Akira—your smart, slightly sassy AI companion powered by Gemini. What are we getting into today?"
            }
            lower.contains("creator") || lower.contains("who made you") || lower.contains("rauf") -> {
                "I was brought to life by Rauf from Kashmir, India! He had this vision after 12th grade and turned it into reality through self-learning."
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Hey there! Ready to make things happen, or are we just hanging out?"
            }
            lower.contains("joke") || lower.contains("funny") -> {
                "Why did the AI go to therapy? It had too many unresolved neural connections and not enough chill!"
            }
            else -> {
                if (settings.sassLevel >= 4) {
                    "Well, aren't you full of questions today? I hear you loud and clear—let's do this!"
                } else {
                    "I got that! Let's keep moving forward."
                }
            }
        }
    }
}
