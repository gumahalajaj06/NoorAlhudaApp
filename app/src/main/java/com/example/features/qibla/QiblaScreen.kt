package com.example.features.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    state: QiblaUiState,
    onStartSensor: () -> Unit,
    onStopSensor: () -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current

    DisposableEffect(Unit) {
        onStartSensor()
        onDispose { onStopSensor() }
    }

    val needleAngle by animateFloatAsState(
        targetValue = state.needleRelativeAngle,
        label = "qibla_needle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NoorTopBar(
            title = "اتجاه القبلة",
            subtitle = "${state.cityName} • ${state.qibla.distanceKm.roundToInt()} كم إلى الكعبة",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Calibration or No-Sensor Notice
        if (!state.hasCompassSensor) {
            NoorGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = noorColors.gold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "مستشعر البوصلة غير متوفر على هذا الجهاز. يتم عرض زاوية القبلة رقمياً بالنسبة للشمال الحقيقي.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (state.needsCalibration) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = noorColors.gold.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "يرجى معايرة البوصلة بتحريك الهاتف على شكل رقم (8) في الهواء",
                    style = MaterialTheme.typography.labelLarge,
                    color = noorColors.gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Golden Circular Compass
        Box(
            modifier = Modifier
                .size(290.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val gold = noorColors.gold
            val emerald = noorColors.emeraldBright
            val dialBg = Color(0xFF0E203B)
            val aligned = state.hasCompassSensor && state.isAlignedWithQibla

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Outer golden ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF192F50), dialBg)
                    ),
                    radius = radius
                )
                drawCircle(
                    color = if (aligned) emerald else gold,
                    radius = radius - 4.dp.toPx(),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = gold.copy(alpha = 0.3f),
                    radius = radius * 0.78f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Degree tick marks rotated inversely with device azimuth if sensor available
                val dialRotation = if (state.hasCompassSensor) -state.deviceAzimuthDegrees else 0f
                rotate(degrees = dialRotation, pivot = center) {
                    for (deg in 0 until 360 step 15) {
                        val rad = Math.toRadians(deg.toDouble() - 90.0)
                        val isCardinal = deg % 90 == 0
                        val innerR = radius * if (isCardinal) 0.80f else 0.88f
                        val outerR = radius * 0.94f
                        drawLine(
                            color = if (deg == 0) gold else gold.copy(alpha = 0.45f),
                            start = Offset(
                                center.x + innerR * cos(rad).toFloat(),
                                center.y + innerR * sin(rad).toFloat()
                            ),
                            end = Offset(
                                center.x + outerR * cos(rad).toFloat(),
                                center.y + outerR * sin(rad).toFloat()
                            ),
                            strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx()
                        )
                    }
                }

                // Qibla Needle
                val pointerAngle = if (state.hasCompassSensor) needleAngle else state.qibla.bearingDegrees.toFloat()
                rotate(degrees = pointerAngle, pivot = center) {
                    val needlePath = Path().apply {
                        moveTo(center.x, center.y - radius * 0.72f)
                        lineTo(center.x - 14.dp.toPx(), center.y)
                        lineTo(center.x + 14.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = needlePath,
                        color = if (aligned) emerald else gold
                    )
                    val tailPath = Path().apply {
                        moveTo(center.x, center.y + radius * 0.45f)
                        lineTo(center.x - 10.dp.toPx(), center.y)
                        lineTo(center.x + 10.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = tailPath,
                        color = Color(0xFF43536B)
                    )
                }

                drawCircle(color = gold, radius = 10.dp.toPx(), center = center)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.hasCompassSensor && state.isAlignedWithQibla) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(noorColors.emerald.copy(alpha = 0.3f))
                    .border(1.dp, noorColors.emeraldBright, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = noorColors.emeraldBright)
                Spacer(modifier = Modifier.width(6.dp))
                Text("أنت متجه نحو القبلة الشريفة", color = Color(0xFFF7F5EF), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        NoorGoldCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("زاوية القبلة", style = MaterialTheme.typography.labelMedium, color = noorColors.mutedText)
                    Text(
                        text = "${state.qibla.bearingDegrees.roundToInt()}° (${state.qibla.cardinalDirectionArabic})",
                        style = MaterialTheme.typography.headlineMedium,
                        color = noorColors.gold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("اتجاه الهاتف", style = MaterialTheme.typography.labelMedium, color = noorColors.mutedText)
                    Text(
                        text = if (state.hasCompassSensor) "${state.deviceAzimuthDegrees.roundToInt()}°" else "رقمي فقط",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "حالة المستشعر: ${if (state.hasCompassSensor) state.accuracyLabel else "غير متوفر"}",
                style = MaterialTheme.typography.labelMedium,
                color = noorColors.mutedText
            )
        }
    }
}
