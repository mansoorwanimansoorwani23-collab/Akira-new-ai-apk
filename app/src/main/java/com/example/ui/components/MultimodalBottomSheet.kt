package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MultimodalData
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraVioletSecondary
import com.example.ui.theme.CyberCardGlass
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.TextMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultimodalBottomSheet(
    sheetState: SheetState,
    currentAttachment: MultimodalData?,
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onDocumentSubmitted: (String, String) -> Unit,
    onClearAttachment: () -> Unit,
    onAskAkiraWithAttachment: (String) -> Unit
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    var docTitle by remember { mutableStateOf("") }
    var docContent by remember { mutableStateOf("") }
    var userQuestion by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("IMAGE") } // IMAGE, DOC, SCREEN

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF333D5E))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Multimodal Vision & Documents",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Akira can analyze images, PDFs, documents, or screenshots",
                        fontSize = 12.sp,
                        color = TextMedium
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardGlass)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabPill("IMAGE", "Photos", Icons.Default.Image, activeTab == "IMAGE") { activeTab = "IMAGE" }
                TabPill("DOC", "Document/Text", Icons.Default.Description, activeTab == "DOC") { activeTab = "DOC" }
                TabPill("SCREEN", "Screen/Game Snip", Icons.Default.ScreenShare, activeTab == "SCREEN") { activeTab = "SCREEN" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentAttachment != null) {
                // Attached Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberCardGlass)
                        .border(1.dp, AkiraCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentAttachment.bitmap != null) {
                            Image(
                                bitmap = currentAttachment.bitmap.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AkiraVioletSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = AkiraVioletSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentAttachment.title,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ready for Akira to analyze",
                                color = AkiraCyanPrimary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = onClearAttachment) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFFF5252))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prompt Input
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = { Text("Ask Akira about this (or leave blank to analyze)...", color = TextMedium) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attachment_question_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AkiraCyanPrimary,
                        unfocusedBorderColor = Color(0x3300E5FF)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val prompt = userQuestion.ifBlank { "Akira, examine this and share your witty breakdown!" }
                        onAskAkiraWithAttachment(prompt)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_attachment_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AkiraCyanPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF070913))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask Akira", color = Color(0xFF070913), fontWeight = FontWeight.Bold)
                }
            } else {
                when (activeTab) {
                    "IMAGE" -> {
                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("pick_image_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCardGlass),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(AkiraCyanPrimary, AkiraVioletSecondary))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AkiraCyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image from Gallery", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                    "DOC" -> {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text("Document Title", color = TextMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AkiraCyanPrimary,
                                unfocusedBorderColor = Color(0x3300E5FF)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = docContent,
                            onValueChange = { docContent = it },
                            label = { Text("Paste Text / PDF Excerpt / Code", color = TextMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AkiraCyanPrimary,
                                unfocusedBorderColor = Color(0x3300E5FF)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (docContent.isNotBlank()) {
                                    val title = docTitle.ifBlank { "Shared Document" }
                                    onDocumentSubmitted(title, docContent)
                                }
                            },
                            enabled = docContent.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AkiraVioletSecondary)
                        ) {
                            Text("Attach Document for Akira", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    "SCREEN" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyberCardGlass)
                                .padding(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenShare,
                                contentDescription = null,
                                tint = AkiraCyanPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Screen & Game Stream Analyzer",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attach a screenshot of your active game, chart, or app for instant strategic feedback.",
                                fontSize = 12.sp,
                                color = TextMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { imagePicker.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = AkiraCyanPrimary)
                            ) {
                                Text("Upload Screen Snip", color = Color(0xFF070913), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    key: String,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AkiraCyanPrimary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AkiraCyanPrimary else TextMedium,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AkiraCyanPrimary else TextMedium
        )
    }
}
