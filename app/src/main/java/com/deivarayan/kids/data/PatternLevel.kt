package com.deivarayan.kids.data

enum class PatternLevel(val displayName: String, val colorCount: Int, val blankCount: Int) {
    BEGINNER("Beginner", colorCount = 2, blankCount = 1),
    INTERMEDIATE("Intermediate", colorCount = 3, blankCount = 1),
    EXPERT("Expert", colorCount = 4, blankCount = 2),
}
