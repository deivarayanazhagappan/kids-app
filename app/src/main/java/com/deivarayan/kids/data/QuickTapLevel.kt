package com.deivarayan.kids.data

/**
 * Difficulty tuning for the Quick Tap reaction game. Each round shows [minTargets]..[maxTargets]
 * images at once; the tap-time budget for a round is [baseTimeMs] plus [perTargetTimeMs] for
 * every image shown, so rounds with more images get proportionally more time to stay achievable.
 */
enum class QuickTapLevel(
    val displayName: String,
    val roundCount: Int,
    val minDelayMs: Long,
    val maxDelayMs: Long,
    val targetSizeDp: Int,
    val minTargets: Int,
    val maxTargets: Int,
    val baseTimeMs: Long,
    val perTargetTimeMs: Long,
) {
    BEGINNER(
        displayName = "Beginner",
        roundCount = 25,
        minDelayMs = 300L,
        maxDelayMs = 700L,
        targetSizeDp = 92,
        minTargets = 2,
        maxTargets = 2,
        baseTimeMs = 500L,
        perTargetTimeMs = 1000L,
    ),
    INTERMEDIATE(
        displayName = "Intermediate",
        roundCount = 25,
        minDelayMs = 250L,
        maxDelayMs = 550L,
        targetSizeDp = 70,
        minTargets = 5,
        maxTargets = 5,
        baseTimeMs = 600L,
        perTargetTimeMs = 750L,
    ),
    EXPERT(
        displayName = "Expert",
        roundCount = 25,
        minDelayMs = 200L,
        maxDelayMs = 400L,
        targetSizeDp = 52,
        minTargets = 8,
        maxTargets = 8,
        baseTimeMs = 750L,
        perTargetTimeMs = 550L,
    ),
}
