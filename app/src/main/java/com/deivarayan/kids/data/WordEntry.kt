package com.deivarayan.kids.data

data class WordEntry(
    val text: String,
    val emoji: String,
    val category: String,
    val imageQuery: String,
)

enum class GameLanguage(val displayName: String, val ttsLocaleTag: String) {
    ENGLISH("English", "en-US"),
    GERMAN("Deutsch", "de-DE"),
}
