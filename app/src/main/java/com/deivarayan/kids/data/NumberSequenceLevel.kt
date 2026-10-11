package com.deivarayan.kids.data

enum class NumberSequenceLevel(val displayName: String, val numberCount: Int, val blankCount: Int) {
    BEGINNER("Beginner", numberCount = 5, blankCount = 1),
    INTERMEDIATE("Intermediate", numberCount = 5, blankCount = 2),
    EXPERT("Expert", numberCount = 10, blankCount = 3),
}
