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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.amma.model.Contact
import com.example.amma.theme.AmmaAmberBright
import com.example.amma.theme.AmmaDimens
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaRedEmergency
import com.example.amma.theme.AmmaSurface
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaTextSizes

@Composable
fun FallbackModal(
    contact: Contact,
    onConfirmCellular: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AmmaDimens.CornerRadiusLarge)),
            shape = RoundedCornerShape(AmmaDimens.CornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = AmmaSurface),
            border = androidx.compose.foundation.BorderStroke(AmmaDimens.BorderMedium, AmmaAmberBright)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = AmmaAmberBright,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ఇంటర్నెట్ లేదు",
                        fontSize = AmmaTextSizes.ModalTitle,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmmaAmberBright
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${contact.displayName} కి సాధారణ ఫోన్ చేయాలా?",
                    fontSize = AmmaTextSizes.ContactName,
                    fontWeight = FontWeight.Bold,
                    color = AmmaTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Green YES Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
                            .background(Color(0xFF1B5E20))
                            .border(AmmaDimens.BorderMedium, AmmaGreenBright, RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
                            .clickable(onClick = onConfirmCellular)
                            .semantics {
                                role = Role.Button
                                contentDescription = "అవును, సాధారణ ఫోన్ చెయ్యి"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "అవును", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            Text(text = "(YES - CALL)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA5D6A7))
                        }
                    }

                    // Red NO Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
                            .background(Color(0xFF5C1D1D))
                            .border(AmmaDimens.BorderMedium, AmmaRedEmergency, RoundedCornerShape(AmmaDimens.CornerRadiusMedium))
                            .clickable(onClick = onCancel)
                            .semantics {
                                role = Role.Button
                                contentDescription = "వద్దు, రద్దు చేయి"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "వద్దు", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            Text(text = "(NO - CANCEL)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCDD2))
                        }
                    }
                }
            }
        }
    }
}
