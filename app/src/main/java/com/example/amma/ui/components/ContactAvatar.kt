package com.example.amma.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.util.PhotoStorageHelper

@Composable
fun ContactAvatar(
    contact: Contact,
    size: Dp = 80.dp,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = AmmaGreenBright,
    isEditable: Boolean = false,
    onEditPhotoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(contact.photoUri) {
        PhotoStorageHelper.loadBitmap(contact.photoUri)
    }

    val relationshipIcon: ImageVector = when {
        contact.displayName.contains("Doctor", ignoreCase = true) || contact.relationship.contains("Doctor", ignoreCase = true) -> Icons.Rounded.LocalHospital
        contact.relationship.contains("Husband", ignoreCase = true) || contact.relationship.contains("భర్త") -> Icons.Rounded.Favorite
        contact.relationship.contains("Daughter", ignoreCase = true) || contact.relationship.contains("కూతురు") -> Icons.Rounded.Face
        contact.relationship.contains("Son", ignoreCase = true) || contact.relationship.contains("కొడుకు") -> Icons.Rounded.Person
        else -> Icons.Rounded.Person
    }

    val avatarGradient = when {
        contact.isEmergencyContact -> listOf(Color(0xFF8B0000), Color(0xFFC62828))
        contact.primaryTransport == CallTransport.WHATSAPP_VIDEO -> listOf(Color(0xFF0F5132), Color(0xFF198754))
        contact.relationship.contains("Husband") || contact.relationship.contains("భర్త") -> listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
        else -> listOf(Color(0xFF1A365D), Color(0xFF2B6CB0))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(Brush.linearGradient(avatarGradient))
                .border(2.5.dp, borderColor.copy(alpha = 0.85f), RoundedCornerShape(cornerRadius))
                .then(
                    if (isEditable && onEditPhotoClick != null) {
                        Modifier.clickable(onClick = onEditPhotoClick)
                    } else {
                        Modifier
                    }
                ),
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
                    modifier = Modifier.size(size * 0.48f)
                )
            }
        }

        // Camera / Edit Overlay Badge in Edit Mode
        if (isEditable && onEditPhotoClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(size * 0.38f)
                    .clip(CircleShape)
                    .background(AmmaGreenBright)
                    .border(2.dp, Color.Black, CircleShape)
                    .clickable(onClick = onEditPhotoClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddAPhoto,
                    contentDescription = "Upload Photo",
                    tint = Color.Black,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
    }
}
