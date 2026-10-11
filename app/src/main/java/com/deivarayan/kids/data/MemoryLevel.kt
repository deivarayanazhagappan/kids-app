package com.deivarayan.kids.data

enum class MemoryLevel(val displayName: String, val pairCount: Int, val columns: Int) {
    BEGINNER("Beginner", pairCount = 20, columns = 5),
    INTERMEDIATE("Intermediate", pairCount = 30, columns = 6),
    EXPERT("Expert", pairCount = 60, columns = 8),
}
