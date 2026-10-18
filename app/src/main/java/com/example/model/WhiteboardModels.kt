package com.example.model

enum class ToolType {
    PAN,
    SELECT,
    PEN,
    HIGHLIGHTER,
    ERASER,
    SHAPE,
    STICKY_NOTE,
    TEXT,
    LASER
}

enum class ShapeType(val displayName: String) {
    RECTANGLE("Rectangle"),
    CIRCLE("Circle"),
    LINE("Line"),
    ARROW("Arrow"),
    TRIANGLE("Triangle"),
    STAR("Star"),
    DIAMOND("Diamond")
}

enum class BackgroundStyle(val displayName: String) {
    PLAIN_WHITE("Whiteboard"),
    DOT_GRID("Dot Grid"),
    SQUARE_GRID("Math Grid"),
    RULED("Ruled Lines"),
    CHALKBOARD("Chalkboard"),
    BLUEPRINT("Blueprint")
}

data class CanvasPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)

data class DrawingStroke(
    val id: String,
    val points: List<CanvasPoint>,
    val colorHex: Long,
    val strokeWidth: Float,
    val toolType: ToolType = ToolType.PEN,
    val isDashed: Boolean = false
)

data class CanvasShape(
    val id: String,
    val type: ShapeType,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val strokeColorHex: Long,
    val fillColorHex: Long? = null,
    val strokeWidth: Float = 4f,
    val isDashed: Boolean = false
)

data class StickyNote(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float = 160f,
    val height: Float = 160f,
    val text: String,
    val colorHex: Long = 0xFFFEF08AL,
    val rotationDegrees: Float = 0f
)

data class CanvasText(
    val id: String,
    val x: Float,
    val y: Float,
    val text: String,
    val colorHex: Long = 0xFF0F172AL,
    val fontSizeSp: Float = 18f
)

data class LaserPoint(
    val x: Float,
    val y: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class Whiteboard(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val backgroundStyle: BackgroundStyle = BackgroundStyle.DOT_GRID,
    val strokes: List<DrawingStroke> = emptyList(),
    val shapes: List<CanvasShape> = emptyList(),
    val stickyNotes: List<StickyNote> = emptyList(),
    val texts: List<CanvasText> = emptyList()
)
