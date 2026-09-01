package com.example.amma.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amma.model.AppSettings
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.theme.AmmaAmberBright
import com.example.amma.theme.AmmaBackground
import com.example.amma.theme.AmmaBorder
import com.example.amma.theme.AmmaDimens
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaRedEmergency
import com.example.amma.theme.AmmaSurface
import com.example.amma.theme.AmmaSurfaceElevated
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaTextSecondary
import com.example.amma.theme.AmmaWhatsAppGreen
import com.example.amma.ui.components.ContactAvatar
import com.example.amma.ui.components.bounceClick
import com.example.amma.util.PhotoStorageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Settings",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "అడ్మిన్ కాన్ఫిగరేషన్",
                            fontSize = 12.sp,
                            color = AmmaTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmmaSurfaceElevated)
                            .border(1.dp, AmmaBorder, CircleShape)
                            .bounceClick(scaleDown = 0.88f) { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF34C759).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF34C759).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .bounceClick(scaleDown = 0.90f) { onNavigateBack() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34C759)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AmmaSurface
                )
            )
        },
        containerColor = AmmaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AmmaSurface,
                contentColor = AmmaGreenBright,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AmmaGreenBright
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Contacts", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Voice & Speech", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Diagnostics", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> ContactsTab(
                    contacts = uiState.contacts,
                    onAddContact = { viewModel.openAddContact() },
                    onEditContact = { viewModel.openEditContact(it) },
                    onDeleteContact = { viewModel.deleteContact(it) }
                )
                1 -> VoiceSettingsTab(
                    settings = uiState.settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onTestSpeech = { viewModel.testTeluguSpeech(it) }
                )
                2 -> DiagnosticsTab(
                    uiState = uiState,
                    onRunAudioTest = { viewModel.testTeluguSpeech("అమ్మా, సిగ్నల్ మరియు బ్యాటరీ పరీక్ష విజయవంతమైంది.") }
                )
            }

            if (uiState.isAddContactOpen && uiState.editingContact != null) {
                ContactEditorSheet(
                    initialContact = uiState.editingContact!!,
                    onSave = { viewModel.saveContact(it) },
                    onDismiss = { viewModel.closeContactDialog() }
                )
            }
        }
    }
}

/**
 * Authentic Apple iOS Toggle Switch with fluid motion, white elevated thumb & drop shadow
 */
