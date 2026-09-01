package com.example.amma.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amma.model.SystemStatus
import com.example.amma.theme.AmmaAmberBright
import com.example.amma.theme.AmmaBorder
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaSurface
import com.example.amma.theme.AmmaTextPrimary
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpokenClockCard(
    status: SystemStatus,
    onTap: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isDayTime = hour in 6..17

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaker_pulse"
    )

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "clock_press_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap,
                onLongClick = onLongClick
            )
            .semantics {
                role = Role.Button
                contentDescription = "సమయం మరియు తేదీ వినడానికి నొక్కండి."
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AmmaSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isPressed) AmmaGreenBright else AmmaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sun / Moon Day-Night Indicator
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDayTime)
                            Brush.radialGradient(listOf(AmmaAmberBright.copy(alpha = 0.25f), Color.Transparent))
                        else
                            Brush.radialGradient(listOf(Color(0xFF5E5CE6).copy(alpha = 0.25f), Color.Transparent))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDayTime) Icons.Rounded.WbSunny else Icons.Rounded.NightlightRound,
                    contentDescription = null,
                    tint = if (isDayTime) AmmaAmberBright else Color(0xFF7D7AFF),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Big Bold Digital Time
            Text(
                text = status.formattedTime,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AmmaTextPrimary,
                letterSpacing = (-0.5).sp
            )

            // Visual Speaker Tap Cue
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(AmmaGreenBright.copy(alpha = 0.12f))
                    .border(1.dp, AmmaGreenBright.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = AmmaGreenBright,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
