package com.example.ui.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.FileProvider
import com.example.model.BackgroundStyle
import com.example.model.ShapeType
import com.example.model.ToolType
import com.example.model.Whiteboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object WhiteboardExportHelper {

    suspend fun exportAndShareBoard(context: Context, board: Whiteboard) = withContext(Dispatchers.IO) {
        val bitmap = renderBoardToBitmap(board)
        val cachePath = File(context.cacheDir, "exports")
        cachePath.mkdirs()
        val file = File(cachePath, "whiteboard_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, board.title)
            putExtra(Intent.EXTRA_TEXT, "Exported from Open Whiteboard: ${board.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Whiteboard")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun renderBoardToBitmap(board: Whiteboard, width: Int = 1920, height: Int = 1080): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgPaint = Paint().apply { isAntiAlias = true }
        when (board.backgroundStyle) {
            BackgroundStyle.PLAIN_WHITE -> canvas.drawColor(android.graphics.Color.rgb(250, 250, 250))
            BackgroundStyle.DOT_GRID -> {
                canvas.drawColor(android.graphics.Color.rgb(248, 250, 252))
                val dotPaint = Paint().apply {
                    color = android.graphics.Color.rgb(203, 213, 225)
                    isAntiAlias = true
                }
                var x = 28f
                while (x < width) {
                    var y = 28f
                    while (y < height) {
                        canvas.drawCircle(x, y, 2f, dotPaint)
                        y += 28f
                    }
                    x += 28f
                }
            }
            BackgroundStyle.SQUARE_GRID -> {
                canvas.drawColor(android.graphics.Color.WHITE)
                val gridPaint = Paint().apply {
                    color = android.graphics.Color.rgb(226, 232, 240)
                    strokeWidth = 1f
                }
                var x = 32f
                while (x < width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
                    x += 32f
                }
                var y = 32f
                while (y < height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
                    y += 32f
                }
            }
            BackgroundStyle.RULED -> {
                canvas.drawColor(android.graphics.Color.rgb(255, 251, 235))
                val ruledPaint = Paint().apply {
                    color = android.graphics.Color.rgb(186, 230, 253)
                    strokeWidth = 1.2f
                }
                var y = 36f
                while (y < height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, ruledPaint)
                    y += 36f
                }
                val marginPaint = Paint().apply {
                    color = android.graphics.Color.rgb(253, 164, 175)
                    strokeWidth = 2f
                }
                canvas.drawLine(80f, 0f, 80f, height.toFloat(), marginPaint)
            }
            BackgroundStyle.CHALKBOARD -> {
                canvas.drawColor(android.graphics.Color.rgb(30, 36, 43))
            }
            BackgroundStyle.BLUEPRINT -> {
                canvas.drawColor(android.graphics.Color.rgb(10, 37, 64))
                val bpPaint = Paint().apply {
                    color = android.graphics.Color.argb(40, 56, 189, 248)
                    strokeWidth = 1f
                }
                var x = 40f
                while (x < width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), bpPaint)
                    x += 40f
                }
                var y = 40f
                while (y < height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, bpPaint)
                    y += 40f
                }
            }
        }

        // Render strokes
        for (stroke in board.strokes) {
            if (stroke.points.isEmpty()) continue
            val strokePaint = Paint().apply {
                color = stroke.colorHex.toInt()
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = if (stroke.toolType == ToolType.HIGHLIGHTER) stroke.strokeWidth * 3f else stroke.strokeWidth
                if (stroke.toolType == ToolType.HIGHLIGHTER) alpha = 90
            }

            if (stroke.points.size == 1) {
                canvas.drawCircle(stroke.points[0].x, stroke.points[0].y, strokePaint.strokeWidth / 2f, strokePaint)
            } else {
                val path = Path()
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    val prev = stroke.points[i - 1]
                    val curr = stroke.points[i]
                    path.quadTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                }
                path.lineTo(stroke.points.last().x, stroke.points.last().y)
                canvas.drawPath(path, strokePaint)
            }
        }

        // Render shapes
        for (shape in board.shapes) {
            val shapePaint = Paint().apply {
                color = shape.strokeColorHex.toInt()
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = shape.strokeWidth
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            val left = minOf(shape.startX, shape.endX)
            val top = minOf(shape.startY, shape.endY)
            val right = maxOf(shape.startX, shape.endX)
            val bottom = maxOf(shape.startY, shape.endY)

            when (shape.type) {
                ShapeType.RECTANGLE -> canvas.drawRect(left, top, right, bottom, shapePaint)
                ShapeType.CIRCLE -> {
                    val cx = (left + right) / 2f
                    val cy = (top + bottom) / 2f
                    val r = maxOf(right - left, bottom - top) / 2f
                    canvas.drawCircle(cx, cy, r, shapePaint)
                }
                ShapeType.LINE -> canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, shapePaint)
                ShapeType.ARROW -> {
                    canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, shapePaint)
                    val angle = atan2((shape.endY - shape.startY).toDouble(), (shape.endX - shape.startX).toDouble())
                    val arrowLen = 22.0
                    val a1 = angle - PI / 6.0
                    val a2 = angle + PI / 6.0
                    canvas.drawLine(shape.endX, shape.endY, (shape.endX - arrowLen * cos(a1)).toFloat(), (shape.endY - arrowLen * sin(a1)).toFloat(), shapePaint)
                    canvas.drawLine(shape.endX, shape.endY, (shape.endX - arrowLen * cos(a2)).toFloat(), (shape.endY - arrowLen * sin(a2)).toFloat(), shapePaint)
                }
                ShapeType.TRIANGLE -> {
                    val p = Path().apply {
                        moveTo((left + right) / 2f, top)
                        lineTo(right, bottom)
                        lineTo(left, bottom)
                        close()
                    }
                    canvas.drawPath(p, shapePaint)
                }
                ShapeType.DIAMOND -> {
                    val midX = (left + right) / 2f
                    val midY = (top + bottom) / 2f
                    val p = Path().apply {
                        moveTo(midX, top)
                        lineTo(right, midY)
                        lineTo(midX, bottom)
                        lineTo(left, midY)
                        close()
                    }
                    canvas.drawPath(p, shapePaint)
                }
                ShapeType.STAR -> {
                    val cx = (left + right) / 2f
                    val cy = (top + bottom) / 2f
                    val rOuter = minOf(right - left, bottom - top) / 2f
                    val rInner = rOuter * 0.45f
                    val p = Path()
                    for (i in 0 until 10) {
                        val angle = (i * PI / 5.0) - (PI / 2.0)
                        val r = if (i % 2 == 0) rOuter else rInner
                        val x = (cx + r * cos(angle)).toFloat()
                        val y = (cy + r * sin(angle)).toFloat()
                        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    p.close()
                    canvas.drawPath(p, shapePaint)
                }
            }
        }

        // Render sticky notes
        val noteBgPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        val noteTextPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
            textSize = 15f
        }
        for (note in board.stickyNotes) {
            noteBgPaint.color = note.colorHex.toInt()
            canvas.drawRect(note.x, note.y, note.x + note.width, note.y + note.height, noteBgPaint)
            val lines = note.text.split("\n")
            var lineY = note.y + 24f
            for (line in lines) {
                canvas.drawText(line, note.x + 12f, lineY, noteTextPaint)
                lineY += 20f
            }
        }

        // Render text
        val textPaint = Paint().apply { isAntiAlias = true }
        for (txt in board.texts) {
            textPaint.color = txt.colorHex.toInt()
            textPaint.textSize = txt.fontSizeSp * 1.5f
            canvas.drawText(txt.text, txt.x, txt.y, textPaint)
        }

        return bitmap
    }
}