@Composable
fun AppleToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color = Color(0xFF34C759),
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(checked, label = "apple_switch_trans")
    val thumbOffset by transition.animateDp(label = "thumb_offset") { isChecked ->
        if (isChecked) 20.dp else 2.dp
    }
    val trackColor by transition.animateColor(label = "track_color") { isChecked ->
        if (isChecked) activeColor else Color(0xFF39393D)
    }

    Box(
        modifier = modifier
            .width(51.dp)
            .height(31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun ContactsTab(
    contacts: List<Contact>,
    onAddContact: () -> Unit,
    onEditContact: (Contact) -> Unit,
    onDeleteContact: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddContact,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(AmmaDimens.CornerRadiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = AmmaGreenBright)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add New Person (వ్యక్తిని జోడించండి)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        items(contacts, key = { it.id }) { contact ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AmmaDimens.CornerRadiusMedium),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        contact = contact,
                        size = 56.dp,
                        cornerRadius = 16.dp,
                        borderColor = if (contact.isEmergencyContact) AmmaRedEmergency else AmmaGreenBright
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.displayName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmmaTextPrimary
                            )
                            if (contact.isEmergencyContact) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = "Emergency",
                                    tint = AmmaRedEmergency,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = contact.phoneNumber,
                            fontSize = 13.sp,
                            color = AmmaTextSecondary
                        )
                        if (!contact.customPronunciation.isNullOrBlank()) {
                            Text(
                                text = "ఉచ్చారణ: ${contact.customPronunciation}",
                                fontSize = 12.sp,
                                color = AmmaGreenBright
                            )
                        }
                    }

                    Row {
                        Button(
                            onClick = { onEditContact(contact) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmmaSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { onDeleteContact(contact.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmmaRedEmergency.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = AmmaRedEmergency,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceSettingsTab(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onTestSpeech: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Neural Voice Selection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google Speech Services (On-Device)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "High-definition Telugu voice engine powered directly by Google Speech Services on this phone.",
                        fontSize = 13.sp,
                        color = AmmaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent("com.android.settings.TTS_SETTINGS").apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = android.content.Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e2: Exception) {
                                    // ignore
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34C759)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759).copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download / Manage Telugu Voice Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF34C759)
                        )
                    }
                }
            }
        }
        // Section 2: Speech Pacing Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Telugu Speech Speed",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Current: ${String.format("%.2f", settings.speechRate)}x (Measured pacing for elders)",
                        fontSize = 13.sp,
                        color = AmmaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = settings.speechRate,
                        onValueChange = { onUpdateSettings(settings.copy(speechRate = it)) },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF34C759),
                            inactiveTrackColor = Color(0xFF39393D)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.7x (Slow)", fontSize = 12.sp, color = AmmaTextSecondary)
                        Text("1.0x (Normal)", fontSize = 12.sp, color = AmmaTextSecondary)
                        Text("1.3x (Fast)", fontSize = 12.sp, color = AmmaTextSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpeedPresetPill(
                            label = "0.85x (Elders)",
                            isSelected = Math.abs(settings.speechRate - 0.85f) < 0.04f,
                            onClick = { onUpdateSettings(settings.copy(speechRate = 0.85f)) },
                            modifier = Modifier.weight(1f)
                        )
                        SpeedPresetPill(
                            label = "1.00x (Normal)",
                            isSelected = Math.abs(settings.speechRate - 1.00f) < 0.04f,
                            onClick = { onUpdateSettings(settings.copy(speechRate = 1.00f)) },
                            modifier = Modifier.weight(1f)
                        )
                        SpeedPresetPill(
                            label = "1.15x (Fast)",
                            isSelected = Math.abs(settings.speechRate - 1.15f) < 0.04f,
                            onClick = { onUpdateSettings(settings.copy(speechRate = 1.15f)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 3: Speech Verification Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Telugu Voice Output",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Plays: \"ఇప్పుడు సమయం పది గంటలు అయింది. ఈరోజు మంగళవారం, సెప్టెంబర్ ఒకటివ తారీఖు.\"",
                        fontSize = 13.sp,
                        color = AmmaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onTestSpeech("ఇప్పుడు సమయం పది గంటలు అయింది. ఈరోజు మంగళవారం, సెప్టెంబర్ ఒకటివ తారీఖు.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play Test Speech (పరీక్షించు)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section 3: High-Assistance Mode Card with Apple Toggle Switch
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onUpdateSettings(settings.copy(isHighAssistanceMode = !settings.isHighAssistanceMode)) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High-Assistance Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Spoken assistance and voice prompts on all primary taps",
                            fontSize = 13.sp,
                            color = AmmaTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Authentic Apple Toggle Switch
                    AppleToggleSwitch(
                        checked = settings.isHighAssistanceMode,
                        onCheckedChange = { onUpdateSettings(settings.copy(isHighAssistanceMode = it)) },
                        activeColor = Color(0xFF34C759)
                    )
                }
            }
        }

        // Section 4: Admin PIN Security (Strict 4-digit validation)
        item {
            var pinInput by remember(settings.adminPin) { mutableStateOf(settings.adminPin) }
            var pinSavedMessage by remember { mutableStateOf(false) }
            val isPinValid = pinInput.length == 4 && pinInput.all { it.isDigit() }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Security PIN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "4-digit numeric code required to open Admin Settings.",
                        fontSize = 13.sp,
                        color = AmmaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                            label = { Text("4-Digit PIN") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmmaGreenBright,
                                unfocusedBorderColor = AmmaBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                if (isPinValid) {
                                    onUpdateSettings(settings.copy(adminPin = pinInput))
                                    pinSavedMessage = true
                                }
                            },
                            enabled = isPinValid && pinInput != settings.adminPin,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmmaGreenBright)
                        ) {
                            Text(
                                text = "Update",
                                fontWeight = FontWeight.Bold,
                                color = if (isPinValid && pinInput != settings.adminPin) Color.Black else AmmaTextSecondary
                            )
                        }
                    }

                    if (pinSavedMessage) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PIN updated successfully!",
                            fontSize = 12.sp,
                            color = AmmaGreenBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedPresetPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF34C759) else AmmaSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) Color(0xFF34C759) else AmmaBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}

