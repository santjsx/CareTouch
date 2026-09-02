package com.example.amma.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.amma.R
import com.example.amma.cloud.auth.AuthState
import com.example.amma.theme.AmmaAmberBright
import com.example.amma.theme.AmmaBorder
import com.example.amma.theme.AmmaBorderFocused
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaRedEmergency
import com.example.amma.theme.AmmaSurface
import com.example.amma.theme.AmmaSurfaceElevated
import com.example.amma.theme.AmmaSurfaceVariant
import com.example.amma.theme.AmmaTextMuted
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaTextSecondary

/**
 * Modern, Enterprise-Grade Caregiver Cloud Setup & Google Sign-In Dialog.
 *
 * Adheres strictly to Google Identity Branding Guidelines and modern dark-mode aesthetics.
 * Provides frictionless 1-tap Google Authentication to activate Firestore sync and Google Drive photo storage.
 */
@Composable
fun CaregiverLoginSheet(
    authState: AuthState,
    onSignIn: (Context) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            if (authState !is AuthState.Loading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = authState !is AuthState.Loading,
            dismissOnClickOutside = authState !is AuthState.Loading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AmmaSurfaceElevated),
            border = BorderStroke(1.dp, Color(0xFF333A45))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Halo & Cloud Icon Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AmmaGreenBright.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF18231C))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        AmmaGreenBright.copy(alpha = 0.2f),
                                        Color(0xFF131915)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudSync,
                            contentDescription = null,
                            tint = AmmaGreenBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = "Caregiver Cloud Setup",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmmaTextPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.4).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in with Google to protect and automatically sync elder contacts and photos across family devices.",
                    fontSize = 13.5.sp,
                    color = AmmaTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Group Card (Apple & Material 3 Settings Style)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AmmaSurface)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Rounded.Sync,
                        iconTint = AmmaGreenBright,
                        iconBg = AmmaGreenBright.copy(alpha = 0.14f),
                        title = "Automatic Realtime Sync",
                        desc = "Contacts sync instantly to the elder's phone"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 48.dp, top = 10.dp, bottom = 10.dp),
                        thickness = 0.8.dp,
                        color = Color(0xFF262C36)
                    )

                    FeatureRow(
                        icon = Icons.Rounded.Security,
                        iconTint = AmmaBorderFocused,
                        iconBg = AmmaBorderFocused.copy(alpha = 0.14f),
                        title = "Encrypted Cloud Backup",
                        desc = "Safe 1-click restore if the device is lost or changed"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 48.dp, top = 10.dp, bottom = 10.dp),
                        thickness = 0.8.dp,
                        color = Color(0xFF262C36)
                    )

                    FeatureRow(
                        icon = Icons.Rounded.PermMedia,
                        iconTint = AmmaAmberBright,
                        iconBg = AmmaAmberBright.copy(alpha = 0.14f),
                        title = "Google Drive Photo Storage",
                        desc = "Elder photos stored privately on your Google Drive"
                    )
                }

                // Error Message Card
                AnimatedVisibility(
                    visible = authState is AuthState.Error,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (authState is AuthState.Error) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF381A1A))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = AmmaRedEmergency,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authState.message,
                                color = Color(0xFFFFB4AB),
                                fontSize = 12.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Standard Google Sign-In Button (Official Google Identity Specification)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .bounceClick(
                            enabled = authState !is AuthState.Loading,
                            scaleDown = 0.96f
                        ) {
                            onSignIn(context)
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFDADCE0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF4285F4),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Connecting to Google...",
                                color = Color(0xFF3C4043),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google",
                                color = Color(0xFF1F1F1F),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                letterSpacing = 0.1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dismiss / Continue Offline Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .bounceClick(
                            enabled = authState !is AuthState.Loading,
                            scaleDown = 0.96f
                        ) {
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Set Up Later (Offline Mode)",
                        color = AmmaTextSecondary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Trust / Privacy Reassurance Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = AmmaTextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Private · Official Google OAuth 2.0",
                        fontSize = 11.sp,
                        color = AmmaTextMuted,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = AmmaTextPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = desc,
                fontSize = 11.5.sp,
                color = AmmaTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
