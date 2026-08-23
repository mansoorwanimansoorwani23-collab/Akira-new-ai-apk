package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AkiraVoice
import com.example.data.model.PersonalitySettings
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraIndigoTertiary
import com.example.ui.theme.AkiraSuccessMint
import com.example.ui.theme.AkiraVioletSecondary
import com.example.ui.theme.AkiraWarningAmber
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun ProfileScreen(
    settings: PersonalitySettings,
    availableVoices: List<AkiraVoice>,
    onSelectVoice: (String) -> Unit,
    onPreviewVoice: (AkiraVoice) -> Unit,
    onUpdateSettings: (PersonalitySettings) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 95.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Profile Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AkiraCyanPrimary, AkiraVioletSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Profile",
                    tint = Color(0xFF070913),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "AKIRA PROFILE & MATRIX",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = Color.White
                )
                Text(
                    text = "Voice Library, Personality & Calibration",
                    fontSize = 11.sp,
                    color = AkiraCyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Voice Library Section
        Text(
            text = "VOICE LIBRARY (5 CALIBRATIONS)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AkiraCyanPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            availableVoices.forEach { voice ->
                val isSelected = settings.voiceId == voice.id
                VoiceLibraryCard(
                    voice = voice,
                    isSelected = isSelected,
                    onSelect = { onSelectVoice(voice.id) },
                    onPreview = { onPreviewVoice(voice) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Personality & Sass Matrix
        Text(
            text = "PERSONALITY & SASS MATRIX",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AkiraVioletSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardGlass),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x33D500F9), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Sass Level Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sassiness & Teasing Level",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = when (settings.sassLevel) {
                            1 -> "1: Gentle"
                            2 -> "2: Playful"
                            3 -> "3: Balanced Sassy"
                            4 -> "4: Extra Spicy"
                            else -> "5: Ultra Sarcastic"
                        },
                        color = AkiraVioletSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Slider(
                    value = settings.sassLevel.toFloat(),
                    onValueChange = { onUpdateSettings(settings.copy(sassLevel = it.toInt())) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = AkiraVioletSecondary,
                        activeTrackColor = AkiraVioletSecondary,
                        inactiveTrackColor = CyberDarkSurface
                    ),
                    modifier = Modifier.testTag("sass_level_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Witty Banter Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Witty Banter & One-Liners",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Clever punchlines & friendly teasing",
                            fontSize = 12.sp,
                            color = TextMedium
                        )
                    }

                    Switch(
                        checked = settings.wittyBanter,
                        onCheckedChange = { onUpdateSettings(settings.copy(wittyBanter = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF070913),
                            checkedTrackColor = AkiraCyanPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Low-End Device Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = AkiraWarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Low-End Device Mode",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Reduces animations for battery & low-end devices",
                            fontSize = 12.sp,
                            color = TextMedium
                        )
                    }

                    Switch(
                        checked = settings.lowEndDeviceMode,
                        onCheckedChange = { onUpdateSettings(settings.copy(lowEndDeviceMode = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF070913),
                            checkedTrackColor = AkiraWarningAmber
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset Conversation Memory
        Button(
            onClick = onClearHistory,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26152B)),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFD500F9)))),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("clear_history_button")
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFFF5252))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Session Context Memory", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Creator Section: "Built by Rauf"
        Text(
            text = "ABOUT CREATOR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AkiraSuccessMint
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0D1826)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(AkiraSuccessMint.copy(alpha = 0.6f), AkiraCyanPrimary.copy(alpha = 0.6f))),
                    RoundedCornerShape(20.dp)
                )
                .testTag("built_by_rauf_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AkiraSuccessMint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = AkiraSuccessMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Built by Rauf",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AkiraSuccessMint, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Kashmir, India",
                                    fontSize = 11.sp,
                                    color = AkiraSuccessMint
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF142B36))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = AkiraCyanPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Self-Taught", fontSize = 11.sp, color = AkiraCyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Akira AI was created by Rauf from Kashmir, India. After completing 12th grade, he had the vision for Akira AI in his mind, then started building it and turning the idea into a real, voice-first AI companion project through self-learning, curiosity, and experimentation.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFD4E2F2)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "“Every great idea begins with a spark of self-belief.”",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = AkiraSuccessMint
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. About Akira AI Architecture Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardGlass),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Akira AI Architecture",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• AI Intelligence: Google Gemini 3.5 & Live Multimodal models\n• Real-Time Audio: Native PCM & AudioWorklet live synthesis\n• Deployment: Cloud-ready Google infrastructure\n• Privacy: Secure client-side credential handling",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextMedium
                )
            }
        }
    }
}

@Composable
private fun VoiceLibraryCard(
    voice: AkiraVoice,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF192242) else CyberCardGlass)
            .border(
                1.dp,
                if (isSelected) AkiraCyanPrimary else Color(0x2200E5FF),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(14.dp)
            .testTag("voice_card_${voice.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) AkiraCyanPrimary.copy(alpha = 0.25f) else Color(0xFF141A2D)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isSelected) AkiraCyanPrimary else TextMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = voice.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AkiraCyanPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF070913),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = voice.personalityDescription,
                        fontSize = 12.sp,
                        color = TextMedium
                    )
                }
            }

            // Preview Play Button
            IconButton(
                onClick = onPreview,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AkiraVioletSecondary.copy(alpha = 0.2f))
                    .testTag("preview_voice_${voice.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Preview Voice",
                    tint = AkiraVioletSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