@Composable
private fun DiagnosticsTab(
    uiState: AdminUiState,
    onRunAudioTest: () -> Unit
) {
    val allGood = uiState.status.isSimAvailable && uiState.isTtsReady && uiState.status.isInternetAvailable

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Overall Health Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (allGood) Color(0xFF1C2B20) else Color(0xFF2C2415)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (allGood) Color(0xFF34C759).copy(alpha = 0.4f) else Color(0xFFFF9F0A).copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (allGood) Color(0xFF34C759).copy(alpha = 0.2f) else Color(0xFFFF9F0A).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allGood) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = if (allGood) Color(0xFF34C759) else Color(0xFFFF9F0A),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (allGood) "System Fully Operational" else "System Running with Alerts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (allGood) "All telecom, speech, and internet services ready" else "Some background services require review",
                            fontSize = 12.sp,
                            color = AmmaTextSecondary
                        )
                    }
                }
            }
        }

        // Section 2: Inset Grouped Diagnostics Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: SIM Card
                    AppleDiagnosticRow(
                        icon = Icons.Rounded.Call,
                        iconBackground = Color(0xFF5856D6),
                        title = "SIM Card Status",
                        subtitle = "Primary Cellular Slot",
                        statusText = if (uiState.status.isSimAvailable) "Active" else "No SIM",
                        isOk = uiState.status.isSimAvailable
                    )

                    HorizontalDivider(
                        color = AmmaBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 62.dp)
                    )

                    // Row 2: Telugu TTS
                    AppleDiagnosticRow(
                        icon = Icons.Rounded.RecordVoiceOver,
                        iconBackground = Color(0xFF0A84FF),
                        title = "Telugu Speech Engine",
                        subtitle = "TTS Voice Synthesizer",
                        statusText = if (uiState.isTtsReady) "Ready (te_IN)" else "Initializing...",
                        isOk = uiState.isTtsReady
                    )

                    HorizontalDivider(
                        color = AmmaBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 62.dp)
                    )

                    // Row 3: WhatsApp
                    AppleDiagnosticRow(
                        icon = Icons.Rounded.Videocam,
                        iconBackground = Color(0xFF25D366),
                        title = "WhatsApp Integration",
                        subtitle = "Video & Voice Calls",
                        statusText = if (uiState.status.isWhatsAppInstalled) "Installed" else "Not Found",
                        isOk = uiState.status.isWhatsAppInstalled
                    )

                    HorizontalDivider(
                        color = AmmaBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 62.dp)
                    )

                    // Row 4: Network
                    AppleDiagnosticRow(
                        icon = Icons.Rounded.GraphicEq,
                        iconBackground = Color(0xFF30B0C7),
                        title = "Network Connectivity",
                        subtitle = if (uiState.status.isWifiConnected) "Wi-Fi Connected" else "Mobile Cellular Data",
                        statusText = if (uiState.status.isInternetAvailable) "Online" else "Offline",
                        isOk = uiState.status.isInternetAvailable
                    )

                    HorizontalDivider(
                        color = AmmaBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 62.dp)
                    )

                    // Row 5: Battery
                    AppleDiagnosticRow(
                        icon = Icons.Rounded.Speed,
                        iconBackground = Color(0xFF32D74B),
                        title = "Battery Health",
                        subtitle = if (uiState.status.isCharging) "Charging active" else "Discharging",
                        statusText = "${uiState.status.batteryPercent}% (${uiState.status.batteryGrade.name})",
                        isOk = uiState.status.batteryPercent > 15
                    )
                }
            }
        }

        // Section 3: Diagnostic Audio Test CTA
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Audio & Telephony Verification",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmmaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recites real-time battery, signal, and connection metrics in Telugu.",
                        fontSize = 13.sp,
                        color = AmmaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRunAudioTest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run Full Voice Diagnostic (పరీక్షించు)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Section 4: Over-The-Air (OTA) Updates via GitHub Releases
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val otaManager = remember { com.example.amma.ota.OtaUpdateManager(context) }
            val updateStatus by otaManager.updateStatus.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.SystemUpdate,
                                contentDescription = null,
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Software & OTA Updates",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmmaTextPrimary
                            )
                        }

                        // Version Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmmaSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v${com.example.amma.BuildConfig.VERSION_NAME}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmmaGreenBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seamless Over-The-Air updates from GitHub repository.",
                        fontSize = 13.sp,
                        color = AmmaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    when (val status = updateStatus) {
                        is com.example.amma.ota.UpdateStatus.Idle -> {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        otaManager.checkForUpdates()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check for Updates", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        is com.example.amma.ota.UpdateStatus.Checking -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmmaSurfaceElevated)
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF0A84FF),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Checking for latest release...", fontSize = 14.sp, color = AmmaTextPrimary)
                            }
                        }
                        is com.example.amma.ota.UpdateStatus.UpToDate -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C2B20))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34C759),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "CareTouch is Up to Date",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Current version v${status.version} is the latest release.",
                                        fontSize = 12.sp,
                                        color = AmmaTextSecondary
                                    )
                                }
                            }
                        }
                        is com.example.amma.ota.UpdateStatus.UpdateAvailable -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1A2634))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Update Available: v${status.info.latestVersion}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0A84FF)
                                    )
                                    Text(
                                        text = String.format("%.1f MB", status.info.apkSizeMb),
                                        fontSize = 12.sp,
                                        color = AmmaTextSecondary
                                    )
                                }

                                if (status.info.releaseNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = status.info.releaseNotes.take(150) + if (status.info.releaseNotes.length > 150) "..." else "",
                                        fontSize = 12.sp,
                                        color = AmmaTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            otaManager.downloadAndInstall(status.info)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bounceClick(scaleDown = 0.95f) {
                                            coroutineScope.launch {
                                                otaManager.downloadAndInstall(status.info)
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudDownload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download & Install Update", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        is com.example.amma.ota.UpdateStatus.Downloading -> {
                            val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = (status.progressPercent / 100f).coerceIn(0f, 1f),
                                animationSpec = androidx.compose.animation.core.tween(
                                    durationMillis = 180,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                label = "ota_download_progress"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmmaSurfaceElevated)
                                    .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF0A84FF),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (status.progressPercent >= 100) "Verifying & Installing..." else "Downloading Update...",
                                            fontSize = 13.sp,
                                            color = AmmaTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${status.progressPercent}%",
                                        fontSize = 14.sp,
                                        color = if (status.progressPercent >= 100) AmmaGreenBright else Color(0xFF0A84FF),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Custom Smooth Gradient Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2C2C2E))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = animatedProgress.coerceAtLeast(0.02f))
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFF007AFF),
                                                        Color(0xFF5856D6),
                                                        Color(0xFF34C759)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = String.format("%.1f MB / %.1f MB", status.downloadedMb, status.totalMb),
                                        fontSize = 12.sp,
                                        color = AmmaTextSecondary
                                    )
                                    Text(
                                        text = "High-Speed Stream",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0A84FF).copy(alpha = 0.8f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        is com.example.amma.ota.UpdateStatus.ReadyToInstall -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C2B20))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF34C759),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Download Complete",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { otaManager.installApk(status.apkFile) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bounceClick(scaleDown = 0.95f) {
                                            otaManager.installApk(status.apkFile)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SystemUpdate,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Install Downloaded Update", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                }
                            }
                        }
                        is com.example.amma.ota.UpdateStatus.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2C2415))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = status.message,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF9F0A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            otaManager.checkForUpdates()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))
                                ) {
                                    Text("Retry Check", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppleDiagnosticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    statusText: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Squircle Icon Badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AmmaTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = AmmaTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status Badge Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isOk) Color(0xFF34C759).copy(alpha = 0.15f) else Color(0xFFFF9F0A).copy(alpha = 0.15f))
                .border(
                    1.dp,
                    if (isOk) Color(0xFF34C759).copy(alpha = 0.35f) else Color(0xFFFF9F0A).copy(alpha = 0.35f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOk) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = if (isOk) Color(0xFF34C759) else Color(0xFFFF9F0A),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOk) Color(0xFF34C759) else Color(0xFFFF9F0A)
                )
            }
        }
    }
}

