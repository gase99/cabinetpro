package com.cabinetpro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cabinetpro.app.data.SectionKey

/** Simple line illustration per section — port of web CabinetIllustration.tsx */
@Composable
fun CabinetIllustration(section: SectionKey, modifier: Modifier = Modifier, color: Color = Color(0xFF526A83)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx())
        when (section) {
            SectionKey.GROUND -> {
                // base cabinet: rectangle + countertop + doors
                drawRect(color = color, topLeft = androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.25f),
                    size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.55f), style = stroke)
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.25f), androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.25f), strokeWidth = 3.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f), androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.8f), strokeWidth = 2.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.8f), androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.92f), strokeWidth = 2.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.8f), androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.92f), strokeWidth = 2.dp.toPx())
            }
            SectionKey.WALL -> {
                drawRect(color = color, topLeft = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.15f),
                    size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.5f), style = stroke)
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.15f), androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.65f), strokeWidth = 2.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.75f), androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.85f), strokeWidth = 2.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.75f), androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.85f), strokeWidth = 2.dp.toPx())
            }
            SectionKey.TALL -> {
                drawRect(color = color, topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.05f),
                    size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.85f), style = stroke)
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.45f), androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.45f), strokeWidth = 2.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.05f), androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.9f), strokeWidth = 2.dp.toPx())
            }
            SectionKey.ISLAND -> {
                drawRect(color = color, topLeft = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.3f),
                    size = androidx.compose.ui.geometry.Size(w * 0.9f, h * 0.45f), style = stroke)
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.02f, h * 0.3f), androidx.compose.ui.geometry.Offset(w * 0.98f, h * 0.3f), strokeWidth = 3.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.3f), androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.75f), strokeWidth = 2.dp.toPx())
                drawLine(color, androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.3f), androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.75f), strokeWidth = 2.dp.toPx())
            }
        }
    }
}
