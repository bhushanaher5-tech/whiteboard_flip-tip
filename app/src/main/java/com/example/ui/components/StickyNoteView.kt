package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StickyNote
import com.example.ui.canvas.canvasToScreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StickyNoteColors
import kotlin.math.roundToInt

@Composable
fun StickyNoteView(
    note: StickyNote,
    panOffset: Offset,
    scale: Float,
    onMove: (deltaX: Float, deltaY: Float) -> Unit,
    onDragEnd: () -> Unit,
    onUpdateText: (String) -> Unit,
    onUpdateColor: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    val screenPos = canvasToScreen(Offset(note.x, note.y), panOffset, scale)
    val noteWidth = (note.width * scale).coerceAtLeast(120f).dp
    val noteHeight = (note.height * scale).coerceAtLeast(110f).dp

    val bgColor = Color(note.colorHex.toInt())

    Box(
        modifier = Modifier
            .offset { IntOffset(screenPos.x.roundToInt(), screenPos.y.roundToInt()) }
            .rotate(note.rotationDegrees)
            .width(noteWidth)
            .height(noteHeight)
            .testTag("sticky_note_${note.id}")
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = (4 * scale).coerceIn(2f, 10f).dp, shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header drag-bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.08f))
                        .pointerInput(note.id) {
                            detectDragGestures(
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onMove(dragAmount.x / scale, dragAmount.y / scale)
                                }
                            )
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Move note",
                        modifier = Modifier.size(16.dp),
                        tint = Slate800.copy(alpha = 0.6f)
                    )

                    Row {
                        IconButton(
                            onClick = { showColorDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Change color",
                                modifier = Modifier.size(14.dp),
                                tint = Slate800.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit text",
                                modifier = Modifier.size(14.dp),
                                tint = Slate800.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete note",
                                modifier = Modifier.size(14.dp),
                                tint = Slate800.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Note text body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showEditDialog = true }
                        .padding(8.dp)
                ) {
                    Text(
                        text = note.text,
                        color = Slate900,
                        fontSize = (13f * scale).coerceIn(10f, 22f).sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = (17f * scale).coerceIn(13f, 26f).sp
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        var editText by remember { mutableStateOf(note.text) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Sticky Note") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("sticky_note_input"),
                    label = { Text("Note content") },
                    maxLines = 6
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateText(editText)
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("save_sticky_note_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Choose Note Color") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StickyNoteColors.All.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (color.toArgb().toLong() == note.colorHex) 3.dp else 1.dp,
                                    color = if (color.toArgb().toLong() == note.colorHex) Slate900 else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onUpdateColor(color.toArgb().toLong() and 0xFFFFFFFFL)
                                    showColorDialog = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
