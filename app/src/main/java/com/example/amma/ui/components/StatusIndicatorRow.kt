package com.example.amma.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.AirplanemodeActive
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amma.model.SignalGrade
import com.example.amma.model.SystemStatus
import com.example.amma.theme.AmmaTextPrimary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra-Smooth Analog Telemetry Cluster with Real Mechanical Needle Physics.
 *
 * Physics & Motion:
 * - Damped mechanical spring oscillation mimicking galvanometer hairspring physics.
 * - Smooth color blending transitions across telemetry state changes.
 * - Electric subtle pulse effect on charging.
 * - Micro-spring tactile feedback on tap.
 */
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
        // =========================================================================
        // 1. BATTERY COMPACT ANALOG GAUGE
        // =========================================================================
        val percent = status.batteryPercent.coerceIn(0, 100)
        val (batteryTargetColor, batteryLabel) = when {
            status.isCharging -> Pair(Color(0xFF00E5FF), "Charging") // Electric Cyan
            percent >= 80 -> Pair(Color(0xFF30D158), "Full")        // Emerald Neon
            percent >= 50 -> Pair(Color(0xFF76FF03), "Good")        // Lime
            percent >= 25 -> Pair(Color(0xFFFF9F0A), "Medium")      // Amber
            percent >= 15 -> Pair(Color(0xFFFF5722), "Low")         // Orange
            else -> Pair(Color(0xFFFF3B30), "Critical")             // Crimson Red
        }

        val animatedBatteryColor by animateColorAsState(
            targetValue = batteryTargetColor,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            label = "battery_color"
        )

        CompactAnalogTile(
            progress = percent / 100f,
            accentColor = animatedBatteryColor,
            primaryText = "$percent%",
            subLabel = batteryLabel,
            leadingIcon = if (status.isCharging) Icons.Rounded.Bolt else null,
            isPulsing = status.isCharging,
            contentDescription = "బ్యాటరీ: $percent శాతం. $batteryLabel. వినడానికి నొక్కండి.",
            onClick = onBatteryTap,
            modifier = Modifier.weight(1f)
        )

        // =========================================================================
        // 2. CELLULAR SIGNAL COMPACT ANALOG TACHOMETER
        // =========================================================================
        val (signalTargetColor, activeBars, signalProgress, signalValue, signalLabel, signalIcon) = when {
            status.signalGrade == SignalGrade.AIRPLANE_MODE ->
                SignalData(Color(0xFFFF9F0A), 0, 0f, "Airplane", "Flight Mode", Icons.Rounded.AirplanemodeActive)
            !status.isSimAvailable ->
                SignalData(Color(0xFFFF3B30), 0, 0f, "No SIM", "No Card", Icons.Rounded.SignalCellularConnectedNoInternet0Bar)
            status.signalGrade == SignalGrade.NO_SIGNAL ->
                SignalData(Color(0xFFFF3B30), 0, 0f, "0 Bars", "No Signal", Icons.Rounded.CellTower)
            status.signalGrade == SignalGrade.POOR ->
                SignalData(Color(0xFFFF9F0A), 1, 0.25f, "1/4", "Weak", Icons.Rounded.CellTower)
            status.signalGrade == SignalGrade.GOOD ->
                SignalData(Color(0xFF00E5FF), 3, 0.75f, "3/4", "Good", Icons.Rounded.CellTower)
            else ->
                SignalData(Color(0xFF0A84FF), 4, 1.0f, "4/4", "Strong", Icons.Rounded.CellTower) // Azure Blue
        }

        val animatedSignalColor by animateColorAsState(
            targetValue = signalTargetColor,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            label = "signal_color"
        )

        CompactAnalogTile(
            progress = signalProgress,
            accentColor = animatedSignalColor,
            primaryText = signalValue,
            subLabel = signalLabel,
            leadingIcon = signalIcon,
            isPulsing = false,
            contentDescription = "సిగ్నల్: $activeBars గీతలు. $signalLabel. వినడానికి నొక్కండి.",
            onClick = onSignalTap,
            modifier = Modifier.weight(1f)
        )

        // =========================================================================
        // 3. INTERNET COMPACT ANALOG SPEEDOMETER
        // =========================================================================
        val (netTargetColor, isConnected, netProgress, netTitle, netLabel, netIcon) = if (status.isInternetAvailable) {
            if (status.isWifiConnected) {
                // Electric Violet for Wi-Fi Link
                NetworkData(Color(0xFFBF5AF2), true, 1.0f, "Wi-Fi", "Online", Icons.Rounded.Wifi)
            } else {
                // Emerald Green for Cellular Mobile Data
                NetworkData(Color(0xFF30D158), true, 0.88f, "Mobile", "Online", Icons.Rounded.Language)
            }
        } else {
            // Neon Crimson for Disconnected
            NetworkData(Color(0xFFFF453A), false, 0f, "Offline", "No Internet", Icons.Rounded.WifiOff)
        }

        val animatedNetColor by animateColorAsState(
            targetValue = netTargetColor,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            label = "net_color"
        )

        CompactAnalogTile(
            progress = netProgress,
            accentColor = animatedNetColor,
            primaryText = netTitle,
            subLabel = netLabel,
            leadingIcon = netIcon,
            isPulsing = false,
            contentDescription = if (isConnected) "నెట్ కనెక్ట్ అయింది. $netTitle." else "నెట్ లేదు.",
            onClick = onInternetTap,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Compact Glassmorphic Pod with Adaptive Ambient Highlights.
 */
@Composable
private fun CompactAnalogTile(
    progress: Float,
    accentColor: Color,
    primaryText: String,
    subLabel: String,
    leadingIcon: ImageVector?,
    isPulsing: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.14f),
                        Color(0xFF161920),
                        Color(0xFF0F1116)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.55f),
                            Color(0xFF282E3A)
                        )
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .bounceClick(scaleDown = 0.94f, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Compact Analog Gauge with Real Mechanical Needle Physics
            CompactMiniDial(
                progress = progress,
                color = accentColor,
                isPulsing = isPulsing,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(width = 40.dp, height = 22.dp)
            )

            // Primary Readout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = primaryText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmmaTextPrimary,
                    letterSpacing = (-0.2).sp
                )
            }

            // Subtitle Badge
            Text(
                text = subLabel,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                letterSpacing = 0.1.sp,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
    }
}

