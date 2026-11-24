package com.cabinetpro.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cabinetpro.app.ui.theme.RingColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * RingLightCard — کادر با رینگ لایت رنگی چرخان
 *
 * دور هر کادر یک حلقه نور رنگین‌کمانی می‌چرخد:
 *  - لایه بیرونی: گرادیان sweep رنگین‌کمانی که حول مرکز می‌چرخد (rotating)
 *  - لایه داخلی: سطح سفید/رنگی که محتوا روی آن قرار می‌گیرد
 *  - هاله نور (glow) با سایه رنگی که هم‌زمان با چرخش تغییر رنگ می‌دهد
 *
 * برای غیرفعال کردن انیمیشن در تست/دسترس‌پذیری، [animated] را false کنید.
 */
@Composable
fun RingLightCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 3.dp,
    surfaceColor: Color = Color.White,
    animated: Boolean = true,
    durationMs: Int = 4500,
    phaseOffset: Float = 0f,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val infinite = rememberInfiniteTransition(label = "ringLight")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAngle"
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringGlow"
    )
    val rotation = if (animated) (angle + phaseOffset) % 360f else phaseOffset
    // Glow color follows the rotating gradient's leading edge
    val glowColor = RingColors[((rotation / 45f).toInt() % RingColors.size + RingColors.size) % RingColors.size]
        .copy(alpha = 0.35f * glowPulse)

    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = glowColor,
                ambientColor = glowColor
            )
            .clip(shape)
    ) {
        // --- Rotating rainbow layer (scaled up so rotation never shows corners) ---
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = 1.6f
                    scaleY = 1.6f
                }
                .background(Brush.sweepGradient(RingColors))
        )
        // --- Inner surface with padding = border thickness ---
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(borderWidth)
                .clip(RoundedCornerShape((cornerRadius - borderWidth).coerceAtLeast(4.dp)))
                .background(surfaceColor)
        )
        // --- Content ---
        Box(modifier = Modifier.padding(borderWidth)) {
            content()
        }
    }
}

/**
 * RingLightBorder — نسخه سبک‌تر: فقط دور محتوا خط نور چرخان می‌کشد
 * (با Canvas و Stroke روی مسیر rounded-rect + گرادیان چرخان).
 * برای کادرهای کوچک مثل کارت‌های طول بخش‌ها استفاده می‌شود.
 */
@Composable
fun RingLightBorder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 2.5.dp,
    animated: Boolean = true,
    durationMs: Int = 4000,
    phaseOffset: Float = 0f,
    content: @Composable () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "ringBorder")
    val angle by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringBorderAngle"
    )
    val rotation = if (animated) (angle + phaseOffset) % 360f else phaseOffset
    val density = LocalDensity.current
    val strokePx = with(density) { borderWidth.toPx() }
    val radiusPx = with(density) { cornerRadius.toPx() }

    Box(modifier = modifier) {
        content()
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            // Rotating sweep brush: rotate the gradient by drawing with a rotated
            // transform around the center, then stroking the rounded-rect path.
            val rad = Math.toRadians(rotation.toDouble())
            val dx = cos(rad).toFloat()
            val dy = sin(rad).toFloat()
            // Sweep gradient whose start point orbits the center -> appears to rotate
            val brush = Brush.sweepGradient(
                colors = RingColors,
                center = Offset(cx + dx * w * 0.35f, cy + dy * h * 0.35f)
            )
            val path = Path().apply {
                val inset = strokePx / 2f
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = inset, top = inset,
                        right = w - inset, bottom = h - inset,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
                    )
                )
            }
            drawPath(path = path, brush = brush, style = Stroke(width = strokePx))
        }
    }
}

/** Total / invoice card variant — thicker ring + navy surface */
@Composable
fun RingLightTotalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    RingLightCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        borderWidth = 4.dp,
        surfaceColor = Color(0xFF203650),
        durationMs = 3800,
        content = content
    )
}
