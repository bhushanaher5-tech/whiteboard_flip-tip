package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TemplateType
import com.example.data.WhiteboardDatabase
import com.example.data.WhiteboardRepository
import com.example.model.BackgroundStyle
import com.example.model.CanvasPoint
import com.example.model.CanvasShape
import com.example.model.CanvasText
import com.example.model.DrawingStroke
import com.example.model.LaserPoint
import com.example.model.ShapeType
import com.example.model.StickyNote
import com.example.model.ToolType
import com.example.model.Whiteboard
import com.example.ui.theme.StickyNoteColors
import com.example.ui.theme.WhiteboardColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.UUID

data class BoardSnapshot(
    val strokes: List<DrawingStroke>,
    val shapes: List<CanvasShape>,
    val stickyNotes: List<StickyNote>,
    val texts: List<CanvasText>
)

class WhiteboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WhiteboardRepository

    private val _allBoards = MutableStateFlow<List<Whiteboard>>(emptyList())
    val allBoards: StateFlow<List<Whiteboard>> = _allBoards.asStateFlow()

    private val _currentBoard = MutableStateFlow<Whiteboard>(
        Whiteboard(
            id = UUID.randomUUID().toString(),
            title = "My Whiteboard",
            backgroundStyle = BackgroundStyle.DOT_GRID
        )
    )
    val currentBoard: StateFlow<Whiteboard> = _currentBoard.asStateFlow()

    // Tool states
    private val _activeTool = MutableStateFlow(ToolType.PEN)
    val activeTool: StateFlow<ToolType> = _activeTool.asStateFlow()

    private val _activeShapeType = MutableStateFlow(ShapeType.RECTANGLE)
    val activeShapeType: StateFlow<ShapeType> = _activeShapeType.asStateFlow()

    private val _strokeColor = MutableStateFlow(WhiteboardColors.BlackInk)
    val strokeColor: StateFlow<Color> = _strokeColor.asStateFlow()

    private val _strokeWidth = MutableStateFlow(6f)
    val strokeWidth: StateFlow<Float> = _strokeWidth.asStateFlow()

    private val _isDashed = MutableStateFlow(false)
    val isDashed: StateFlow<Boolean> = _isDashed.asStateFlow()

    private val _activeStickyColor = MutableStateFlow(StickyNoteColors.Yellow)
    val activeStickyColor: StateFlow<Color> = _activeStickyColor.asStateFlow()

    // Canvas view transform
    private val _scale = MutableStateFlow(1.0f)
    val scale: StateFlow<Float> = _scale.asStateFlow()

    private val _panOffset = MutableStateFlow(Offset.Zero)
    val panOffset: StateFlow<Offset> = _panOffset.asStateFlow()

    // Laser trail
    private val _laserPoints = MutableStateFlow<List<LaserPoint>>(emptyList())
    val laserPoints: StateFlow<List<LaserPoint>> = _laserPoints.asStateFlow()
    private var laserPruneJob: Job? = null

    // Undo / Redo stacks
    private val undoStack = ArrayDeque<BoardSnapshot>()
    private val redoStack = ArrayDeque<BoardSnapshot>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // UI overlays
    private val _isZenMode = MutableStateFlow(false)
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    private val _isBoardDrawerOpen = MutableStateFlow(false)
    val isBoardDrawerOpen: StateFlow<Boolean> = _isBoardDrawerOpen.asStateFlow()

    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen.asStateFlow()

    private val _isClearConfirmOpen = MutableStateFlow(false)
    val isClearConfirmOpen: StateFlow<Boolean> = _isClearConfirmOpen.asStateFlow()

    private val _isRenameDialogOpen = MutableStateFlow(false)
    val isRenameDialogOpen: StateFlow<Boolean> = _isRenameDialogOpen.asStateFlow()

    private val _isHelpDialogOpen = MutableStateFlow(false)
    val isHelpDialogOpen: StateFlow<Boolean> = _isHelpDialogOpen.asStateFlow()

    // Selection
    private val _selectedElementId = MutableStateFlow<String?>(null)
    val selectedElementId: StateFlow<String?> = _selectedElementId.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        val database = WhiteboardDatabase.getDatabase(application)
        repository = WhiteboardRepository(database.whiteboardDao())

        viewModelScope.launch {
            repository.getAllWhiteboards().collectLatest { list ->
                _allBoards.value = list
                if (list.isNotEmpty() && _currentBoard.value.strokes.isEmpty() && _currentBoard.value.shapes.isEmpty() && _currentBoard.value.stickyNotes.isEmpty()) {
                    // Load most recent board
                    val mostRecent = list.first()
                    _currentBoard.value = mostRecent
                    adjustColorForBackground(mostRecent.backgroundStyle)
                } else if (list.isEmpty()) {
                    val starter = repository.ensureDefaultBoardExists()
                    _currentBoard.value = starter
                }
            }
        }
    }

    private fun pushSnapshot() {
        val curr = _currentBoard.value
        undoStack.push(
            BoardSnapshot(
                strokes = curr.strokes,
                shapes = curr.shapes,
                stickyNotes = curr.stickyNotes,
                texts = curr.texts
            )
        )
        // Cap undo stack to 40 steps
        if (undoStack.size > 40) {
            undoStack.removeLast()
        }
        redoStack.clear()
        updateUndoRedoState()
    }

    private fun updateUndoRedoState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _currentBoard.value
        redoStack.push(
            BoardSnapshot(
                strokes = current.strokes,
                shapes = current.shapes,
                stickyNotes = current.stickyNotes,
                texts = current.texts
            )
        )
        val prev = undoStack.pop()
        _currentBoard.value = current.copy(
            strokes = prev.strokes,
            shapes = prev.shapes,
            stickyNotes = prev.stickyNotes,
            texts = prev.texts
        )
        updateUndoRedoState()
        triggerAutoSave()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _currentBoard.value
        undoStack.push(
            BoardSnapshot(
                strokes = current.strokes,
                shapes = current.shapes,
                stickyNotes = current.stickyNotes,
                texts = current.texts
            )
        )
        val next = redoStack.pop()
        _currentBoard.value = current.copy(
            strokes = next.strokes,
            shapes = next.shapes,
            stickyNotes = next.stickyNotes,
            texts = next.texts
        )
        updateUndoRedoState()
        triggerAutoSave()
    }

    fun setTool(tool: ToolType) {
        _activeTool.value = tool
        if (tool != ToolType.SELECT) {
            _selectedElementId.value = null
        }
    }

    fun setShapeType(shape: ShapeType) {
        _activeShapeType.value = shape
        _activeTool.value = ToolType.SHAPE
    }

    fun setStrokeColor(color: Color) {
        _strokeColor.value = color
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth.value = width
    }

    fun toggleDashed() {
        _isDashed.value = !_isDashed.value
    }

    fun setStickyColor(color: Color) {
        _activeStickyColor.value = color
    }

    fun setBackgroundStyle(style: BackgroundStyle) {
        val curr = _currentBoard.value
        _currentBoard.value = curr.copy(backgroundStyle = style)
        adjustColorForBackground(style)
        triggerAutoSave()
    }

    private fun adjustColorForBackground(style: BackgroundStyle) {
        // If switching to dark chalkboard or blueprint, default pen should be white/light
        if (style == BackgroundStyle.CHALKBOARD && _strokeColor.value == WhiteboardColors.BlackInk) {
            _strokeColor.value = WhiteboardColors.WhiteInk
        } else if (style != BackgroundStyle.CHALKBOARD && style != BackgroundStyle.BLUEPRINT && _strokeColor.value == WhiteboardColors.WhiteInk) {
            _strokeColor.value = WhiteboardColors.BlackInk
        }
    }

    // Drawing operations
    fun addStroke(stroke: DrawingStroke) {
        pushSnapshot()
        val curr = _currentBoard.value
        _currentBoard.value = curr.copy(strokes = curr.strokes + stroke)
        triggerAutoSave()
    }

    fun addShape(shape: CanvasShape) {
        pushSnapshot()
        val curr = _currentBoard.value
        _currentBoard.value = curr.copy(shapes = curr.shapes + shape)
        triggerAutoSave()
    }

    fun addStickyNote(x: Float, y: Float) {
        pushSnapshot()
        val curr = _currentBoard.value
        val newNote = StickyNote(
            id = UUID.randomUUID().toString(),
            x = x - 90f,
            y = y - 90f,
            width = 180f,
            height = 170f,
            text = "Tap to edit note",
            colorHex = _activeStickyColor.value.toArgb().toLong() and 0xFFFFFFFFL
        )
        _currentBoard.value = curr.copy(stickyNotes = curr.stickyNotes + newNote)
        triggerAutoSave()
    }

    fun updateStickyNote(id: String, text: String, colorHex: Long? = null) {
        val curr = _currentBoard.value
        val updated = curr.stickyNotes.map { note ->
            if (note.id == id) {
                note.copy(
                    text = text,
                    colorHex = colorHex ?: note.colorHex
                )
            } else note
        }
        _currentBoard.value = curr.copy(stickyNotes = updated)
        triggerAutoSave()
    }

    fun moveStickyNote(id: String, deltaX: Float, deltaY: Float) {
        val curr = _currentBoard.value
        val updated = curr.stickyNotes.map { note ->
            if (note.id == id) {
                note.copy(x = note.x + deltaX, y = note.y + deltaY)
            } else note
        }
        _currentBoard.value = curr.copy(stickyNotes = updated)
    }

    fun onStickyDragEnd() {
        pushSnapshot()
        triggerAutoSave()
    }

    fun deleteStickyNote(id: String) {
        pushSnapshot()
        val curr = _currentBoard.value
        _currentBoard.value = curr.copy(stickyNotes = curr.stickyNotes.filter { it.id != id })
        triggerAutoSave()
    }

    fun addText(x: Float, y: Float, text: String) {
        pushSnapshot()
        val curr = _currentBoard.value
        val newText = CanvasText(
            id = UUID.randomUUID().toString(),
            x = x,
            y = y,
            text = text,
            colorHex = _strokeColor.value.toArgb().toLong() and 0xFFFFFFFFL,
            fontSizeSp = 20f
        )
        _currentBoard.value = curr.copy(texts = curr.texts + newText)
        triggerAutoSave()
    }

    fun updateText(id: String, newText: String) {
        pushSnapshot()
        val curr = _currentBoard.value
        _currentBoard.value = curr.copy(
            texts = curr.texts.map { if (it.id == id) it.copy(text = newText) else it }
        )
        triggerAutoSave()
    }

    fun deleteText(id: String) {
        pushSnapshot()
        val curr = _currentBoard.value
        _currentBoard.value = curr.copy(texts = curr.texts.filter { it.id != id })
        triggerAutoSave()
    }

    // Laser pointer
    fun addLaserPoint(x: Float, y: Float) {
        val updated = _laserPoints.value + LaserPoint(x, y)
        _laserPoints.value = updated
        startLaserDecay()
    }

    private fun startLaserDecay() {
        if (laserPruneJob?.isActive == true) return
        laserPruneJob = viewModelScope.launch {
            while (_laserPoints.value.isNotEmpty()) {
                delay(80)
                val cutoff = System.currentTimeMillis() - 1200L
                val pruned = _laserPoints.value.filter { it.timestamp > cutoff }
                _laserPoints.value = pruned
            }
        }
    }

    // Erase collision
    fun eraseAt(x: Float, y: Float, radius: Float = 24f) {
        val curr = _currentBoard.value
        val hitStrokes = curr.strokes.filter { stroke ->
            stroke.points.any { p ->
                val dx = p.x - x
                val dy = p.y - y
                (dx * dx + dy * dy) <= (radius * radius)
            }
        }
        val hitShapes = curr.shapes.filter { shape ->
            val minX = minOf(shape.startX, shape.endX) - radius
            val maxX = maxOf(shape.startX, shape.endX) + radius
            val minY = minOf(shape.startY, shape.endY) - radius
            val maxY = maxOf(shape.startY, shape.endY) + radius
            x in minX..maxX && y in minY..maxY
        }

        if (hitStrokes.isNotEmpty() || hitShapes.isNotEmpty()) {
            pushSnapshot()
            val remainingStrokes = curr.strokes.filterNot { hitStrokes.contains(it) }
            val remainingShapes = curr.shapes.filterNot { hitShapes.contains(it) }
            _currentBoard.value = curr.copy(
                strokes = remainingStrokes,
                shapes = remainingShapes
            )
            triggerAutoSave()
        }
    }

    fun clearBoard() {
        val curr = _currentBoard.value
        if (curr.strokes.isEmpty() && curr.shapes.isEmpty() && curr.stickyNotes.isEmpty() && curr.texts.isEmpty()) {
            return
        }
        pushSnapshot()
        _currentBoard.value = curr.copy(
            strokes = emptyList(),
            shapes = emptyList(),
            stickyNotes = emptyList(),
            texts = emptyList()
        )
        triggerAutoSave()
    }

    // Navigation and zoom
    fun onPanAndZoom(panChange: Offset, zoomChange: Float) {
        val newScale = (_scale.value * zoomChange).coerceIn(0.2f, 5.0f)
        _scale.value = newScale
        _panOffset.value = _panOffset.value + panChange
    }

    fun resetView() {
        _scale.value = 1.0f
        _panOffset.value = Offset.Zero
    }

    fun zoomIn() {
        _scale.value = (_scale.value * 1.25f).coerceIn(0.2f, 5.0f)
    }

    fun zoomOut() {
        _scale.value = (_scale.value / 1.25f).coerceIn(0.2f, 5.0f)
    }

    // Board management
    fun createNewBoard(template: TemplateType = TemplateType.BLANK) {
        val newBoard = repository.createTemplate(template)
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoState()
        _currentBoard.value = newBoard
        resetView()
        adjustColorForBackground(newBoard.backgroundStyle)
        triggerAutoSave()
        _isBoardDrawerOpen.value = false
    }

    fun selectBoard(board: Whiteboard) {
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoState()
        _currentBoard.value = board
        resetView()
        adjustColorForBackground(board.backgroundStyle)
        _isBoardDrawerOpen.value = false
    }

    fun renameBoard(newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        val updated = _currentBoard.value.copy(title = trimmed)
        _currentBoard.value = updated
        triggerAutoSave()
        _isRenameDialogOpen.value = false
    }

    fun duplicateCurrentBoard() {
        val curr = _currentBoard.value
        val duplicated = curr.copy(
            id = UUID.randomUUID().toString(),
            title = "${curr.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveWhiteboard(duplicated)
            selectBoard(duplicated)
        }
    }

    fun deleteCurrentBoard() {
        val idToDelete = _currentBoard.value.id
        viewModelScope.launch {
            repository.deleteWhiteboard(idToDelete)
            val remaining = _allBoards.value.filter { it.id != idToDelete }
            if (remaining.isNotEmpty()) {
                selectBoard(remaining.first())
            } else {
                createNewBoard(TemplateType.BLANK)
            }
        }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(400) // Debounce auto-save
            repository.saveWhiteboard(_currentBoard.value)
        }
    }

    // Dialog toggles
    fun toggleZenMode() {
        _isZenMode.value = !_isZenMode.value
    }

    fun openBoardDrawer() {
        _isBoardDrawerOpen.value = true
    }

    fun closeBoardDrawer() {
        _isBoardDrawerOpen.value = false
    }

    fun openExportDialog() {
        _isExportDialogOpen.value = true
    }

    fun closeExportDialog() {
        _isExportDialogOpen.value = false
    }

    fun openClearConfirm() {
        _isClearConfirmOpen.value = true
    }

    fun closeClearConfirm() {
        _isClearConfirmOpen.value = false
    }

    fun openRenameDialog() {
        _isRenameDialogOpen.value = true
    }

    fun closeRenameDialog() {
        _isRenameDialogOpen.value = false
    }

    fun openHelpDialog() {
        _isHelpDialogOpen.value = true
    }

    fun closeHelpDialog() {
        _isHelpDialogOpen.value = false
    }

    fun selectElement(id: String?) {
        _selectedElementId.value = id
    }
}