/**
 * High-Precision Galvanometer Gauge with Mechanical Spring Physics & Damped Inertia.
 */
@Composable
private fun CompactMiniDial(
    progress: Float,
    color: Color,
    isPulsing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // True mechanical needle physics: Damped hairspring with realistic inertia & micro-settlement
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.68f, // Authentic physical needle overshoot and subtle settling
            stiffness = 65f       // Realistic mechanical needle speed
        ),
        label = "mechanical_needle_physics"
    )

    // Gentle electric charging pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dial geometry: compact, centered arch
        val pivot = Offset(w / 2f, h)
        val radius = (w / 2f) - 3.5.dp.toPx()
        val startAngle = 160f
        val sweepAngle = 220f
        val trackWidth = 2.4.dp.toPx()

        val arcRect = Size(radius * 2, radius * 2)
        val topLeft = Offset(pivot.x - radius, pivot.y - radius)

        // 1. Radial Calibration Ticks (5 Divisions: 0%, 25%, 50%, 75%, 100%)
        val tickCount = 5
        for (i in 0 until tickCount) {
            val fraction = i.toFloat() / (tickCount - 1)
            val angleRad = Math.toRadians((startAngle + fraction * sweepAngle).toDouble())
            val isPassed = fraction <= (animatedProgress + 0.02f)
            val isMajor = (i == 0 || i == tickCount - 1 || i == 2)

            val innerR = radius + 1.2.dp.toPx()
            val outerR = radius + (if (isMajor) 3.5.dp.toPx() else 2.2.dp.toPx())

            val p1 = Offset(
                pivot.x + innerR * cos(angleRad).toFloat(),
                pivot.y + innerR * sin(angleRad).toFloat()
            )
            val p2 = Offset(
                pivot.x + outerR * cos(angleRad).toFloat(),
                pivot.y + outerR * sin(angleRad).toFloat()
            )

            drawLine(
                color = if (isPassed) color.copy(alpha = pulseAlpha) else Color(0xFF2B323F),
                start = p1,
                end = p2,
                strokeWidth = if (isMajor) 1.4.dp.toPx() else 0.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 2. Inactive Base Track
        drawArc(
            color = Color(0xFF1E232E),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = trackWidth, cap = StrokeCap.Round)
        )

        // 3. Dynamic Active Colored Sweep Track
        val currentSweep = animatedProgress * sweepAngle
        if (currentSweep > 0) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        color.copy(alpha = 0.4f * pulseAlpha),
                        color.copy(alpha = pulseAlpha)
                    ),
                    center = pivot
                ),
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcRect,
                style = Stroke(width = trackWidth, cap = StrokeCap.Round)
            )
        }

        // 4. Live Damped Mechanical Needle
        val needleAngleRad = Math.toRadians((startAngle + currentSweep).toDouble())
        val needleLength = radius - 0.5.dp.toPx()
        val needleTip = Offset(
            pivot.x + needleLength * cos(needleAngleRad).toFloat(),
            pivot.y + needleLength * sin(needleAngleRad).toFloat()
        )

        // Needle Spine (Clean White with Soft Taper)
        drawLine(
            color = Color.White.copy(alpha = pulseAlpha),
            start = pivot,
            end = needleTip,
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Illuminated Tip Dot
        drawCircle(
            color = color.copy(alpha = pulseAlpha),
            radius = 1.8.dp.toPx(),
            center = needleTip
        )

        // 5. Metallic Chrome Center Pivot Hub
        drawCircle(
            color = Color(0xFF12151B),
            radius = 2.8.dp.toPx(),
            center = pivot
        )
        drawCircle(
            color = color.copy(alpha = pulseAlpha),
            radius = 1.4.dp.toPx(),
            center = pivot
        )
    }
}

private data class SignalData(
    val color: Color,
    val bars: Int,
    val progress: Float,
    val value: String,
    val label: String,
    val icon: ImageVector
)

private data class NetworkData(
    val color: Color,
    val isConnected: Boolean,
    val progress: Float,
    val title: String,
    val label: String,
    val icon: ImageVector
)
