package com.example.amma.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amma.model.BatteryLevelGrade
import com.example.amma.model.SignalGrade
import com.example.amma.model.SystemStatus
import com.example.amma.theme.AmmaSurface

@Composable
fun StatusIndicatorRow(
    status: SystemStatus,
    onBatteryTap: () -> Unit,
    onSignalTap: () -> Unit,
    onInternetTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Graphical Battery Gauge Capsule
        val batteryColor = when (status.batteryGrade) {
            BatteryLevelGrade.EXCELLENT, BatteryLevelGrade.GOOD, BatteryLevelGrade.CHARGING -> Color(0xFF30D158)
            BatteryLevelGrade.MEDIUM -> Color(0xFFFF9F0A)
            BatteryLevelGrade.LOW, BatteryLevelGrade.CRITICAL -> Color(0xFFFF453A)
        }

        StatusCapsule(
            onClick = onBatteryTap,
            contentDescription = "బ్యాటరీ: ${status.batteryPercent} శాతం. వినడానికి నొక్కండి.",
            borderColor = batteryColor.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        ) {
            GraphicalBatteryGauge(
                percent = status.batteryPercent,
                isCharging = status.isCharging,
                color = batteryColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${status.batteryPercent}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = batteryColor
            )
        }

        // 2. Graphical 4-Bar Stepped Signal Capsule
        val (signalColor, activeBars) = when {
            !status.isSimAvailable || status.signalGrade == SignalGrade.NO_SIGNAL || status.signalGrade == SignalGrade.AIRPLANE_MODE ->
                Pair(Color(0xFFFF453A), 0)
            status.signalGrade == SignalGrade.POOR ->
                Pair(Color(0xFFFF9F0A), 1)
            status.signalGrade == SignalGrade.GOOD ->
                Pair(Color(0xFF30D158), 3)
            else ->
                Pair(Color(0xFF30D158), 4)
        }

        StatusCapsule(
            onClick = onSignalTap,
            contentDescription = "సిగ్నల్: $activeBars గీతలు. వినడానికి నొక్కండి.",
            borderColor = signalColor.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        ) {
            GraphicalSignalTower(
                activeBars = activeBars,
                color = signalColor
            )
        }

        // 3. Graphical Internet / Wifi Wave Capsule
        val (netColor, isConnected) = if (status.isInternetAvailable) {
            Pair(if (status.isWifiConnected) Color(0xFF0A84FF) else Color(0xFF30D158), true)
        } else {
            Pair(Color(0xFFFF453A), false)
        }

        StatusCapsule(
            onClick = onInternetTap,
            contentDescription = if (isConnected) "నెట్ కనెక్ట్ అయింది." else "నెట్ లేదు.",
            borderColor = netColor.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        ) {
            GraphicalNetIndicator(
                isOnline = isConnected,
                isWifi = status.isWifiConnected,
                color = netColor
            )
        }
    }
}

@Composable
private fun StatusCapsule(
    onClick: () -> Unit,
    contentDescription: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AmmaSurface)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .bounceClick(scaleDown = 0.90f, onClick = onClick)
            .padding(horizontal = 8.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * Visual Battery Level Graphic: Liquid fill inside horizontal shell with charging bolt
 */
@Composable
private fun GraphicalBatteryGauge(
    percent: Int,
    isCharging: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 28.dp, height = 15.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = 1.5.dp.toPx()

            // Outer Shell
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, 0f),
                size = Size(w - 3.dp.toPx(), h),
                cornerRadius = CornerRadius(3.5.dp.toPx()),
                style = Stroke(width = stroke)
            )

            // Positive Terminal Nipple
            drawRoundRect(
                color = color,
                topLeft = Offset(w - 2.5.dp.toPx(), h * 0.3f),
                size = Size(2.5.dp.toPx(), h * 0.4f),
                cornerRadius = CornerRadius(1.dp.toPx()),
                style = Fill
            )

            // Inner Liquid Fill
            val fillPadding = stroke + 1.dp.toPx()
            val maxFillWidth = w - 3.dp.toPx() - (fillPadding * 2)
            val currentFillWidth = (maxFillWidth * (percent.coerceIn(0, 100) / 100f)).coerceAtLeast(2.dp.toPx())

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(color.copy(alpha = 0.8f), color)
                ),
                topLeft = Offset(fillPadding, fillPadding),
                size = Size(currentFillWidth, h - (fillPadding * 2)),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Fill
            )
        }

        if (isCharging) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * Visual 4-Bar Cellular Graphic: Stepped height bars
 */
@Composable
private fun GraphicalSignalTower(
    activeBars: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val heights = listOf(6.dp, 10.dp, 14.dp, 18.dp)
        for (i in 0..3) {
            val isActive = i < activeBars
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .height(heights[i])
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (isActive) color else Color(0xFF39393D))
            )
        }
    }
}

/**
 * Visual Internet & Data Graphic: Active Wifi Waves or Globe with pulsating status dot
 */
@Composable
private fun GraphicalNetIndicator(
    isOnline: Boolean,
    isWifi: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_net")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "net_pulse"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (!isOnline) {
                Icons.Rounded.WifiOff
            } else if (isWifi) {
                Icons.Rounded.Wifi
            } else {
                Icons.Rounded.Language
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        // Glowing live indicator dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .scale(if (isOnline) dotScale else 1.0f)
                .clip(CircleShape)
                .background(color)
        )
    }
}
