package com.deivarayan.kids.ui.games

data class GameDef(
    val id: String,
    val title: String,
    val emoji: String,
    val color: Long,
)

const val GAME_ID_WORD_SPELLING = "word_spelling"
const val GAME_ID_PHONICS = "phonics"
const val GAME_ID_LISTENING = "listening"
const val GAME_ID_MEMORY = "memory"
const val GAME_ID_SIMON_SAYS = "simon_says"
const val GAME_ID_PATTERN = "pattern"
const val GAME_ID_RHYME = "rhyme"
const val GAME_ID_QUICK_TAP = "quick_tap"
const val GAME_ID_COLOR_MATCH = "color_match"
const val GAME_ID_WHERE_WAS_IT = "where_was_it"
const val GAME_ID_NUMBER_SEQUENCE = "number_sequence"

object GameCatalog {
    val games = listOf(
        GameDef(
            id = GAME_ID_WORD_SPELLING,
            title = "Word Spelling",
            emoji = "🔤",
            color = 0xFFFF7A18,
        ),
        GameDef(
            id = GAME_ID_PHONICS,
            title = "Phonics",
            emoji = "🔊",
            color = 0xFF00C48C,
        ),
        GameDef(
            id = GAME_ID_LISTENING,
            title = "Listening",
            emoji = "👂",
            color = 0xFF1976D2,
        ),
        GameDef(
            id = GAME_ID_MEMORY,
            title = "Memory",
            emoji = "🧠",
            color = 0xFFD32F2F,
        ),
        GameDef(
            id = GAME_ID_SIMON_SAYS,
            title = "Simon Says",
            emoji = "🎮",
            color = 0xFF00838F,
        ),
        GameDef(
            id = GAME_ID_PATTERN,
            title = "Pattern Game",
            emoji = "🧩",
            color = 0xFFFFC107,
        ),
        GameDef(
            id = GAME_ID_RHYME,
            title = "Rhyme Time",
            emoji = "🎵",
            color = 0xFFEC407A,
        ),
        GameDef(
            id = GAME_ID_QUICK_TAP,
            title = "Quick Tap",
            emoji = "⚡",
            color = 0xFF512DA8,
        ),
        GameDef(
            id = GAME_ID_COLOR_MATCH,
            title = "Color Match",
            emoji = "🎨",
            color = 0xFF00ACC1,
        ),
        GameDef(
            id = GAME_ID_WHERE_WAS_IT,
            title = "Where Was It?",
            emoji = "🔎",
            color = 0xFF7B1FA2,
        ),
        GameDef(
            id = GAME_ID_NUMBER_SEQUENCE,
            title = "Number Sequence",
            emoji = "🔢",
            color = 0xFF43A047,
        ),
    )
}