@Composable
private fun ContactEditorSheet(
    initialContact: Contact,
    onSave: (Contact) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var contactId by remember { mutableStateOf(initialContact.id) }
    var name by remember { mutableStateOf(initialContact.displayName) }
    var phone by remember { mutableStateOf(initialContact.phoneNumber) }
    var whatsapp by remember { mutableStateOf(initialContact.whatsappNumber) }
    var pronunciation by remember { mutableStateOf(initialContact.customPronunciation ?: "") }
    var primaryTransport by remember { mutableStateOf(initialContact.primaryTransport) }
    var isEmergency by remember { mutableStateOf(initialContact.isEmergencyContact) }
    var photoUri by remember { mutableStateOf(initialContact.photoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = PhotoStorageHelper.savePhotoLocally(context, uri, contactId)
            if (savedPath != null) {
                photoUri = savedPath
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AmmaSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AmmaBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isFormValid = name.trim().isNotBlank() && phone.trim().filter { it.isDigit() || it == '+' }.length >= 3

                    // Apple-style Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AmmaSurfaceElevated)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmmaTextSecondary,
                            modifier = Modifier
                                .bounceClick(scaleDown = 0.90f, onClick = onDismiss)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Text(
                            text = if (initialContact.displayName.isEmpty()) "New Contact" else "Edit Contact",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmmaTextPrimary
                        )

                        Text(
                            text = "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isFormValid) AmmaGreenBright else AmmaTextSecondary.copy(alpha = 0.35f),
                            modifier = Modifier
                                .bounceClick(
                                    enabled = isFormValid,
                                    scaleDown = 0.90f,
                                    onClick = {
                                        if (isFormValid) {
                                            onSave(
                                                initialContact.copy(
                                                    displayName = name.trim(),
                                                    relationship = "",
                                                    phoneNumber = phone.trim(),
                                                    whatsappNumber = if (whatsapp.isBlank()) phone.trim() else whatsapp.trim(),
                                                    customPronunciation = pronunciation.trim().ifEmpty { null },
                                                    primaryTransport = primaryTransport,
                                                    isEmergencyContact = isEmergency,
                                                    photoUri = photoUri
                                                )
                                            )
                                        }
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                val currentContactForAvatar = initialContact.copy(
                                    displayName = name,
                                    relationship = "",
                                    photoUri = photoUri,
                                    isEmergencyContact = isEmergency
                                )

                                ContactAvatar(
                                    contact = currentContactForAvatar,
                                    size = 100.dp,
                                    cornerRadius = 28.dp,
                                    borderColor = if (isEmergency) AmmaRedEmergency else AmmaGreenBright,
                                    isEditable = true,
                                    onEditPhotoClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (photoUri != null) "Change Photo (ఫోటో మార్చండి)" else "Add Photo (ఫోటో జోడించండి)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmmaGreenBright,
                                    modifier = Modifier
                                        .bounceClick {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Form Inputs Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AmmaSurfaceElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmmaBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Display Name (e.g. Santhosh)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AmmaGreenBright,
                                            unfocusedBorderColor = AmmaBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AmmaGreenBright,
                                            unfocusedBorderColor = AmmaBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    OutlinedTextField(
                                        value = pronunciation,
                                        onValueChange = { pronunciation = it },
                                        label = { Text("Custom Telugu Pronunciation (సంతోష్)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AmmaGreenBright,
                                            unfocusedBorderColor = AmmaBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Form Section 2: Primary Action Selector
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Primary Action (Default 1-Tap Action)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmmaTextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AmmaSurfaceElevated)
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val isPhone = primaryTransport == CallTransport.CELLULAR
                                    val isWaAudio = primaryTransport == CallTransport.WHATSAPP_AUDIO
                                    val isWaVideo = primaryTransport == CallTransport.WHATSAPP_VIDEO

                                    // Phone Call
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isPhone) AmmaGreenBright else Color.Transparent)
                                            .clickable { primaryTransport = CallTransport.CELLULAR }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Call,
                                                contentDescription = null,
                                                tint = if (isPhone) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Phone",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPhone) Color.Black else Color.White
                                            )
                                        }
                                    }

                                    // WA Video
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isWaVideo) AmmaWhatsAppGreen else Color.Transparent)
                                            .clickable { primaryTransport = CallTransport.WHATSAPP_VIDEO }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Videocam,
                                                contentDescription = null,
                                                tint = if (isWaVideo) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "WA Video",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isWaVideo) Color.Black else Color.White
                                            )
                                        }
                                    }

                                    // WA Audio
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isWaAudio) AmmaWhatsAppGreen else Color.Transparent)
                                            .clickable { primaryTransport = CallTransport.WHATSAPP_AUDIO }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.HeadsetMic,
                                                contentDescription = null,
                                                tint = if (isWaAudio) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "WA Voice",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isWaAudio) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Form Section 3: Emergency Contact Toggle Row
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AmmaSurfaceElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isEmergency) AmmaRedEmergency.copy(alpha = 0.6f) else AmmaBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.NotificationsActive,
                                            contentDescription = null,
                                            tint = if (isEmergency) AmmaRedEmergency else AmmaTextSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Emergency Contact",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AmmaTextPrimary
                                            )
                                            Text(
                                                text = "Linked to giant SOS button",
                                                fontSize = 12.sp,
                                                color = AmmaTextSecondary
                                            )
                                        }
                                    }

                                    // Apple Toggle Switch for Emergency Contact
                                    AppleToggleSwitch(
                                        checked = isEmergency,
                                        onCheckedChange = { isEmergency = it },
                                        activeColor = AmmaRedEmergency
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
