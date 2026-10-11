package com.deivarayan.kids.data

/**
 * Difficulty tuning for the "Where Was It?" spatial-memory game. A picture flashes for
 * [showDurationMs] at one cell of a [gridColumns] x [gridColumns] grid, then the grid goes
 * blank and the player taps the cell where it appeared.
 */
enum class WhereWasItLevel(
    val displayName: String,
    val roundCount: Int,
    val gridColumns: Int,
    val showDurationMs: Long,
    val minDelayMs: Long,
    val maxDelayMs: Long,
) {
    BEGINNER(
        displayName = "Beginner",
        roundCount = 10,
        gridColumns = 3,
        showDurationMs = 1500L,
        minDelayMs = 500L,
        maxDelayMs = 1000L,
    ),
    INTERMEDIATE(
        displayName = "Intermediate",
        roundCount = 12,
        gridColumns = 4,
        showDurationMs = 1100L,
        minDelayMs = 400L,
        maxDelayMs = 900L,
    ),
    EXPERT(
        displayName = "Expert",
        roundCount = 15,
        gridColumns = 5,
        showDurationMs = 800L,
        minDelayMs = 300L,
        maxDelayMs = 700L,
    ),
}
