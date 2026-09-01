package com.example.amma.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaWhatsAppGreen
import com.example.amma.util.PhotoStorageHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactTile(
    contact: Contact,
    onTap: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "tile_scale"
    )
    val borderWidth by animateFloatAsState(
        targetValue = if (isPressed) 3.5f else 2.0f,
        label = "border_width"
    )

    val transportColor: Color = when (contact.primaryTransport) {
        CallTransport.CELLULAR -> AmmaGreenBright
        CallTransport.WHATSAPP_AUDIO -> AmmaWhatsAppGreen
        CallTransport.WHATSAPP_VIDEO -> AmmaWhatsAppGreen
    }

    val bitmap = remember(contact.photoUri) {
        PhotoStorageHelper.loadBitmap(contact.photoUri)
    }

    val relationshipIcon: ImageVector = when {
        contact.displayName.contains("Doctor", ignoreCase = true) || contact.displayName.contains("వైద్యు") -> Icons.Rounded.LocalHospital
        contact.isEmergencyContact -> Icons.Rounded.Favorite
        else -> Icons.Rounded.Person
    }

    val avatarGradient = when {
        contact.isEmergencyContact -> listOf(Color(0xFF8B0000), Color(0xFFC62828))
        contact.primaryTransport == CallTransport.WHATSAPP_VIDEO -> listOf(Color(0xFF0F5132), Color(0xFF198754))
        else -> listOf(Color(0xFF1A365D), Color(0xFF2B6CB0))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Full Photo Squircle Card with Spring Tactile Physics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap,
                    onLongClick = onLongClick
                )
                .semantics {
                    role = Role.Button
                    contentDescription = "${contact.displayName}. ఫోన్ చేయడానికి నొక్కండి."
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(borderWidth.dp, if (isPressed) Color.White else transportColor.copy(alpha = 0.85f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(avatarGradient)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = contact.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = relationshipIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Minimal, single-row, non-clipped name below the photo card
        Text(
            text = contact.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AmmaTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
