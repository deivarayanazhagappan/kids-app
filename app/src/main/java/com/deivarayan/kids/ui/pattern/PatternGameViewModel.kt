package com.deivarayan.kids.ui.pattern

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.PatternLevel
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PatternOptionStatus { NEUTRAL, CORRECT, WRONG }

const val PATTERN_BLANK = "❓"

data class PatternUiState(
    val cells: List<String> = emptyList(),
    val activeCellIndex: Int = -1,
    val options: List<String> = emptyList(),
    val optionStatuses: List<PatternOptionStatus> = emptyList(),
    val roundNumber: Int = 0,
    val inputEnabled: Boolean = false,
    val isSessionComplete: Boolean = false,
)

private val PATTERN_ELEMENTS = listOf("🔴", "🟠", "🟡", "🟢", "🔵", "🟣")
private const val ROUND_LIMIT = 20
private const val CORRECT_DELAY_MS = 1200L
private const val WRONG_RESET_DELAY_MS = 900L

class PatternGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: PatternLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    val nextLevel: PatternLevel? = when (level) {
        PatternLevel.BEGINNER -> PatternLevel.INTERMEDIATE
        PatternLevel.INTERMEDIATE -> PatternLevel.EXPERT
        PatternLevel.EXPERT -> null
    }

    private val nextPhrase = if (language == GameLanguage.ENGLISH) "What comes next?" else "Was kommt als Nächstes?"
    private val missingPhrase = if (language == GameLanguage.ENGLISH) "What's missing?" else "Was fehlt?"
    private val correctPhrase = if (language == GameLanguage.ENGLISH) "Correct!" else "Richtig!"
    private val notCorrectPhrase = if (language == GameLanguage.ENGLISH) "Not correct. Try again." else "Nicht richtig. Versuch's nochmal."
    private val completePhrase = if (language == GameLanguage.ENGLISH) "Great job! You solved them all!" else "Toll gemacht! Du hast alle gelöst!"

    private val _uiState = MutableStateFlow(PatternUiState())
    val uiState: StateFlow<PatternUiState> = _uiState

    private var fullSequence: List<String> = emptyList()
    private var blankPositions: List<Int> = emptyList()
    private var blankPointer: Int = 0
    private var correctAnswer: String = ""
    private var roundJob: Job? = null

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
                startRound(1)
            }
        }
    }

    fun onOptionClicked(index: Int) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        if (state.optionStatuses.getOrNull(index) != PatternOptionStatus.NEUTRAL) return

        if (state.options[index] == correctAnswer) {
            _uiState.update { it.copy(optionStatuses = it.optionStatuses.replacedAt(index, PatternOptionStatus.CORRECT), inputEnabled = false) }
            roundJob = viewModelScope.launch {
                ttsController.speak(correctPhrase)
                delay(CORRECT_DELAY_MS)
                advanceAfterCorrect(state.roundNumber)
            }
        } else {
            _uiState.update { it.copy(optionStatuses = it.optionStatuses.replacedAt(index, PatternOptionStatus.WRONG), inputEnabled = false) }
            roundJob = viewModelScope.launch {
                ttsController.speak(notCorrectPhrase)
                delay(WRONG_RESET_DELAY_MS)
                _uiState.update { it.copy(optionStatuses = List(it.options.size) { PatternOptionStatus.NEUTRAL }, inputEnabled = true) }
            }
        }
    }

    fun playAgain() {
        roundJob?.cancel()
        startRound(1)
    }

    fun onPause() {
        ttsController.stop()
        roundJob?.cancel()
    }

    fun onResume() {
        val state = _uiState.value
        if (state.isSessionComplete) return
        if (!state.inputEnabled) startRound(state.roundNumber.coerceAtLeast(1))
    }

    private fun advanceAfterCorrect(roundNumber: Int) {
        val solvedIndex = blankPositions[blankPointer]
        val revealedCells = _uiState.value.cells.replacedAt(solvedIndex, correctAnswer)
        blankPointer++

        if (blankPointer < blankPositions.size) {
            val nextIndex = blankPositions[blankPointer]
            val answer = fullSequence[nextIndex]
            correctAnswer = answer
            val options = buildOptions(answer)
            _uiState.update {
                it.copy(
                    cells = revealedCells,
                    activeCellIndex = nextIndex,
                    options = options,
                    optionStatuses = List(options.size) { PatternOptionStatus.NEUTRAL },
                    inputEnabled = false,
                )
            }
            roundJob = viewModelScope.launch {
                delay(300)
                ttsController.speak(missingPhrase)
                _uiState.update { it.copy(inputEnabled = true) }
            }
        } else {
            _uiState.update { it.copy(cells = revealedCells) }
            if (roundNumber >= ROUND_LIMIT) {
                completeSession()
            } else {
                startRound(roundNumber + 1)
            }
        }
    }

    private fun buildOptions(answer: String): List<String> {
        val distractors = PATTERN_ELEMENTS.filter { it != answer }.shuffled().take(2)
        return (distractors + answer).shuffled()
    }

    private fun startRound(roundNumber: Int) {
        roundJob?.cancel()
        val period = level.colorCount
        val elements = PATTERN_ELEMENTS.shuffled().take(period)
        val sequenceLength = period * 2 + 1
        fullSequence = (0 until sequenceLength).map { elements[it % period] }

        blankPositions = when (level) {
            PatternLevel.BEGINNER -> listOf(sequenceLength - 1)
            PatternLevel.INTERMEDIATE -> listOf((1 until sequenceLength - 1).random())
            PatternLevel.EXPERT -> (1 until sequenceLength).shuffled().take(2).sorted()
        }
        blankPointer = 0

        val firstBlankIndex = blankPositions[0]
        correctAnswer = fullSequence[firstBlankIndex]
        val cells = fullSequence.mapIndexed { index, emoji -> if (index in blankPositions) PATTERN_BLANK else emoji }
        val options = buildOptions(correctAnswer)

        _uiState.update {
            it.copy(
                cells = cells,
                activeCellIndex = firstBlankIndex,
                options = options,
                optionStatuses = List(options.size) { PatternOptionStatus.NEUTRAL },
                roundNumber = roundNumber,
                inputEnabled = false,
                isSessionComplete = false,
            )
        }
        val phrase = if (level == PatternLevel.BEGINNER) nextPhrase else missingPhrase
        roundJob = viewModelScope.launch {
            delay(300)
            ttsController.speak(phrase)
            _uiState.update { it.copy(inputEnabled = true) }
        }
    }

    private fun completeSession() {
        _uiState.update { it.copy(isSessionComplete = true, inputEnabled = false) }
        viewModelScope.launch { ttsController.speak(completePhrase) }
    }

    private fun List<PatternOptionStatus>.replacedAt(index: Int, value: PatternOptionStatus): List<PatternOptionStatus> =
        toMutableList().also { it[index] = value }

    private fun List<String>.replacedAt(index: Int, value: String): List<String> =
        toMutableList().also { it[index] = value }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class PatternGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: PatternLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PatternGameViewModel(application, language, level) as T
    }
}
