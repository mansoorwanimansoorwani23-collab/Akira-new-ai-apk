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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.AkiraState
import com.example.data.model.GamingCompanionData
import com.example.ui.components.LiveWaveformBar
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraVioletSecondary
import com.example.ui.theme.AkiraWarningAmber
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun GamingModeScreen(
    gamingData: GamingCompanionData,
    akiraState: AkiraState,
    audioAmplitude: Float,
    onToggleGamingMode: (Boolean) -> Unit,
    onSelectGame: (String) -> Unit,
    onRequestTacticalHint: () -> Unit,
    onVoiceOrbClick: () -> Unit,
    onOpenMultimodalSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supportedGames = listOf(
        "Cyberpunk Odyssey",
        "Elden Ring / Souls",
        "Valorant / Tactical FPS",
        "League of Legends / MOBA",
        "Genshin Impact / RPG",
        "Minecraft / Sandbox"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Gaming HUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AkiraVioletSecondary, AkiraCyanPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Gaming HUD",
                        tint = Color(0xFF070913),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "GAMING COMPANION",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Low-Latency Tactical Co-Op AI",
                        fontSize = 11.sp,
                        color = AkiraVioletSecondary
                    )
                }
            }

            // HUD Master Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (gamingData.isGamingMode) "HUD ACTIVE" else "STANDBY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gamingData.isGamingMode) AkiraCyanPrimary else TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = gamingData.isGamingMode,
                    onCheckedChange = onToggleGamingMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF070913),
                        checkedTrackColor = AkiraCyanPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CyberDarkSurface
                    ),
                    modifier = Modifier.testTag("gaming_hud_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Selection Chips
        Text(
            text = "SELECT ACTIVE GAME",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(supportedGames) { game ->
                val isSelected = gamingData.currentGameTitle == game
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) AkiraVioletSecondary.copy(alpha = 0.25f) else CyberCardGlass)
                        .border(
                            1.dp,
                            if (isSelected) AkiraVioletSecondary else Color(0x22FFFFFF),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectGame(game) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("game_chip_${game.take(5)}")
                ) {
                    Text(
                        text = game,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tactical Audio HUD Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF14172B), Color(0xFF0E1220))
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(AkiraCyanPrimary, AkiraVioletSecondary)),
                    RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AkiraCyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = AkiraCyanPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = gamingData.currentGameTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Akira Audio Co-Pilot Active",
                                fontSize = 11.sp,
                                color = AkiraCyanPrimary
                            )
                        }
                    }

                    // Tactical Co-Op Quick Voice Button
                    Button(
                        onClick = onVoiceOrbClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (akiraState == AkiraState.LISTENING) AkiraCyanPrimary else AkiraVioletSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("gaming_voice_coop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Callout",
                            tint = Color(0xFF070913),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (akiraState == AkiraState.LISTENING) "Listening..." else "Talk Co-Op",
                            color = Color(0xFF070913),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LiveWaveformBar(
                    state = akiraState,
                    amplitude = audioAmplitude,
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Latest Tactical Advice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090D1A))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AkiraWarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = gamingData.recentTacticalHint,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Tactical Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRequestTacticalHint,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("request_tactical_hint_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCardGlass),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(AkiraCyanPrimary, AkiraVioletSecondary))
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = AkiraCyanPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ask Strategy", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onOpenMultimodalSheet,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("gaming_screen_scan_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCardGlass),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(AkiraVioletSecondary, AkiraCyanPrimary))
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Gamepad, contentDescription = null, tint = AkiraVioletSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analyze Screen", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tactical Callouts History
        Text(
            text = "LIVE TACTICAL LOG",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 85.dp)
        ) {
            items(gamingData.hintsHistory) { hint ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardGlass)
                        .padding(12.dp)
                ) {
                    Text(
                        text = hint,
                        fontSize = 13.sp,
                        color = TextMedium,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
