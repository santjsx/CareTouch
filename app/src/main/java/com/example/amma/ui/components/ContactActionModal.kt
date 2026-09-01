package com.example.amma.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.theme.AmmaBorder
import com.example.amma.theme.AmmaDimens
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaRedEmergency
import com.example.amma.theme.AmmaSurface
import com.example.amma.theme.AmmaSurfaceElevated
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaTextSecondary
import com.example.amma.theme.AmmaTextSizes
import com.example.amma.theme.AmmaWhatsAppGreen

@Composable
fun ContactActionModal(
    contact: Contact,
    onSelectTransport: (CallTransport) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AmmaDimens.CornerRadiusLarge)),
            shape = RoundedCornerShape(AmmaDimens.CornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = AmmaSurface),
            border = androidx.compose.foundation.BorderStroke(AmmaDimens.BorderMedium, AmmaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${contact.displayName} (${contact.relationship})",
                    fontSize = AmmaTextSizes.ModalTitle,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmmaTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ఏ విధంగా ఫోన్ చేయాలి?",
                    fontSize = AmmaTextSizes.Date,
                    fontWeight = FontWeight.SemiBold,
                    color = AmmaGreenBright
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Normal Phone Call
                ActionButtonRow(
                    icon = Icons.Rounded.Call,
                    title = "సాధారణ ఫోన్",
                    subtitle = "Phone Call",
                    color = AmmaGreenBright,
                    onClick = { onSelectTransport(CallTransport.CELLULAR) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // WhatsApp Audio Call
                ActionButtonRow(
                    icon = Icons.Rounded.HeadsetMic,
                    title = "వాట్సాప్ ఆడియో",
                    subtitle = "WhatsApp Audio",
                    color = AmmaWhatsAppGreen,
                    onClick = { onSelectTransport(CallTransport.WHATSAPP_AUDIO) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // WhatsApp Video Call
                ActionButtonRow(
                    icon = Icons.Rounded.Videocam,
                    title = "వాట్సాప్ వీడియో",
                    subtitle = "WhatsApp Video",
                    color = AmmaWhatsAppGreen,
                    onClick = { onSelectTransport(CallTransport.WHATSAPP_VIDEO) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
                        .background(AmmaSurfaceElevated)
                        .border(AmmaDimens.BorderThin, AmmaBorder, RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = AmmaRedEmergency,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "రద్దు (Close)",
                            fontSize = AmmaTextSizes.StatusHeader,
                            fontWeight = FontWeight.Bold,
                            color = AmmaRedEmergency
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
            .background(AmmaSurfaceElevated)
            .border(AmmaDimens.BorderThin, color.copy(alpha = 0.8f), RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .semantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = AmmaTextSizes.ButtonLabel,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AmmaTextSecondary
                )
            }
        }
    }
}
