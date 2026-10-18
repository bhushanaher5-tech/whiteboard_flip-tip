package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BackgroundStyle
import com.example.model.CanvasShape
import com.example.model.CanvasText
import com.example.model.DrawingStroke
import com.example.model.StickyNote
import com.example.model.Whiteboard

@Entity(tableName = "whiteboards")
data class WhiteboardEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val backgroundStyle: BackgroundStyle,
    val strokes: List<DrawingStroke>,
    val shapes: List<CanvasShape>,
    val stickyNotes: List<StickyNote>,
    val texts: List<CanvasText>
) {
    fun toDomain(): Whiteboard = Whiteboard(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        backgroundStyle = backgroundStyle,
        strokes = strokes,
        shapes = shapes,
        stickyNotes = stickyNotes,
        texts = texts
    )

    companion object {
        fun fromDomain(domain: Whiteboard): WhiteboardEntity = WhiteboardEntity(
            id = domain.id,
            title = domain.title,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            backgroundStyle = domain.backgroundStyle,
            strokes = domain.strokes,
            shapes = domain.shapes,
            stickyNotes = domain.stickyNotes,
            texts = domain.texts
        )
    }
}
