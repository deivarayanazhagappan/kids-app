package com.deivarayan.kids.data

/** A named color swatch; [colorHex] is an ARGB Long (e.g. 0xFFE53935), matching [GameDef.color]. */
data class NamedColor(val english: String, val german: String, val colorHex: Long)

object ColorMatchData {
    val colors = listOf(
        NamedColor("Red", "Rot", 0xFFE53935),
        NamedColor("Blue", "Blau", 0xFF1E88E5),
        NamedColor("Green", "Grün", 0xFF43A047),
        NamedColor("Yellow", "Gelb", 0xFFFDD835),
        NamedColor("Orange", "Orange", 0xFFFB8C00),
        NamedColor("Purple", "Lila", 0xFF8E24AA),
        NamedColor("Pink", "Pink", 0xFFD81B60),
        NamedColor("Brown", "Braun", 0xFF6D4C41),
        NamedColor("Black", "Schwarz", 0xFF212121),
        NamedColor("Gray", "Grau", 0xFF757575),
    )
}
