package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary App Colors - Creative Modern Indigo & Slate
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val Indigo600 = Color(0xFF4F46E5)
val Indigo500 = Color(0xFF6366F1)
val Indigo400 = Color(0xFF818CF8)
val Indigo100 = Color(0xFFE0E7FF)

val Cyan500 = Color(0xFF06B6D4)
val Emerald500 = Color(0xFF10B981)
val Amber500 = Color(0xFFF59E0B)
val Rose500 = Color(0xFFF43F5E)
val Violet500 = Color(0xFF8B5CF6)

// Default Curated Whiteboard Inks (Hex values)
object WhiteboardColors {
    val BlackInk = Color(0xFF0F172A)
    val SlateInk = Color(0xFF475569)
    val BlueInk = Color(0xFF2563EB)
    val IndigoInk = Color(0xFF4F46E5)
    val PurpleInk = Color(0xFF7C3AED)
    val PinkInk = Color(0xFFDB2777)
    val RedInk = Color(0xFFDC2626)
    val OrangeInk = Color(0xFFEA580C)
    val AmberInk = Color(0xFFD97706)
    val GreenInk = Color(0xFF16A34A)
    val TealInk = Color(0xFF0D9488)
    val CyanInk = Color(0xFF0284C7)
    val WhiteInk = Color(0xFFF8FAFC)
    val NeonYellow = Color(0xFFEAB308)

    val DefaultPalette = listOf(
        BlackInk,
        BlueInk,
        RedInk,
        GreenInk,
        AmberInk,
        PurpleInk,
        TealInk,
        WhiteInk
    )

    val ExtendedPalette = listOf(
        BlackInk, SlateInk, BlueInk, IndigoInk,
        PurpleInk, PinkInk, RedInk, OrangeInk,
        AmberInk, GreenInk, TealInk, CyanInk,
        WhiteInk, NeonYellow
    )
}

// Sticky Note Color Options
object StickyNoteColors {
    val Yellow = Color(0xFFFEF08A)
    val Cyan = Color(0xFFBAE6FD)
    val Green = Color(0xFFBBF7D0)
    val Pink = Color(0xFFFBCFE8)
    val Orange = Color(0xFFFED7AA)
    val Purple = Color(0xFFDDD6FE)

    val All = listOf(Yellow, Cyan, Green, Pink, Orange, Purple)
}
