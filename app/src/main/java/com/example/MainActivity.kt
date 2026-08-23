package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.model.AkiraState
import com.example.ui.components.AkiraBackground
import com.example.ui.components.AkiraNavScreen
import com.example.ui.components.MultimodalBottomSheet
import com.example.ui.components.QuickActionDock
import com.example.ui.components.ToolExecutionBanner
import com.example.ui.screens.GamingModeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VoiceHomeScreen
import com.example.ui.theme.AkiraAITheme
import com.example.ui.viewmodel.AkiraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AkiraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkiraAITheme {
                AkiraApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AkiraApp(viewModel: AkiraViewModel) {
    var currentScreen by remember { mutableStateOf(AkiraNavScreen.VOICE_MAIN) }
    var isMultimodalSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val akiraState by viewModel.akiraState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val lastSpokenResponse by viewModel.lastSpokenResponse.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val gamingData by viewModel.gamingState.collectAsState()
    val currentAttachment by viewModel.currentAttachment.collectAsState()
    val activeToolNotification by viewModel.activeToolNotification.collectAsState()

    // Permission launcher for live voice recognition
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.connectToAkira()
        }
    }

    LaunchedEffect(Unit) {
        // Prompt for mic permission smoothly on first launch
        val context = com.example.MainActivity()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            QuickActionDock(
                currentScreen = currentScreen,
                isGamingActive = gamingData.isGamingMode,
                onNavigate = { currentScreen = it },
                onOpenMultimodalSheet = { isMultimodalSheetOpen = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated Cyber Ambient Canvas
            AkiraBackground(
                state = akiraState,
                lowEndMode = settings.lowEndDeviceMode
            )

            // Screen Switcher
            when (currentScreen) {
                AkiraNavScreen.VOICE_MAIN -> {
                    VoiceHomeScreen(
                        akiraState = akiraState,
                        audioAmplitude = audioAmplitude,
                        liveTranscript = liveTranscript,
                        lastSpokenResponse = lastSpokenResponse,
                        errorMessage = errorMessage,
                        onVoiceOrbClick = {
                            if (akiraState == AkiraState.DISCONNECTED) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.onVoiceOrbTapped()
                            }
                        },
                        onConnectToggle = {
                            if (akiraState == AkiraState.DISCONNECTED) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.toggleConnection()
                            }
                        },
                        onSendManualPrompt = { prompt ->
                            viewModel.handleUserSpokenInput(prompt)
                        }
                    )
                }

                AkiraNavScreen.GAMING_MODE -> {
                    GamingModeScreen(
                        gamingData = gamingData,
                        akiraState = akiraState,
                        audioAmplitude = audioAmplitude,
                        onToggleGamingMode = { enabled ->
                            viewModel.toggleGamingMode(enabled)
                        },
                        onSelectGame = { gameTitle ->
                            viewModel.selectGame(gameTitle)
                        },
                        onRequestTacticalHint = {
                            viewModel.requestTacticalAdvice()
                        },
                        onVoiceOrbClick = {
                            if (akiraState == AkiraState.DISCONNECTED) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.onVoiceOrbTapped()
                            }
                        },
                        onOpenMultimodalSheet = {
                            isMultimodalSheetOpen = true
                        }
                    )
                }

                AkiraNavScreen.PROFILE -> {
                    ProfileScreen(
                        settings = settings,
                        availableVoices = viewModel.availableVoices,
                        onSelectVoice = { voiceId ->
                            viewModel.selectVoice(voiceId)
                        },
                        onPreviewVoice = { voice ->
                            viewModel.previewVoice(voice)
                        },
                        onUpdateSettings = { newSettings ->
                            viewModel.updatePersonalitySettings(newSettings)
                        },
                        onClearHistory = {
                            viewModel.clearConversationHistory()
                        }
                    )
                }
            }

            // Floating Top Banner for Tool Executions
            ToolExecutionBanner(
                tool = activeToolNotification,
                onDismiss = { viewModel.dismissToolNotification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )

            // Multimodal Scanner Bottom Sheet
            if (isMultimodalSheetOpen) {
                MultimodalBottomSheet(
                    sheetState = sheetState,
                    currentAttachment = currentAttachment,
                    onDismiss = { isMultimodalSheetOpen = false },
                    onImageSelected = { uri ->
                        viewModel.attachImageUri(uri)
                    },
                    onDocumentSubmitted = { title, content ->
                        viewModel.attachDocumentText(title, content)
                    },
                    onClearAttachment = {
                        viewModel.clearAttachment()
                    },
                    onAskAkiraWithAttachment = { prompt ->
                        viewModel.handleUserSpokenInput(prompt)
                    }
                )
            }
        }
    }
}
