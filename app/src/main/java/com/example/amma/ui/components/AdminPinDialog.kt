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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.amma.theme.AmmaBorder
import com.example.amma.theme.AmmaDimens
import com.example.amma.theme.AmmaGreenBright
import com.example.amma.theme.AmmaRedEmergency
import com.example.amma.theme.AmmaSurface
import com.example.amma.theme.AmmaSurfaceElevated
import com.example.amma.theme.AmmaTextPrimary
import com.example.amma.theme.AmmaTextSecondary

@Composable
fun AdminPinDialog(
    expectedPin: String,
    onPinSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var failedAttempts by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var lockoutSeconds by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // Countdown ticker for lockout
    androidx.compose.runtime.LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            lockoutSeconds -= 1
        }
    }

    val isLockedOut = lockoutSeconds > 0

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = if (isLockedOut) AmmaRedEmergency else AmmaGreenBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Admin Access",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmmaTextPrimary
                    )
                }

                Text(
                    text = if (isLockedOut) "Security Lockout Active" else "Enter 4-digit PIN (Default: 1234)",
                    fontSize = 14.sp,
                    color = if (isLockedOut) AmmaRedEmergency else AmmaTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = enteredPin.length > i
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isError || isLockedOut) AmmaRedEmergency
                                    else if (isFilled) AmmaGreenBright
                                    else AmmaSurfaceElevated
                                )
                                .border(
                                    2.dp,
                                    if (isError || isLockedOut) AmmaRedEmergency else AmmaBorder,
                                    CircleShape
                                )
                        )
                    }
                }

                if (isLockedOut) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Too many attempts. Try again in ${lockoutSeconds}s",
                        fontSize = 13.sp,
                        color = AmmaRedEmergency,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Incorrect PIN (${5 - failedAttempts} attempts remaining)",
                        fontSize = 13.sp,
                        color = AmmaRedEmergency,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Keypad (1 to 9, 0, backspace)
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "BACKSPACE")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                text = key,
                                onClick = {
                                    if (isLockedOut) return@KeypadButton
                                    isError = false
                                    when (key) {
                                        "C" -> enteredPin = ""
                                        "BACKSPACE" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                val newPin = enteredPin + key
                                                enteredPin = newPin
                                                if (newPin.length == 4) {
                                                    if (newPin == expectedPin) {
                                                        failedAttempts = 0
                                                        onPinSuccess()
                                                    } else {
                                                        failedAttempts += 1
                                                        if (failedAttempts >= 5) {
                                                            lockoutSeconds = 30
                                                            failedAttempts = 0
                                                        }
                                                        isError = true
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    color = AmmaTextSecondary,
                    modifier = Modifier
                        .bounceClick(scaleDown = 0.92f, onClick = onDismiss)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(AmmaSurfaceElevated)
            .border(AmmaDimens.BorderThin, AmmaBorder, CircleShape)
            .bounceClick(scaleDown = 0.86f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (text == "BACKSPACE") {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = "Backspace",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
