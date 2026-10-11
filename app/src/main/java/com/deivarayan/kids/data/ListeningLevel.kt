package com.deivarayan.kids.data

enum class ListeningLevel(
    val displayName: String,
    val tileCount: Int,
    val maxNumber: Int,
    val showThumbsUpOnCorrect: Boolean,
) {
    BEGINNER("Beginner", tileCount = 2, maxNumber = 10, showThumbsUpOnCorrect = false),
    INTERMEDIATE("Intermediate", tileCount = 4, maxNumber = 50, showThumbsUpOnCorrect = false),
    EXPERT("Expert", tileCount = 8, maxNumber = 100, showThumbsUpOnCorrect = true),
}
