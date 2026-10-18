package com.example.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.BackgroundStyle
import com.example.model.CanvasPoint
import com.example.model.CanvasShape
import com.example.model.DrawingStroke
import com.example.model.LaserPoint
import com.example.model.ShapeType
import com.example.model.ToolType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun screenToCanvas(screenPoint: Offset, panOffset: Offset, scale: Float): Offset {
    return Offset(
        x = (screenPoint.x - panOffset.x) / scale,
        y = (screenPoint.y - panOffset.y) / scale
    )
}

fun canvasToScreen(canvasPoint: Offset, panOffset: Offset, scale: Float): Offset {
    return Offset(
        x = canvasPoint.x * scale + panOffset.x,
        y = canvasPoint.y * scale + panOffset.y
    )
}

fun DrawScope.drawCanvasBackground(style: BackgroundStyle, panOffset: Offset, scale: Float) {
    val canvasW = size.width
    val canvasH = size.height

    when (style) {
        BackgroundStyle.PLAIN_WHITE -> {
            drawRect(color = Color(0xFFFAFAFA))
        }
        BackgroundStyle.DOT_GRID -> {
            drawRect(color = Color(0xFFF8FAFC))
            val spacing = 28f * scale
            val dotRadius = 1.6f * scale.coerceIn(0.6f, 2.0f)
            val startX = (panOffset.x % spacing)
            val startY = (panOffset.y % spacing)

            var x = startX
            while (x < canvasW) {
                var y = startY
                while (y < canvasH) {
                    drawCircle(
                        color = Color(0xFFCBD5E1),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += spacing
                }
                x += spacing
            }
        }
        BackgroundStyle.SQUARE_GRID -> {
            drawRect(color = Color(0xFFFFFFFF))
            val spacing = 32f * scale
            val startX = (panOffset.x % spacing)
            val startY = (panOffset.y % spacing)

            var x = startX
            while (x < canvasW) {
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 1f
                )
                x += spacing
            }
            var y = startY
            while (y < canvasH) {
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 1f
                )
                y += spacing
            }
        }
        BackgroundStyle.RULED -> {
            drawRect(color = Color(0xFFFFFBEB)) // Warm notebook paper
            val spacing = 36f * scale
            val startY = (panOffset.y % spacing)

            // Ruled blue horizontal lines
            var y = startY
            while (y < canvasH) {
                drawLine(
                    color = Color(0xFFBAE6FD),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 1.2f
                )
                y += spacing
            }
            // Vertical pink margin line
            val marginX = (80f * scale) + panOffset.x
            if (marginX in 0f..canvasW) {
                drawLine(
                    color = Color(0xFFFDA4AF),
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, canvasH),
                    strokeWidth = 2f
                )
            }
        }
        BackgroundStyle.CHALKBOARD -> {
            drawRect(color = Color(0xFF1E242B)) // Deep dark slate chalkboard
            val spacing = 36f * scale
            val startX = (panOffset.x % spacing)
            val startY = (panOffset.y % spacing)
            var x = startX
            while (x < canvasW) {
                drawLine(
                    color = Color(0x15FFFFFF),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 0.8f
                )
                x += spacing
            }
            var y = startY
            while (y < canvasH) {
                drawLine(
                    color = Color(0x15FFFFFF),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 0.8f
                )
                y += spacing
            }
        }
        BackgroundStyle.BLUEPRINT -> {
            drawRect(color = Color(0xFF0A2540)) // Engineering blueprint blue
            val fineSpacing = 16f * scale
            val majorSpacing = 80f * scale

            val startFX = (panOffset.x % fineSpacing)
            val startFY = (panOffset.y % fineSpacing)
            var x = startFX
            while (x < canvasW) {
                drawLine(
                    color = Color(0x1838BDF8),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 0.6f
                )
                x += fineSpacing
            }
            var y = startFY
            while (y < canvasH) {
                drawLine(
                    color = Color(0x1838BDF8),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 0.6f
                )
                y += fineSpacing
            }

            // Major grid lines
            val startMX = (panOffset.x % majorSpacing)
            val startMY = (panOffset.y % majorSpacing)
            var mx = startMX
            while (mx < canvasW) {
                drawLine(
                    color = Color(0x3538BDF8),
                    start = Offset(mx, 0f),
                    end = Offset(mx, canvasH),
                    strokeWidth = 1.2f
                )
                mx += majorSpacing
            }
            var my = startMY
            while (my < canvasH) {
                drawLine(
                    color = Color(0x3538BDF8),
                    start = Offset(0f, my),
                    end = Offset(canvasW, my),
                    strokeWidth = 1.2f
                )
                my += majorSpacing
            }
        }
    }
}

