package com.deivarayan.kids.ui.quicktap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.QuickTapLevel
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RoundPhase { WAITING, SHOWING }

enum class QuickTapResultTier { EXCELLENT, GOOD, TRY_AGAIN }

data class QuickTapTarget(val id: Int, val xFraction: Float, val yFraction: Float, val tapped: Boolean = false)

data class QuickTapUiState(
    val phase: RoundPhase = RoundPhase.WAITING,
    val targets: List<QuickTapTarget> = emptyList(),
    val roundNumber: Int = 0,
    val totalRounds: Int = 0,
    val totalHits: Int = 0,
    val totalTargets: Int = 0,
    val isSessionComplete: Boolean = false,
    val resultTier: QuickTapResultTier? = null,
    val resultPercent: Int = 0,
    val isShowingIntro: Boolean = false,
    val motivationText: String? = null,
) {
    val inputEnabled: Boolean get() = phase == RoundPhase.SHOWING && !isSessionComplete
    val tappedThisRound: Int get() = targets.count { it.tapped }
}

private const val ROUND_CLEAR_DELAY_MS = 120L
private const val MOTIVATION_INTERVAL = 5
private const val MOTIVATION_DISPLAY_MS = 900L
private const val ASSUMED_PLAY_AREA_DP = 340f
private const val MIN_DISTANCE_MULTIPLIER = 1.4f
private const val MAX_PLACEMENT_ATTEMPTS = 60

class QuickTapGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: QuickTapLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    val nextLevel: QuickTapLevel? = when (level) {
        QuickTapLevel.BEGINNER -> QuickTapLevel.INTERMEDIATE
        QuickTapLevel.INTERMEDIATE -> QuickTapLevel.EXPERT
        QuickTapLevel.EXPERT -> null
    }

    private val introText = if (language == GameLanguage.ENGLISH) {
        "Tap every circle as fast as you can before time runs out!"
    } else {
        "Tippe so schnell du kannst auf jeden Kreis, bevor die Zeit abläuft!"
    }
    private val excellentPhrase = if (language == GameLanguage.ENGLISH) "Excellent!" else "Ausgezeichnet!"
    private val goodPhrase = if (language == GameLanguage.ENGLISH) "Good job!" else "Gut gemacht!"
    private val tryAgainPhrase = if (language == GameLanguage.ENGLISH) {
        "Try again to increase your speed!"
    } else {
        "Versuch es nochmal, um schneller zu werden!"
    }
    private val motivationPhrases = if (language == GameLanguage.ENGLISH) {
        listOf("Keep going!", "You're doing great!", "Almost there!", "Nice work!", "You've got this!")
    } else {
        listOf("Weiter so!", "Du machst das super!", "Fast geschafft!", "Gute Arbeit!", "Du schaffst das!")
    }

    private val _uiState = MutableStateFlow(QuickTapUiState())
    val uiState: StateFlow<QuickTapUiState> = _uiState

    private var roundJob: Job? = null
    private var nextTargetId = 0

    init {
        viewModelScope.launch {
            val ready = ttsController.initialize()
            if (ready) {
                val locale = when (language) {
                    GameLanguage.ENGLISH -> Locale.US
                    GameLanguage.GERMAN -> Locale.GERMANY
                }
                ttsController.setLocale(locale)
                ttsController.setSpeechRate(1.0f)
                showIntroThenStart()
            }
        }
    }

    fun onTargetTapped(targetId: Int) {
        val state = _uiState.value
        if (state.phase != RoundPhase.SHOWING) return
        val target = state.targets.find { it.id == targetId } ?: return
        if (target.tapped) return

        val updatedTargets = state.targets.map { if (it.id == targetId) it.copy(tapped = true) else it }
        val allTapped = updatedTargets.all { it.tapped }
        _uiState.update { it.copy(targets = updatedTargets, totalHits = it.totalHits + 1) }

        if (allTapped) {
            roundJob?.cancel()
            roundJob = viewModelScope.launch {
                delay(ROUND_CLEAR_DELAY_MS)
                advance(state.roundNumber)
            }
        }
    }

    fun playAgain() {
        roundJob?.cancel()
        _uiState.value = QuickTapUiState()
        startRound(1)
    }

    fun onPause() {
        ttsController.stop()
        roundJob?.cancel()
    }

    fun onResume() {
        val state = _uiState.value
        if (state.isSessionComplete) return
        if (state.isShowingIntro) {
            showIntroThenStart()
            return
        }
        startRound(state.roundNumber.coerceAtLeast(1))
    }

    private fun showIntroThenStart() {
        roundJob?.cancel()
        _uiState.update { it.copy(isShowingIntro = true) }
        roundJob = viewModelScope.launch {
            ttsController.speak(introText)
            _uiState.update { it.copy(isShowingIntro = false) }
            startRound(1)
        }
    }

    private fun advance(roundNumber: Int) {
        if (roundNumber >= level.roundCount) {
            completeSession()
        } else if (roundNumber % MOTIVATION_INTERVAL == 0) {
            showMotivationThenStart(roundNumber + 1)
        } else {
            startRound(roundNumber + 1)
        }
    }

    private fun showMotivationThenStart(nextRoundNumber: Int) {
        roundJob?.cancel()
        val phrase = motivationPhrases.random()
        _uiState.update { it.copy(phase = RoundPhase.WAITING, targets = emptyList(), motivationText = phrase) }
        roundJob = viewModelScope.launch {
            ttsController.speak(phrase)
            delay(MOTIVATION_DISPLAY_MS)
            _uiState.update { it.copy(motivationText = null) }
            startRound(nextRoundNumber)
        }
    }

    private fun startRound(roundNumber: Int) {
        roundJob?.cancel()
        val targetCount = if (level.minTargets == level.maxTargets) {
            level.minTargets
        } else {
            Random.nextInt(level.minTargets, level.maxTargets + 1)
        }
        _uiState.update {
            it.copy(
                phase = RoundPhase.WAITING,
                targets = emptyList(),
                roundNumber = roundNumber,
                totalRounds = level.roundCount,
                totalTargets = it.totalTargets + targetCount,
                isSessionComplete = false,
            )
        }
        val delayMs = Random.nextLong(level.minDelayMs, level.maxDelayMs + 1)
        val windowMs = level.baseTimeMs + level.perTargetTimeMs * targetCount
        roundJob = viewModelScope.launch {
            delay(delayMs)
            val targets = generateTargets(targetCount)
            _uiState.update { it.copy(phase = RoundPhase.SHOWING, targets = targets) }
            delay(windowMs)
            val current = _uiState.value
            if (current.phase == RoundPhase.SHOWING && current.roundNumber == roundNumber) {
                advance(roundNumber)
            }
        }
    }

    private fun generateTargets(count: Int): List<QuickTapTarget> {
        val minDistance = (level.targetSizeDp / ASSUMED_PLAY_AREA_DP) * MIN_DISTANCE_MULTIPLIER
        val positions = mutableListOf<Pair<Float, Float>>()
        repeat(count) {
            var candidate: Pair<Float, Float>
            var attempts = 0
            do {
                candidate = 0.08f + Random.nextFloat() * 0.84f to 0.08f + Random.nextFloat() * 0.84f
                attempts++
            } while (
                attempts < MAX_PLACEMENT_ATTEMPTS &&
                positions.any { existing ->
                    val dx = existing.first - candidate.first
                    val dy = existing.second - candidate.second
                    kotlin.math.sqrt(dx * dx + dy * dy) < minDistance
                }
            )
            positions += candidate
        }
        return positions.map { (x, y) -> QuickTapTarget(id = nextTargetId++, xFraction = x, yFraction = y) }
    }

    private fun completeSession() {
        val state = _uiState.value
        val percent = if (state.totalTargets == 0) 0 else (state.totalHits * 100 / state.totalTargets)
        val tier = when {
            percent >= 90 -> QuickTapResultTier.EXCELLENT
            percent >= 60 -> QuickTapResultTier.GOOD
            else -> QuickTapResultTier.TRY_AGAIN
        }
        _uiState.update {
            it.copy(isSessionComplete = true, targets = emptyList(), resultTier = tier, resultPercent = percent)
        }
        val phrase = when (tier) {
            QuickTapResultTier.EXCELLENT -> excellentPhrase
            QuickTapResultTier.GOOD -> goodPhrase
            QuickTapResultTier.TRY_AGAIN -> tryAgainPhrase
        }
        viewModelScope.launch { ttsController.speak(phrase) }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class QuickTapGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: QuickTapLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return QuickTapGameViewModel(application, language, level) as T
    }
}
