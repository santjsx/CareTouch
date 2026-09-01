package com.example.amma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amma.model.Contact
import com.example.amma.theme.AmmaDimens
import com.example.amma.theme.AmmaTextSizes
import kotlinx.coroutines.delay

@Composable
fun EmergencyTile(
    emergencyContact: Contact?,
    onEmergencyTriggered: (Contact) -> Unit,
    onHoldTick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (emergencyContact == null) return

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val totalSteps = 20
            val stepTime = 50L // 50ms * 20 = 1000ms (1 second)
            for (step in 1..totalSteps) {
                if (!isHolding) break
                delay(stepTime)
                holdProgress = step.toFloat() / totalSteps.toFloat()
                if (step % 4 == 0) {
                    onHoldTick()
                }
            }
            if (isHolding && holdProgress >= 1f) {
                onEmergencyTriggered(emergencyContact)
                isHolding = false
                holdProgress = 0f
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AmmaDimens.EmergencyButtonHeight)
            .clip(RoundedCornerShape(AmmaDimens.CornerRadiusLarge))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF8B0000),
                        Color(0xFFB71C1C),
                        Color(0xFFD32F2F)
                    )
                )
            )
            .border(
                AmmaDimens.BorderMedium,
                Color(0xFFFF8A80).copy(alpha = 0.8f),
                RoundedCornerShape(AmmaDimens.CornerRadiusLarge)
            )
            .pointerInput(emergencyContact) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            }
            .padding(horizontal = 20.dp)
            .semantics {
                role = Role.Button
                contentDescription = "అత్యవసర సహాయ బటన్. ${emergencyContact.displayName} కి సహాయం కోసం ఫోన్ చేయడానికి 1 సెకను నొక్కి పట్టుకోండి."
            },
        contentAlignment = Alignment.Center
    ) {
        // Progress overlay when holding
        if (isHolding && holdProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(AmmaDimens.CornerRadiusLarge))
                    .background(Color.White.copy(alpha = 0.2f * holdProgress))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "అత్యవసరం (HELP)",
                        fontSize = AmmaTextSizes.ButtonLabel,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${emergencyContact.displayName} కి కాల్ చేయడానికి నొక్కి పట్టుకోండి",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFCDD2)
                    )
                }
            }

            if (isHolding) {
                CircularProgressIndicator(
                    progress = { holdProgress },
                    modifier = Modifier.width(36.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