fun DrawScope.drawSingleStroke(
    stroke: DrawingStroke,
    panOffset: Offset,
    scale: Float
) {
    if (stroke.points.isEmpty()) return

    val color = Color(stroke.colorHex.toInt())
    val strokeColor = if (stroke.toolType == ToolType.HIGHLIGHTER) {
        color.copy(alpha = 0.38f)
    } else {
        color
    }

    val effectiveWidth = if (stroke.toolType == ToolType.HIGHLIGHTER) {
        stroke.strokeWidth * 3.0f * scale
    } else {
        stroke.strokeWidth * scale
    }

    val pathEffect = if (stroke.isDashed) {
        PathEffect.dashPathEffect(floatArrayOf(16f * scale, 12f * scale), 0f)
    } else null

    if (stroke.points.size == 1) {
        val p = stroke.points[0]
        val center = canvasToScreen(Offset(p.x, p.y), panOffset, scale)
        drawCircle(
            color = strokeColor,
            radius = effectiveWidth / 2f,
            center = center
        )
        return
    }

    val path = Path()
    val first = canvasToScreen(Offset(stroke.points[0].x, stroke.points[0].y), panOffset, scale)
    path.moveTo(first.x, first.y)

    for (i in 1 until stroke.points.size) {
        val prev = canvasToScreen(Offset(stroke.points[i - 1].x, stroke.points[i - 1].y), panOffset, scale)
        val curr = canvasToScreen(Offset(stroke.points[i].x, stroke.points[i].y), panOffset, scale)
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadraticTo(prev.x, prev.y, midX, midY)
    }

    val last = canvasToScreen(Offset(stroke.points.last().x, stroke.points.last().y), panOffset, scale)
    path.lineTo(last.x, last.y)

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = effectiveWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = pathEffect
        )
    )
}

