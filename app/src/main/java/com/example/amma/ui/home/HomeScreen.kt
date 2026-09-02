package com.example.amma.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amma.cloud.auth.AuthState
import com.example.amma.model.CallState
import com.example.amma.theme.AmmaBackground
import com.example.amma.theme.AmmaDimens
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaRedBright
import com.example.amma.theme.AmmaSurfaceElevated
import com.example.amma.theme.AmmaTextMuted
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaTextSecondary
import com.example.amma.ui.components.AdminPinDialog
import com.example.amma.ui.components.CaregiverLoginSheet
import com.example.amma.ui.components.ContactActionModal
import com.example.amma.ui.components.ContactTile
import com.example.amma.ui.components.EmergencyTile
import com.example.amma.ui.components.FallbackModal
import com.example.amma.ui.components.SpokenClockCard
import com.example.amma.ui.components.StatusIndicatorRow
import com.example.amma.ui.components.TellMeButton
import com.example.amma.ui.components.bounceClick

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAdmin: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Edge Case: Elder accidentally swipes or presses system back button -> Launcher stays open
    androidx.activity.compose.BackHandler(enabled = true) {
        // No-op: CareTouch is the primary launcher home
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AmmaBackground),
        containerColor = AmmaBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Row with Logo and Settings Gear Button
                item(span = { GridItemSpan(4) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo (Clean, non-interactive)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AmmaRedBright.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = AmmaRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CareTouch",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmmaGreenBright,
                                letterSpacing = (-0.3).sp
                            )
                        }

                        // Right Side Controls: Sign In Button (if not signed in) + Settings Gear
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.authState !is AuthState.Authenticated) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AmmaSurfaceElevated)
                                        .bounceClick(scaleDown = 0.92f) { viewModel.openLoginSheet() }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = "Sign In",
                                        tint = AmmaGreenBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Sign In",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AmmaTextPrimary
                                    )
                                }
                            }

                            // Settings Gear Button -> Opens Admin Panel with Bouncy Micro-interaction
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmmaSurfaceElevated)
                                    .bounceClick(scaleDown = 0.88f) { viewModel.openAdminAuth() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Admin Settings",
                                    tint = AmmaTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Spoken Clock Card
                item(span = { GridItemSpan(4) }) {
                    SpokenClockCard(
                        status = uiState.status,
                        onTap = { viewModel.onClockTap() },
                        onLongClick = { viewModel.onClockLongClick() }
                    )
                }

                // Battery / Signal / Internet Indicators
                item(span = { GridItemSpan(4) }) {
                    StatusIndicatorRow(
                        status = uiState.status,
                        onBatteryTap = { viewModel.onBatteryTap() },
                        onSignalTap = { viewModel.onSignalTap() },
                        onInternetTap = { viewModel.onInternetTap() }
                    )
                }

                // Section title: పరిచయాలు (Contacts)
                item(span = { GridItemSpan(4) }) {
                    Text(
                        text = "పరిచయాలు (Contacts)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmmaTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Edge Case: Intuitive zero-contacts empty placeholder with quick-access to Settings
                if (uiState.contacts.isEmpty()) {
                    item(span = { GridItemSpan(4) }) {
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .bounceClick { viewModel.openAdminAuth() },
                            shape = RoundedCornerShape(20.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = AmmaSurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF2C2C2E))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = AmmaGreenBright,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "పరిచయాలు లేవు (No Contacts)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmmaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "సెట్టింగ్‌లను నొక్కి కొత్త వారిని జోడించండి",
                                    fontSize = 13.sp,
                                    color = AmmaTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // 4-Column Horizontal Contact Grid
                    items(uiState.contacts, key = { it.id }) { contact ->
                        ContactTile(
                            contact = contact,
                            onTap = { viewModel.onContactTap(contact) },
                            onLongClick = { viewModel.onContactLongClick(contact) }
                        )
                    }
                }
            }

            // Contact Action Selection Modal
            val currentCallState = uiState.callState
            if (currentCallState is CallState.ContactOptionsPicker) {
                ContactActionModal(
                    contact = currentCallState.contact,
                    onSelectTransport = { transport ->
                        viewModel.onSelectTransport(currentCallState.contact, transport)
                    },
                    onDismiss = { viewModel.dismissCallState() }
                )
            }

            // Fallback Dialog (No Internet -> Call Cellular)
            if (currentCallState is CallState.FallbackPrompt) {
                FallbackModal(
                    contact = currentCallState.contact,
                    onConfirmCellular = {
                        viewModel.onConfirmFallback(currentCallState.contact)
                    },
                    onCancel = { viewModel.dismissCallState() }
                )
            }

            // Admin Authentication PIN Pad
            if (uiState.showAdminAuth) {
                AdminPinDialog(
                    expectedPin = uiState.settings.adminPin,
                    onPinSuccess = {
                        viewModel.dismissAdminAuth()
                        onNavigateToAdmin()
                    },
                    onDismiss = { viewModel.dismissAdminAuth() }
                )
            }

            // First-Time Launch Caregiver Google Sign-In Sheet
            if (uiState.showInitialLogin) {
                CaregiverLoginSheet(
                    authState = uiState.authState,
                    onSignIn = { ctx -> viewModel.signInWithGoogle(ctx) },
                    onDismiss = { viewModel.dismissInitialLogin() }
                )
            }
        }
    }
}
