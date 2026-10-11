package com.deivarayan.kids.data

enum class GameSpeed(
    val displayName: String,
    val letterRate: Float,
    val wordRate: Float,
    val letterPauseMs: Long,
) {
    BEGINNER("Beginner", 0.5f, 0.6f, 1000L),
    INTERMEDIATE("Intermediate", 0.65f, 0.85f, 500L),
    EXPERT("Expert", 0.8f, 1.0f,    50L),
}