fun DrawScope.drawSingleShape(
    shape: CanvasShape,
    panOffset: Offset,
    scale: Float
) {
    val p1 = canvasToScreen(Offset(shape.startX, shape.startY), panOffset, scale)
    val p2 = canvasToScreen(Offset(shape.endX, shape.endY), panOffset, scale)

    val strokeColor = Color(shape.strokeColorHex.toInt())
    val strokeWidth = shape.strokeWidth * scale
    val pathEffect = if (shape.isDashed) {
        PathEffect.dashPathEffect(floatArrayOf(16f * scale, 12f * scale), 0f)
    } else null

    val left = minOf(p1.x, p2.x)
    val top = minOf(p1.y, p2.y)
    val right = maxOf(p1.x, p2.x)
    val bottom = maxOf(p1.y, p2.y)
    val width = right - left
    val height = bottom - top

    when (shape.type) {
        ShapeType.RECTANGLE -> {
            shape.fillColorHex?.let { fillHex ->
                drawRect(
                    color = Color(fillHex.toInt()),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(width, height),
                    style = Fill
                )
            }
            drawRect(
                color = strokeColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(width, height),
                style = Stroke(width = strokeWidth, pathEffect = pathEffect)
            )
        }
        ShapeType.CIRCLE -> {
            val center = Offset((left + right) / 2f, (top + bottom) / 2f)
            val radius = maxOf(width, height) / 2f
            shape.fillColorHex?.let { fillHex ->
                drawCircle(
                    color = Color(fillHex.toInt()),
                    center = center,
                    radius = radius,
                    style = Fill
                )
            }
            drawCircle(
                color = strokeColor,
                center = center,
                radius = radius,
                style = Stroke(width = strokeWidth, pathEffect = pathEffect)
            )
        }
        ShapeType.LINE -> {
            drawLine(
                color = strokeColor,
                start = p1,
                end = p2,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = pathEffect
            )
        }
        ShapeType.ARROW -> {
            drawLine(
                color = strokeColor,
                start = p1,
                end = p2,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = pathEffect
            )
            // Arrowhead calculations
            val angle = atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())
            val arrowLen = (18f * scale).coerceIn(14f, 36f)
            val arrowAngle = PI / 6.0 // 30 degrees

            val x1 = p2.x - (arrowLen * cos(angle - arrowAngle)).toFloat()
            val y1 = p2.y - (arrowLen * sin(angle - arrowAngle)).toFloat()
            val x2 = p2.x - (arrowLen * cos(angle + arrowAngle)).toFloat()
            val y2 = p2.y - (arrowLen * sin(angle + arrowAngle)).toFloat()

            val arrowPath = Path().apply {
                moveTo(p2.x, p2.y)
                lineTo(x1, y1)
                moveTo(p2.x, p2.y)
                lineTo(x2, y2)
            }
            drawPath(
                path = arrowPath,
                color = strokeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                moveTo((left + right) / 2f, top)
                lineTo(right, bottom)
                lineTo(left, bottom)
                close()
            }
            shape.fillColorHex?.let { fillHex ->
                drawPath(path = path, color = Color(fillHex.toInt()), style = Fill)
            }
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect)
            )
        }
        ShapeType.DIAMOND -> {
            val midX = (left + right) / 2f
            val midY = (top + bottom) / 2f
            val path = Path().apply {
                moveTo(midX, top)
                lineTo(right, midY)
                lineTo(midX, bottom)
                lineTo(left, midY)
                close()
            }
            shape.fillColorHex?.let { fillHex ->
                drawPath(path = path, color = Color(fillHex.toInt()), style = Fill)
            }
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect)
            )
        }
        ShapeType.STAR -> {
            val cx = (left + right) / 2f
            val cy = (top + bottom) / 2f
            val rOuter = minOf(width, height) / 2f
            val rInner = rOuter * 0.45f
            val path = Path()

            for (i in 0 until 10) {
                val angle = (i * PI / 5.0) - (PI / 2.0)
                val r = if (i % 2 == 0) rOuter else rInner
                val x = (cx + r * cos(angle)).toFloat()
                val y = (cy + r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            shape.fillColorHex?.let { fillHex ->
                drawPath(path = path, color = Color(fillHex.toInt()), style = Fill)
            }
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect)
            )
        }
    }
}

fun DrawScope.drawLaserTrail(
    points: List<LaserPoint>,
    panOffset: Offset,
    scale: Float
) {
    if (points.isEmpty()) return
    val now = System.currentTimeMillis()

    for (p in points) {
        val age = (now - p.timestamp).coerceAtLeast(0L)
        val maxAge = 1200L
        if (age < maxAge) {
            val progress = 1f - (age.toFloat() / maxAge.toFloat())
            val screenPos = canvasToScreen(Offset(p.x, p.y), panOffset, scale)
            val radius = (10f * progress * scale).coerceIn(3f, 24f)

            // Outer glow
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.35f * progress),
                radius = radius * 2.2f,
                center = screenPos
            )
            // Core laser dot
            drawCircle(
                color = Color(0xFFFF8888).copy(alpha = 0.9f * progress),
                radius = radius,
                center = screenPos
            )
        }
    }
}
