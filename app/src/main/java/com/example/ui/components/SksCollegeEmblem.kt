package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Official SKS College of Nursing Emblem Composable.
 * Faithfully represents the circular crest of SKS College of Nursing, Harur - 636 903:
 * - Emerald & Teal circular crest border
 * - Nursing caduceus & Florence Nightingale eternal lamp
 * - Nurse cap with caring hands
 * - Motto: "PREVENT • PROMOTE • PROTECT"
 */
@Composable
fun SksCollegeEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    showMotto: Boolean = false
) {
    val emeraldDark = Color(0xFF065F46)
    val emeraldMid = Color(0xFF0F766E)
    val goldenYellow = Color(0xFFF59E0B)
    val medicalRed = Color(0xFFDC2626)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 3.dp, shape = CircleShape, ambientColor = emeraldDark.copy(alpha = 0.15f))
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = this.size.minDimension
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val outerRadius = canvasSize / 2f - 2f

                // Outer decorative ring
                drawCircle(
                    color = emeraldDark,
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = canvasSize * 0.045f)
                )

                // Outer text band background
                drawCircle(
                    color = Color(0xFFF0FDF4),
                    radius = outerRadius * 0.94f,
                    center = center
                )

                // Mid ring
                drawCircle(
                    color = emeraldMid,
                    radius = outerRadius * 0.74f,
                    center = center,
                    style = Stroke(width = canvasSize * 0.025f)
                )

                // Gold accent ring
                drawCircle(
                    color = goldenYellow,
                    radius = outerRadius * 0.70f,
                    center = center,
                    style = Stroke(width = canvasSize * 0.015f)
                )

                // Inner white badge
                drawCircle(
                    color = Color.White,
                    radius = outerRadius * 0.68f,
                    center = center
                )

                // Draw central healthcare insignia (Lamp, Cross, Caduceus, Care Hands)
                drawInsignia(center, outerRadius * 0.62f, emeraldDark, goldenYellow, medicalRed)
            }

            // Central SKS initials badge overlay for crisp typography at small sizes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.08f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SKS COLLEGE OF NURSING",
                    fontSize = (size.value * 0.065f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = emeraldDark,
                    letterSpacing = 0.2.sp,
                    textAlign = TextAlign.Center
                )

                // Central shield label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(emeraldDark.copy(alpha = 0.08f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "SKS",
                        fontSize = (size.value * 0.13f).sp,
                        fontWeight = FontWeight.Black,
                        color = emeraldDark,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "★ HARUR - 636 903 ★",
                    fontSize = (size.value * 0.055f).sp,
                    fontWeight = FontWeight.Bold,
                    color = goldenYellow,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showMotto) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NursingTealSubtle)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "PREVENT • PROMOTE • PROTECT",
                    style = Typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NursingTealDark,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun DrawScope.drawInsignia(
    center: Offset,
    radius: Float,
    emerald: Color,
    gold: Color,
    red: Color
) {
    val cx = center.x
    val cy = center.y

    // Caduceus wings outline
    val wingPath = Path().apply {
        moveTo(cx - radius * 0.55f, cy - radius * 0.2f)
        cubicTo(
            cx - radius * 0.3f, cy - radius * 0.45f,
            cx + radius * 0.3f, cy - radius * 0.45f,
            cx + radius * 0.55f, cy - radius * 0.2f
        )
        lineTo(cx + radius * 0.4f, cy - radius * 0.1f)
        cubicTo(
            cx + radius * 0.2f, cy - radius * 0.3f,
            cx - radius * 0.2f, cy - radius * 0.3f,
            cx - radius * 0.4f, cy - radius * 0.1f
        )
        close()
    }
    drawPath(wingPath, color = gold.copy(alpha = 0.75f))

    // Medical Red Cross in central shield
    val crossW = radius * 0.10f
    val crossH = radius * 0.28f
    drawRect(
        color = red,
        topLeft = Offset(cx - crossW / 2f, cy - crossH / 2f - radius * 0.05f),
        size = androidx.compose.ui.geometry.Size(crossW, crossH)
    )
    drawRect(
        color = red,
        topLeft = Offset(cx - crossH / 2f, cy - crossW / 2f - radius * 0.05f),
        size = androidx.compose.ui.geometry.Size(crossH, crossW)
    )

    // Florence Nightingale Lamp Base & Flame
    val lampPath = Path().apply {
        moveTo(cx - radius * 0.35f, cy + radius * 0.25f)
        cubicTo(
            cx - radius * 0.2f, cy + radius * 0.45f,
            cx + radius * 0.2f, cy + radius * 0.45f,
            cx + radius * 0.35f, cy + radius * 0.25f
        )
        lineTo(cx - radius * 0.35f, cy + radius * 0.25f)
        close()
    }
    drawPath(lampPath, color = emerald)

    // Lamp flame
    drawCircle(
        color = gold,
        radius = radius * 0.09f,
        center = Offset(cx, cy + radius * 0.18f)
    )
}
