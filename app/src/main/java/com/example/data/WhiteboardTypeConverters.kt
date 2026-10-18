package com.example.data

import androidx.room.TypeConverter
import com.example.model.BackgroundStyle
import com.example.model.CanvasShape
import com.example.model.CanvasText
import com.example.model.DrawingStroke
import com.example.model.StickyNote
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WhiteboardTypeConverters {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val strokeListType = Types.newParameterizedType(List::class.java, DrawingStroke::class.java)
    private val shapeListType = Types.newParameterizedType(List::class.java, CanvasShape::class.java)
    private val stickyListType = Types.newParameterizedType(List::class.java, StickyNote::class.java)
    private val textListType = Types.newParameterizedType(List::class.java, CanvasText::class.java)

    private val strokeAdapter = moshi.adapter<List<DrawingStroke>>(strokeListType)
    private val shapeAdapter = moshi.adapter<List<CanvasShape>>(shapeListType)
    private val stickyAdapter = moshi.adapter<List<StickyNote>>(stickyListType)
    private val textAdapter = moshi.adapter<List<CanvasText>>(textListType)

    @TypeConverter
    fun fromBackgroundStyle(style: BackgroundStyle): String = style.name

    @TypeConverter
    fun toBackgroundStyle(name: String): BackgroundStyle =
        try { BackgroundStyle.valueOf(name) } catch (_: Exception) { BackgroundStyle.DOT_GRID }

    @TypeConverter
    fun fromStrokes(strokes: List<DrawingStroke>): String = strokeAdapter.toJson(strokes)

    @TypeConverter
    fun toStrokes(json: String): List<DrawingStroke> =
        try { strokeAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }

    @TypeConverter
    fun fromShapes(shapes: List<CanvasShape>): String = shapeAdapter.toJson(shapes)

    @TypeConverter
    fun toShapes(json: String): List<CanvasShape> =
        try { shapeAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }

    @TypeConverter
    fun fromStickyNotes(notes: List<StickyNote>): String = stickyAdapter.toJson(notes)

    @TypeConverter
    fun toStickyNotes(json: String): List<StickyNote> =
        try { stickyAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }

    @TypeConverter
    fun fromTexts(texts: List<CanvasText>): String = textAdapter.toJson(texts)

    @TypeConverter
    fun toTexts(json: String): List<CanvasText> =
        try { textAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
}
