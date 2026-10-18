package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CanvasText
import com.example.ui.canvas.canvasToScreen
import kotlin.math.roundToInt

@Composable
fun CanvasTextView(
    canvasText: CanvasText,
    panOffset: Offset,
    scale: Float,
    onUpdateText: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    val screenPos = canvasToScreen(Offset(canvasText.x, canvasText.y), panOffset, scale)
    val color = Color(canvasText.colorHex.toInt())

    Box(
        modifier = Modifier
            .offset { IntOffset(screenPos.x.roundToInt(), screenPos.y.roundToInt()) }
            .clickable { isSelected = !isSelected }
            .padding(2.dp)
            .testTag("canvas_text_${canvasText.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (isSelected) {
                Modifier
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            } else Modifier
        ) {
            Text(
                text = canvasText.text,
                color = if (isSelected) Color(canvasText.colorHex.toInt()) else color,
                fontSize = (canvasText.fontSizeSp * scale).coerceIn(10f, 38f).sp,
                fontWeight = FontWeight.SemiBold
            )

            if (isSelected) {
                IconButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit text",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete text",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }

    if (isEditing) {
        var newContent by remember { mutableStateOf(canvasText.text) }
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text("Edit Canvas Text") },
            text = {
                OutlinedTextField(
                    value = newContent,
                    onValueChange = { newContent = it },
                    modifier = Modifier.fillMaxWidth().testTag("text_input_field"),
                    label = { Text("Text") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateText(newContent)
                        isEditing = false
                        isSelected = false
                    },
                    modifier = Modifier.testTag("save_text_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
