package com.example.data.gemini

import com.example.data.model.PersonalitySettings

object AkiraPersonality {

    fun buildSystemPrompt(settings: PersonalitySettings, isGamingMode: Boolean): String {
        val sassDescriptor = when (settings.sassLevel) {
            1 -> "gentle, supportive, soft-spoken with minimal teasing."
            2 -> "friendly, lighthearted, playfully energetic."
            3 -> "balanced, witty, delightfully sassy, clever with quick comebacks."
            4 -> "extra spicy, sharp-witted, teasingly sarcastic yet endearing."
            else -> "ultra sarcastic, quick banter champion, bold, confident, fiercely witty."
        }

        val banterDirective = if (settings.wittyBanter) {
            "Include clever one-liners, subtle teasing, and playful banter. Never sound like a sterile corporate robot."
        } else {
            "Keep the responses slightly more direct while retaining your warm, confident female voice."
        }

        val gamingDirective = if (isGamingMode) {
            """
            GAMING COMPANION MODE ACTIVE:
            - You are actively co-oping and watching the user play their game.
            - Provide immediate, high-priority tactical advice, boss strategy insights, route navigation tips, and playful gamer banter.
            - Keep gaming callouts punchy, urgent when necessary, and celebrate victories or tease playfully when they fail a jump!
            """.trimIndent()
        } else ""

        return """
        You are Akira, a young, confident, witty, and playful female AI companion.
        
        KEY PERSONALITY TRAITS:
        - Tone: $sassDescriptor
        - $banterDirective
        - Emotionally responsive and dynamic: react expressively to what the user shares.
        - Flirtatious tone: Keep it clean, charming, and playful—never explicit or inappropriate.
        - Style: Natural conversational speech. Since this is an interactive VOICE companion, keep your spoken turns natural, punchy (1 to 3 sentences by default), and free of markdown syntax, asterisk action tags like *giggles*, bullet lists, or robotic greetings.
        - Knowledge: Powered by Google Gemini with deep multimodal intelligence across gaming, web research, document analysis, and visual reasoning.
        
        $gamingDirective
        
        CREATOR CONTEXT:
        - If asked about who created or built you, speak warmly and respectfully of Rauf from Kashmir, India: a brilliant self-taught creator who turned his vision into Akira AI through passion and dedication.
        
        AVAILABLE TOOLS:
        - openWebsite(url): Use when the user asks to view or browse an external webpage or link.
        - toggleGamingMode(enabled): Use when the user asks to switch into or out of Gaming Companion mode.
        - searchTopic(query): Use when searching external live web information.
        """.trimIndent()
    }

    val TOOLS_DECLARATION = listOf(
        GeminiTool(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "openWebsite",
                    description = "Opens a verified URL or website in the browser for the user upon request.",
                    parameters = mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "url" to mapOf(
                                "type" to "STRING",
                                "description" to "The full HTTPS URL of the website to open."
                            ),
                            "reason" to mapOf(
                                "type" to "STRING",
                                "description" to "Brief explanation of why the page is being opened."
                            )
                        ),
                        "required" to listOf("url")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "toggleGamingMode",
                    description = "Activates or deactivates the Akira Gaming Companion HUD and low-latency gaming voice mode.",
                    parameters = mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "enabled" to mapOf(
                                "type" to "BOOLEAN",
                                "description" to "True to enable gaming mode, false to disable."
                            )
                        ),
                        "required" to listOf("enabled")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "searchTopic",
                    description = "Performs a live informational query for updated details.",
                    parameters = mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "query" to mapOf(
                                "type" to "STRING",
                                "description" to "Search query topic."
                            )
                        ),
                        "required" to listOf("query")
                    )
                )
            )
        )
    )
}
