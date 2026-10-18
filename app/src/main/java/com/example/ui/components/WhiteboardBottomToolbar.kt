package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShapeType
import com.example.model.ToolType
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.StickyNoteColors
import com.example.ui.theme.WhiteboardColors
import com.example.ui.viewmodel.WhiteboardViewModel

@Composable
fun WhiteboardBottomToolbar(
    viewModel: WhiteboardViewModel,
    modifier: Modifier = Modifier
) {
    val activeTool by viewModel.activeTool.collectAsState()
    val activeShapeType by viewModel.activeShapeType.collectAsState()
    val strokeColor by viewModel.strokeColor.collectAsState()
    val strokeWidth by viewModel.strokeWidth.collectAsState()
    val isDashed by viewModel.isDashed.collectAsState()
    val activeStickyColor by viewModel.activeStickyColor.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable Context Sub-bar (Colors, Stroke widths, Shapes)
        AnimatedVisibility(
            visible = activeTool == ToolType.PEN || activeTool == ToolType.HIGHLIGHTER || activeTool == ToolType.SHAPE || activeTool == ToolType.STICKY_NOTE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sub-bar for Pen & Highlighter: Colors + Widths + Style
                    if (activeTool == ToolType.PEN || activeTool == ToolType.HIGHLIGHTER || activeTool == ToolType.SHAPE) {
                        // 1. Color Palette Swatches
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WhiteboardColors.ExtendedPalette.forEach { color ->
                                val isSelected = strokeColor == color
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setStrokeColor(color) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected color",
                                            tint = if (color == WhiteboardColors.WhiteInk) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Stroke Width & Line Style Options
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Width presets
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    2f to "Fine",
                                    6f to "Medium",
                                    12f to "Thick",
                                    24f to "Bold"
                                ).forEach { (width, label) ->
                                    val isSelected = strokeWidth == width
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { viewModel.setStrokeWidth(width) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Slate600
                                        )
                                    }
                                }
                            }

                            // Dashed toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isDashed) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.toggleDashed() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isDashed) "Dashed" else "Solid",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDashed) MaterialTheme.colorScheme.onPrimaryContainer else Slate600
                                )
                            }
                        }
                    }

                    // Sub-bar for Shape: Shape Types
                    if (activeTool == ToolType.SHAPE) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ShapeType.values().forEach { shape ->
                                val isSelected = activeShapeType == shape
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setShapeType(shape) },
                                    label = { Text(shape.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Sub-bar for Sticky Note: Colors
                    if (activeTool == ToolType.STICKY_NOTE) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sticky Color:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate900
                            )
                            StickyNoteColors.All.forEach { color ->
                                val isSelected = activeStickyColor == color
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Slate900 else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setStickyColor(color) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected color",
                                            tint = Slate900,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Primary Floating Toolbar Dock
        Surface(
            modifier = Modifier.widthIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 6.dp,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(
                    tool = ToolType.PAN,
                    icon = Icons.Default.PanTool,
                    label = "Hand",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.PAN) }
                )
                ToolButton(
                    tool = ToolType.SELECT,
                    icon = Icons.Default.NearMe,
                    label = "Select",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.SELECT) }
                )
                ToolButton(
                    tool = ToolType.PEN,
                    icon = Icons.Default.Brush,
                    label = "Pen",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.PEN) }
                )
                ToolButton(
                    tool = ToolType.HIGHLIGHTER,
                    icon = Icons.Default.Highlight,
                    label = "Marker",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.HIGHLIGHTER) }
                )
                ToolButton(
                    tool = ToolType.ERASER,
                    icon = Icons.Default.AutoFixNormal,
                    label = "Eraser",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.ERASER) }
                )
                ToolButton(
                    tool = ToolType.SHAPE,
                    icon = Icons.Default.Category,
                    label = "Shapes",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.SHAPE) }
                )
                ToolButton(
                    tool = ToolType.STICKY_NOTE,
                    icon = Icons.Default.NoteAlt,
                    label = "Sticky",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.STICKY_NOTE) }
                )
                ToolButton(
                    tool = ToolType.TEXT,
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.TEXT) }
                )
                ToolButton(
                    tool = ToolType.LASER,
                    icon = Icons.Default.WbIncandescent,
                    label = "Laser",
                    activeTool = activeTool,
                    onClick = { viewModel.setTool(ToolType.LASER) }
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    tool: ToolType,
    icon: ImageVector,
    label: String,
    activeTool: ToolType,
    onClick: () -> Unit
) {
    val isActive = activeTool == tool

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("tool_${tool.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary else Slate600,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else Slate600
        )
    }
}
