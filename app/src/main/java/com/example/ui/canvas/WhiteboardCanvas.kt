package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.testTag
import com.example.model.CanvasPoint
import com.example.model.CanvasShape
import com.example.model.DrawingStroke
import com.example.model.ToolType
import com.example.ui.components.CanvasTextView
import com.example.ui.components.StickyNoteView
import com.example.ui.viewmodel.WhiteboardViewModel
import java.util.UUID

@Composable
fun WhiteboardCanvas(
    viewModel: WhiteboardViewModel,
    modifier: Modifier = Modifier
) {
    val board by viewModel.currentBoard.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val activeShapeType by viewModel.activeShapeType.collectAsState()
    val strokeColor by viewModel.strokeColor.collectAsState()
    val strokeWidth by viewModel.strokeWidth.collectAsState()
    val isDashed by viewModel.isDashed.collectAsState()
    val scale by viewModel.scale.collectAsState()
    val panOffset by viewModel.panOffset.collectAsState()
    val laserPoints by viewModel.laserPoints.collectAsState()

    // Real-time in-progress stroke
    val inProgressPoints = remember { mutableStateListOf<CanvasPoint>() }
    // Real-time in-progress shape
    var inProgressShape by remember { mutableStateOf<CanvasShape?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("whiteboard_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("drawing_canvas")
                .pointerInput(activeTool, scale, panOffset, strokeColor, strokeWidth, isDashed) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
                        var isMultiTouch = false

                        val canvasDown = screenToCanvas(down.position, panOffset, scale)

                        when (activeTool) {
                            ToolType.PAN -> {
                                // 1-finger pan or 2-finger zoom handled in drag loop
                            }
                            ToolType.PEN, ToolType.HIGHLIGHTER -> {
                                inProgressPoints.clear()
                                inProgressPoints.add(CanvasPoint(canvasDown.x, canvasDown.y))
                                down.consume()
                            }
                            ToolType.SHAPE -> {
                                inProgressShape = CanvasShape(
                                    id = UUID.randomUUID().toString(),
                                    type = activeShapeType,
                                    startX = canvasDown.x,
                                    startY = canvasDown.y,
                                    endX = canvasDown.x,
                                    endY = canvasDown.y,
                                    strokeColorHex = strokeColor.toArgb().toLong() and 0xFFFFFFFFL,
                                    strokeWidth = strokeWidth,
                                    isDashed = isDashed
                                )
                                down.consume()
                            }
                            ToolType.ERASER -> {
                                viewModel.eraseAt(canvasDown.x, canvasDown.y, 24f)
                                down.consume()
                            }
                            ToolType.LASER -> {
                                viewModel.addLaserPoint(canvasDown.x, canvasDown.y)
                                down.consume()
                            }
                            ToolType.STICKY_NOTE -> {
                                viewModel.addStickyNote(canvasDown.x, canvasDown.y)
                                viewModel.setTool(ToolType.SELECT)
                                down.consume()
                            }
                            ToolType.TEXT -> {
                                viewModel.addText(canvasDown.x, canvasDown.y, "Whiteboard Note")
                                viewModel.setTool(ToolType.SELECT)
                                down.consume()
                            }
                            ToolType.SELECT -> {
                                // Tapping handles selection
                            }
                        }

                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Main)
                            val activePointers = event.changes.filter { it.pressed }

                            if (activePointers.isEmpty()) {
                                // Gesture ended
                                if (inProgressPoints.size > 0 && (activeTool == ToolType.PEN || activeTool == ToolType.HIGHLIGHTER)) {
                                    val newStroke = DrawingStroke(
                                        id = UUID.randomUUID().toString(),
                                        points = inProgressPoints.toList(),
                                        colorHex = strokeColor.toArgb().toLong() and 0xFFFFFFFFL,
                                        strokeWidth = strokeWidth,
                                        toolType = activeTool,
                                        isDashed = isDashed
                                    )
                                    viewModel.addStroke(newStroke)
                                    inProgressPoints.clear()
                                }
                                inProgressShape?.let { shape ->
                                    if (shape.startX != shape.endX || shape.startY != shape.endY) {
                                        viewModel.addShape(shape)
                                    }
                                    inProgressShape = null
                                }
                                break
                            }

                            if (activePointers.size >= 2) {
                                isMultiTouch = true
                                // Multi-finger pinch zoom & pan
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                viewModel.onPanAndZoom(pan, zoom)
                                inProgressPoints.clear()
                                inProgressShape = null
                                event.changes.forEach { it.consume() }
                            } else if (!isMultiTouch) {
                                val pointer = activePointers.first()
                                val canvasPos = screenToCanvas(pointer.position, panOffset, scale)

                                when (activeTool) {
                                    ToolType.PAN -> {
                                        val pan = pointer.positionChange()
                                        viewModel.onPanAndZoom(pan, 1f)
                                        pointer.consume()
                                    }
                                    ToolType.PEN, ToolType.HIGHLIGHTER -> {
                                        inProgressPoints.add(CanvasPoint(canvasPos.x, canvasPos.y))
                                        pointer.consume()
                                    }
                                    ToolType.SHAPE -> {
                                        inProgressShape = inProgressShape?.copy(
                                            endX = canvasPos.x,
                                            endY = canvasPos.y
                                        )
                                        pointer.consume()
                                    }
                                    ToolType.ERASER -> {
                                        viewModel.eraseAt(canvasPos.x, canvasPos.y, 24f)
                                        pointer.consume()
                                    }
                                    ToolType.LASER -> {
                                        viewModel.addLaserPoint(canvasPos.x, canvasPos.y)
                                        pointer.consume()
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
        ) {
            // 1. Draw canvas background grid/ruled/dots/chalkboard
            drawCanvasBackground(board.backgroundStyle, panOffset, scale)

            // 2. Draw existing strokes
            for (stroke in board.strokes) {
                drawSingleStroke(stroke, panOffset, scale)
            }

            // 3. Draw in-progress stroke
            if (inProgressPoints.isNotEmpty()) {
                val tempStroke = DrawingStroke(
                    id = "temp",
                    points = inProgressPoints.toList(),
                    colorHex = strokeColor.toArgb().toLong() and 0xFFFFFFFFL,
                    strokeWidth = strokeWidth,
                    toolType = activeTool,
                    isDashed = isDashed
                )
                drawSingleStroke(tempStroke, panOffset, scale)
            }

            // 4. Draw existing shapes
            for (shape in board.shapes) {
                drawSingleShape(shape, panOffset, scale)
            }

            // 5. Draw in-progress shape
            inProgressShape?.let { shape ->
                drawSingleShape(shape, panOffset, scale)
            }

            // 6. Draw laser pointer trail
            drawLaserTrail(laserPoints, panOffset, scale)
        }

        // Render Sticky Notes overlay
        for (note in board.stickyNotes) {
            StickyNoteView(
                note = note,
                panOffset = panOffset,
                scale = scale,
                onMove = { dx, dy -> viewModel.moveStickyNote(note.id, dx, dy) },
                onDragEnd = { viewModel.onStickyDragEnd() },
                onUpdateText = { newText -> viewModel.updateStickyNote(note.id, newText) },
                onUpdateColor = { colorHex -> viewModel.updateStickyNote(note.id, note.text, colorHex) },
                onDelete = { viewModel.deleteStickyNote(note.id) }
            )
        }

        // Render Canvas Text labels overlay
        for (txt in board.texts) {
            CanvasTextView(
                canvasText = txt,
                panOffset = panOffset,
                scale = scale,
                onUpdateText = { newText -> viewModel.updateText(txt.id, newText) },
                onDelete = { viewModel.deleteText(txt.id) }
            )
        }
    }
}
