package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TemplateType
import com.example.data.WhiteboardRepository
import com.example.model.BackgroundStyle
import com.example.model.CanvasPoint
import com.example.model.CanvasShape
import com.example.model.DrawingStroke
import com.example.model.ShapeType
import com.example.model.StickyNote
import com.example.model.ToolType
import com.example.model.Whiteboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Open Whiteboard", appName)
    }

    @Test
    fun `whiteboard model handles strokes shapes and sticky notes`() {
        val stroke = DrawingStroke(
            id = UUID.randomUUID().toString(),
            points = listOf(CanvasPoint(10f, 20f), CanvasPoint(30f, 40f)),
            colorHex = 0xFF2563EBL,
            strokeWidth = 6f,
            toolType = ToolType.PEN
        )
        val shape = CanvasShape(
            id = UUID.randomUUID().toString(),
            type = ShapeType.RECTANGLE,
            startX = 50f,
            startY = 50f,
            endX = 150f,
            endY = 150f,
            strokeColorHex = 0xFFDC2626L
        )
        val note = StickyNote(
            id = UUID.randomUUID().toString(),
            x = 200f,
            y = 200f,
            text = "Brainstorming note"
        )

        val board = Whiteboard(
            id = "test-board",
            title = "Test Board",
            backgroundStyle = BackgroundStyle.DOT_GRID,
            strokes = listOf(stroke),
            shapes = listOf(shape),
            stickyNotes = listOf(note)
        )

        assertEquals("Test Board", board.title)
        assertEquals(1, board.strokes.size)
        assertEquals(1, board.shapes.size)
        assertEquals(1, board.stickyNotes.size)
        assertEquals(BackgroundStyle.DOT_GRID, board.backgroundStyle)
    }
}
