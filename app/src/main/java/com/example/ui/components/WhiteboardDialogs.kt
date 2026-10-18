package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.export.WhiteboardExportHelper
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WhiteboardViewModel
import kotlinx.coroutines.launch

@Composable
fun WhiteboardDialogs(viewModel: WhiteboardViewModel) {
    val isClearOpen by viewModel.isClearConfirmOpen.collectAsState()
    val isRenameOpen by viewModel.isRenameDialogOpen.collectAsState()
    val isExportOpen by viewModel.isExportDialogOpen.collectAsState()
    val isHelpOpen by viewModel.isHelpDialogOpen.collectAsState()
    val currentBoard by viewModel.currentBoard.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Clear Confirmation Dialog
    if (isClearOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeClearConfirm() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear Canvas?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to clear all strokes, shapes, and notes from this whiteboard? You can still use Undo if needed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearBoard()
                        viewModel.closeClearConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeClearConfirm() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Rename Board Dialog
    if (isRenameOpen) {
        var newTitle by remember { mutableStateOf(currentBoard.title) }
        AlertDialog(
            onDismissRequest = { viewModel.closeRenameDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rename Whiteboard", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Whiteboard Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_board_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameBoard(newTitle)
                    },
                    modifier = Modifier.testTag("save_rename_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeRenameDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Export Dialog
    if (isExportOpen) {
        var isExporting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isExporting) viewModel.closeExportDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Whiteboard", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Ready to export '${currentBoard.title}' as a high-resolution PNG image.")
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Format: High-res PNG (1920 × 1080)", style = MaterialTheme.typography.bodySmall)
                            Text("• Elements: ${currentBoard.strokes.size} strokes, ${currentBoard.shapes.size} shapes, ${currentBoard.stickyNotes.size} notes", style = MaterialTheme.typography.bodySmall)
                            Text("• Background: ${currentBoard.backgroundStyle.displayName}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            WhiteboardExportHelper.exportAndShareBoard(context, currentBoard)
                            isExporting = false
                            viewModel.closeExportDialog()
                        }
                    },
                    enabled = !isExporting,
                    modifier = Modifier.testTag("confirm_export_share_button")
                ) {
                    Text(if (isExporting) "Rendering..." else "Share via Apps")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.closeExportDialog() },
                    enabled = !isExporting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Help & Gestures Guide Dialog
    if (isHelpOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeHelpDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Whiteboard Gestures & Tips", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GestureTipRow(
                        icon = Icons.Default.ZoomIn,
                        title = "2-Finger Pan & Pinch Zoom",
                        desc = "Use two fingers anywhere on the canvas to pinch zoom and pan around the infinite board."
                    )
                    GestureTipRow(
                        icon = Icons.Default.TouchApp,
                        title = "1-Finger Drawing & Actions",
                        desc = "Use one finger to draw smooth Bézier curves, place geometric shapes, or use the stroke eraser."
                    )
                    GestureTipRow(
                        icon = Icons.Default.PanTool,
                        title = "Hand / Pan Tool",
                        desc = "Switch to the Hand tool in the bottom dock to navigate the board effortlessly with 1 finger."
                    )
                    GestureTipRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Laser Pointer Mode",
                        desc = "Great for walkthroughs and presentations! Glowing laser trails fade out after 1 second."
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.closeHelpDialog() }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
private fun GestureTipRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate900)
            Text(desc, fontSize = 12.sp, color = Slate600, lineHeight = 16.sp)
        }
    }
}
