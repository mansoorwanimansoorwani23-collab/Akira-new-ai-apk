package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AkiraState
import com.example.ui.components.AkiraVoiceOrb
import com.example.ui.components.LiveWaveformBar
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraSuccessMint
import com.example.ui.theme.AkiraVioletSecondary
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun VoiceHomeScreen(
    akiraState: AkiraState,
    audioAmplitude: Float,
    liveTranscript: String,
    lastSpokenResponse: String,
    errorMessage: String?,
    onVoiceOrbClick: () -> Unit,
    onConnectToggle: () -> Unit,
    onSendManualPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var manualInputText by remember { mutableStateOf("") }
    val quickSuggestions = listOf(
        "Who created you?",
        "What's your sass level?",
        "Give me a gaming tip",
        "Tell me something witty",
        "Explain quantum computing in 2 lines"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Akira Identity Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AkiraCyanPrimary, AkiraVioletSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Akira Logo",
                        tint = Color(0xFF070913),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "AKIRA AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Gemini Live Voice Companion",
                        fontSize = 11.sp,
                        color = AkiraCyanPrimary
                    )
                }
            }

            // Connection Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberCardGlass)
                    .border(
                        1.dp,
                        if (akiraState == AkiraState.DISCONNECTED) Color(0x33FFFFFF) else AkiraCyanPrimary.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(onClick = onConnectToggle)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("toggle_connect_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (akiraState) {
                                AkiraState.DISCONNECTED -> Color.Gray
                                AkiraState.ERROR -> Color(0xFFFF5252)
                                AkiraState.CONNECTING -> Color(0xFFFFAB00)
                                else -> AkiraSuccessMint
                            }
                        )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = akiraState.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Toggle Link",
                    tint = if (akiraState == AkiraState.DISCONNECTED) Color.Gray else AkiraCyanPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // State Guidance Label
        Text(
            text = akiraState.description,
            fontSize = 13.sp,
            color = if (akiraState == AkiraState.ERROR) Color(0xFFFF8A80) else TextMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Central Voice Orb
        AkiraVoiceOrb(
            state = akiraState,
            amplitude = audioAmplitude,
            onClick = onVoiceOrbClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Reactive Audio Equalizer
        LiveWaveformBar(
            state = akiraState,
            amplitude = audioAmplitude
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Transcript / Spoken Output Display
        AnimatedVisibility(
            visible = liveTranscript.isNotBlank() || akiraState == AkiraState.LISTENING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1528))
                    .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = AkiraCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (liveTranscript.isBlank()) "Listening for your voice..." else "“$liveTranscript”",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontStyle = if (liveTranscript.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Akira's Response Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(CyberCardGlass, Color(0xFF10152B))
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(Color(0x3300E5FF), Color(0x33D500F9))),
                    RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
                .testTag("akira_response_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AkiraVioletSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AKIRA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AkiraVioletSecondary
                        )
                    }

                    if (akiraState == AkiraState.SPEAKING) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speaking",
                                tint = AkiraCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Speaking live",
                                fontSize = 11.sp,
                                color = AkiraCyanPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lastSpokenResponse,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.White
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFFF5252),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Topic Chips
        Text(
            text = "TRY ASKING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickSuggestions) { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberCardGlass)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onSendManualPrompt(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 12.sp,
                        color = TextMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quiet Text Mode Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualInputText,
                onValueChange = { manualInputText = it },
                placeholder = { Text("Or type a prompt...", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AkiraCyanPrimary,
                    unfocusedBorderColor = Color(0x2200E5FF),
                    focusedContainerColor = CyberCardGlass,
                    unfocusedContainerColor = CyberCardGlass
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (manualInputText.isNotBlank()) {
                        onSendManualPrompt(manualInputText)
                        manualInputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (manualInputText.isNotBlank()) AkiraCyanPrimary else Color(0xFF1F263D)
                    )
                    .testTag("send_manual_prompt_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (manualInputText.isNotBlank()) Color(0xFF070913) else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
