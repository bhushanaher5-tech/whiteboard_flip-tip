package com.example.data

import com.example.model.BackgroundStyle
import com.example.model.CanvasPoint
import com.example.model.CanvasShape
import com.example.model.CanvasText
import com.example.model.DrawingStroke
import com.example.model.ShapeType
import com.example.model.StickyNote
import com.example.model.ToolType
import com.example.model.Whiteboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WhiteboardRepository(private val dao: WhiteboardDao) {

    fun getAllWhiteboards(): Flow<List<Whiteboard>> =
        dao.getAllWhiteboards().map { list -> list.map { it.toDomain() } }

    suspend fun getWhiteboardById(id: String): Whiteboard? =
        dao.getWhiteboardById(id)?.toDomain()

    suspend fun saveWhiteboard(board: Whiteboard) {
        val updated = board.copy(updatedAt = System.currentTimeMillis())
        dao.insertWhiteboard(WhiteboardEntity.fromDomain(updated))
    }

    suspend fun deleteWhiteboard(id: String) {
        dao.deleteWhiteboard(id)
    }

    suspend fun ensureDefaultBoardExists(): Whiteboard {
        val count = dao.getBoardCount()
        if (count == 0) {
            val starterBoard = createStarterBrainstormingBoard()
            saveWhiteboard(starterBoard)
            return starterBoard
        }
        val first = dao.getAllWhiteboards()
        // Return starter template if needed
        return createStarterBrainstormingBoard()
    }

    fun createTemplate(templateType: TemplateType): Whiteboard {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return when (templateType) {
            TemplateType.BLANK -> Whiteboard(
                id = id,
                title = "Untitled Canvas",
                createdAt = now,
                updatedAt = now,
                backgroundStyle = BackgroundStyle.DOT_GRID
            )
            TemplateType.BRAINSTORMING -> createStarterBrainstormingBoard(id, "Creative Brainstorm")
            TemplateType.FLOWCHART -> createFlowchartTemplate(id)
            TemplateType.MEETING_NOTES -> createMeetingNotesTemplate(id)
            TemplateType.CHALKBOARD -> createChalkboardTemplate(id)
            TemplateType.BLUEPRINT -> createBlueprintTemplate(id)
        }
    }

    private fun createStarterBrainstormingBoard(
        id: String = UUID.randomUUID().toString(),
        title: String = "Project Kickoff"
    ): Whiteboard {
        val now = System.currentTimeMillis()

        val strokes = listOf(
            // Underline beneath title
            DrawingStroke(
                id = UUID.randomUUID().toString(),
                points = listOf(
                    CanvasPoint(80f, 160f),
                    CanvasPoint(180f, 162f),
                    CanvasPoint(280f, 160f),
                    CanvasPoint(380f, 164f)
                ),
                colorHex = 0xFF4F46E5L,
                strokeWidth = 5f,
                toolType = ToolType.PEN
            )
        )

        val shapes = listOf(
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.ARROW,
                startX = 280f,
                startY = 280f,
                endX = 400f,
                endY = 280f,
                strokeColorHex = 0xFF6366F1L,
                strokeWidth = 4f
            ),
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.ARROW,
                startX = 600f,
                startY = 280f,
                endX = 720f,
                endY = 280f,
                strokeColorHex = 0xFF06B6D4L,
                strokeWidth = 4f
            )
        )

        val stickyNotes = listOf(
            StickyNote(
                id = UUID.randomUUID().toString(),
                x = 80f,
                y = 200f,
                width = 180f,
                height = 170f,
                text = "💡 Problem Statement\nWhat challenges are our users facing daily?",
                colorHex = 0xFFFEF08AL,
                rotationDegrees = -2f
            ),
            StickyNote(
                id = UUID.randomUUID().toString(),
                x = 400f,
                y = 200f,
                width = 180f,
                height = 170f,
                text = "⚡ Key Ideas\n- Infinite zoom canvas\n- Smooth Bézier pen\n- Sticky notes & shapes",
                colorHex = 0xFFBAE6FDL,
                rotationDegrees = 1.5f
            ),
            StickyNote(
                id = UUID.randomUUID().toString(),
                x = 720f,
                y = 200f,
                width = 180f,
                height = 170f,
                text = "🎯 Action Plan\nExport to PNG and collaborate seamlessly!",
                colorHex = 0xFFBBF7D0L,
                rotationDegrees = -1f
            )
        )

        val texts = listOf(
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 80f,
                y = 110f,
                text = "Welcome to Open Whiteboard ✨",
                colorHex = 0xFF0F172AL,
                fontSizeSp = 24f
            )
        )

        return Whiteboard(
            id = id,
            title = title,
            createdAt = now,
            updatedAt = now,
            backgroundStyle = BackgroundStyle.DOT_GRID,
            strokes = strokes,
            shapes = shapes,
            stickyNotes = stickyNotes,
            texts = texts
        )
    }

    private fun createFlowchartTemplate(id: String): Whiteboard {
        val now = System.currentTimeMillis()
        val shapes = listOf(
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.RECTANGLE,
                startX = 100f,
                startY = 150f,
                endX = 300f,
                endY = 240f,
                strokeColorHex = 0xFF2563EBL,
                fillColorHex = 0x222563EBL,
                strokeWidth = 4f
            ),
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.ARROW,
                startX = 300f,
                startY = 195f,
                endX = 420f,
                endY = 195f,
                strokeColorHex = 0xFF475569L,
                strokeWidth = 4f
            ),
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.DIAMOND,
                startX = 420f,
                startY = 140f,
                endX = 580f,
                endY = 250f,
                strokeColorHex = 0xFFD97706L,
                fillColorHex = 0x22D97706L,
                strokeWidth = 4f
            ),
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.ARROW,
                startX = 580f,
                startY = 195f,
                endX = 700f,
                endY = 195f,
                strokeColorHex = 0xFF475569L,
                strokeWidth = 4f
            ),
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.CIRCLE,
                startX = 700f,
                startY = 150f,
                endX = 850f,
                endY = 240f,
                strokeColorHex = 0xFF16A34AL,
                fillColorHex = 0x2216A34AL,
                strokeWidth = 4f
            )
        )

        val texts = listOf(
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 100f,
                y = 90f,
                text = "System Process Flow",
                colorHex = 0xFF0F172AL,
                fontSizeSp = 22f
            ),
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 140f,
                y = 185f,
                text = "User Input",
                colorHex = 0xFF2563EBL,
                fontSizeSp = 16f
            ),
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 470f,
                y = 185f,
                text = "Valid?",
                colorHex = 0xFFD97706L,
                fontSizeSp = 16f
            ),
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 745f,
                y = 185f,
                text = "Output",
                colorHex = 0xFF16A34AL,
                fontSizeSp = 16f
            )
        )

        return Whiteboard(
            id = id,
            title = "Process Flowchart",
            createdAt = now,
            updatedAt = now,
            backgroundStyle = BackgroundStyle.SQUARE_GRID,
            shapes = shapes,
            texts = texts
        )
    }

    private fun createMeetingNotesTemplate(id: String): Whiteboard {
        val now = System.currentTimeMillis()
        val stickyNotes = listOf(
            StickyNote(
                id = UUID.randomUUID().toString(),
                x = 100f,
                y = 180f,
                width = 220f,
                height = 200f,
                text = "📋 Agenda\n1. Project Status\n2. Design Review\n3. Milestones & Timeline",
                colorHex = 0xFFFEF08AL
            ),
            StickyNote(
                id = UUID.randomUUID().toString(),
                x = 360f,
                y = 180f,
                width = 220f,
                height = 200f,
                text = "🔑 Decisions\n- Use Jetpack Compose canvas\n- Support infinite pan & zoom\n- Offline-first local storage",
                colorHex = 0xFFBBF7D0L
            ),
            StickyNote(
                id = UUID.randomUUID().toString(),
                x = 620f,
                y = 180f,
                width = 220f,
                height = 200f,
                text = "📌 Action Items\n@Alex: Polish UI widgets\n@Jordan: Add laser trail\n@Sam: Test export engine",
                colorHex = 0xFFFBCFE8L
            )
        )

        val texts = listOf(
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 100f,
                y = 100f,
                text = "Sprint Planning & Standup",
                colorHex = 0xFF0F172AL,
                fontSizeSp = 24f
            )
        )

        return Whiteboard(
            id = id,
            title = "Team Standup Notes",
            createdAt = now,
            updatedAt = now,
            backgroundStyle = BackgroundStyle.RULED,
            stickyNotes = stickyNotes,
            texts = texts
        )
    }

    private fun createChalkboardTemplate(id: String): Whiteboard {
        val now = System.currentTimeMillis()
        val strokes = listOf(
            DrawingStroke(
                id = UUID.randomUUID().toString(),
                points = listOf(
                    CanvasPoint(120f, 260f),
                    CanvasPoint(200f, 240f),
                    CanvasPoint(280f, 290f),
                    CanvasPoint(360f, 210f),
                    CanvasPoint(440f, 270f)
                ),
                colorHex = 0xFFF8FAFC,
                strokeWidth = 4f,
                toolType = ToolType.PEN
            )
        )

        val shapes = listOf(
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.TRIANGLE,
                startX = 520f,
                startY = 180f,
                endX = 720f,
                endY = 320f,
                strokeColorHex = 0xFFFBBF24L,
                strokeWidth = 4f
            )
        )

        val texts = listOf(
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 120f,
                y = 120f,
                text = "Physics & Geometry Chalkboard",
                colorHex = 0xFFF8FAFC,
                fontSizeSp = 24f
            ),
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 120f,
                y = 180f,
                text = "E = mc²   |   a² + b² = c²",
                colorHex = 0xFF38BDF8L,
                fontSizeSp = 20f
            )
        )

        return Whiteboard(
            id = id,
            title = "Chalkboard Notes",
            createdAt = now,
            updatedAt = now,
            backgroundStyle = BackgroundStyle.CHALKBOARD,
            strokes = strokes,
            shapes = shapes,
            texts = texts
        )
    }

    private fun createBlueprintTemplate(id: String): Whiteboard {
        val now = System.currentTimeMillis()
        val shapes = listOf(
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.RECTANGLE,
                startX = 120f,
                startY = 180f,
                endX = 600f,
                endY = 480f,
                strokeColorHex = 0xFF38BDF8L,
                strokeWidth = 3f,
                isDashed = true
            ),
            CanvasShape(
                id = UUID.randomUUID().toString(),
                type = ShapeType.CIRCLE,
                startX = 200f,
                startY = 240f,
                endX = 360f,
                endY = 400f,
                strokeColorHex = 0xFFE0F2FEL,
                strokeWidth = 3f
            )
        )

        val texts = listOf(
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 120f,
                y = 110f,
                text = "Architectural Blueprint [Rev A]",
                colorHex = 0xFFE0F2FEL,
                fontSizeSp = 22f
            ),
            CanvasText(
                id = UUID.randomUUID().toString(),
                x = 400f,
                y = 300f,
                text = "Main Chamber",
                colorHex = 0xFF38BDF8L,
                fontSizeSp = 16f
            )
        )

        return Whiteboard(
            id = id,
            title = "Drafting Blueprint",
            createdAt = now,
            updatedAt = now,
            backgroundStyle = BackgroundStyle.BLUEPRINT,
            shapes = shapes,
            texts = texts
        )
    }
}

enum class TemplateType(val title: String, val subtitle: String) {
    BLANK("Blank Canvas", "Start with an empty infinite board"),
    BRAINSTORMING("Idea Brainstorming", "Sticky notes for problems, ideas & actions"),
    FLOWCHART("Process Flowchart", "Connected workflow nodes and decision blocks"),
    MEETING_NOTES("Meeting & Standup", "Agenda, key decisions, and action items"),
    CHALKBOARD("Dark Chalkboard", "Math, equations, and diagram board"),
    BLUEPRINT("Technical Blueprint", "Cyan drafting grid with architectural frames")
}
