package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraVioletSecondary
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.TextMedium

enum class AkiraNavScreen {
    VOICE_MAIN,
    GAMING_MODE,
    PROFILE
}

@Composable
fun QuickActionDock(
    currentScreen: AkiraNavScreen,
    isGamingActive: Boolean,
    onNavigate: (AkiraNavScreen) -> Unit,
    onOpenMultimodalSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberCardGlass.copy(alpha = 0.95f),
                        Color(0xFF0C0F1D).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x3300E5FF),
                        Color(0x33D500F9),
                        Color(0x3300E5FF)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                icon = Icons.Default.GraphicEq,
                label = "Akira Live",
                isSelected = currentScreen == AkiraNavScreen.VOICE_MAIN,
                testTag = "nav_voice_main",
                onClick = { onNavigate(AkiraNavScreen.VOICE_MAIN) }
            )

            DockItem(
                icon = Icons.Default.DocumentScanner,
                label = "Scan & Vision",
                isSelected = false,
                testTag = "nav_multimodal_scanner",
                onClick = onOpenMultimodalSheet
            )

            DockItem(
                icon = Icons.Default.Gamepad,
                label = "Gaming HUD",
                isSelected = currentScreen == AkiraNavScreen.GAMING_MODE || isGamingActive,
                badge = if (isGamingActive) "LIVE" else null,
                testTag = "nav_gaming_mode",
                onClick = { onNavigate(AkiraNavScreen.GAMING_MODE) }
            )

            DockItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == AkiraNavScreen.PROFILE,
                testTag = "nav_profile",
                onClick = { onNavigate(AkiraNavScreen.PROFILE) }
            )
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    val activeColor = if (isSelected) AkiraCyanPrimary else TextMedium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Brush.linearGradient(
                                    listOf(AkiraCyanPrimary.copy(alpha = 0.25f), AkiraVioletSecondary.copy(alpha = 0.25f))
                                )
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = activeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AkiraVioletSecondary)
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = activeColor
        )
    }
}
