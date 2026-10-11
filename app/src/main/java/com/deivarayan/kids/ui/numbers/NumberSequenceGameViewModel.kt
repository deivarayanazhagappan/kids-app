package com.deivarayan.kids.ui.numbers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.NumberSequenceLevel
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NumberTileStatus { AVAILABLE, PLACED }

data class NumberTile(val id: Int, val value: Int, val status: NumberTileStatus = NumberTileStatus.AVAILABLE)

data class NumberSequenceUiState(
    val cells: List<Int?> = emptyList(),
    val options: List<NumberTile> = emptyList(),
    val roundNumber: Int = 0,
    val inputEnabled: Boolean = false,
    val isSessionComplete: Boolean = false,
    val wrongAttemptTick: Int = 0,
)

const val NUMBER_SEQUENCE_ROUND_LIMIT = 20
private const val CORRECT_DELAY_MS = 900L
private const val DISTRACTOR_COUNT = 2

class NumberSequenceGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: NumberSequenceLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    val nextLevel: NumberSequenceLevel? = when (level) {
        NumberSequenceLevel.BEGINNER -> NumberSequenceLevel.INTERMEDIATE
        NumberSequenceLevel.INTERMEDIATE -> NumberSequenceLevel.EXPERT
        NumberSequenceLevel.EXPERT -> null
    }

    private val missingPhrase = if (language == GameLanguage.ENGLISH) "Drag the missing numbers into the gaps." else "Zieh die fehlenden Zahlen in die Lücken."
    private val correctPhrase = if (language == GameLanguage.ENGLISH) "Correct!" else "Richtig!"
    private val wrongPhrase = if (language == GameLanguage.ENGLISH) "Wrong. Try again." else "Falsch. Versuch's nochmal."
    private val completePhrase = if (language == GameLanguage.ENGLISH) "Great job! You solved them all!" else "Toll gemacht! Du hast alle gelöst!"

    private val _uiState = MutableStateFlow(NumberSequenceUiState())
    val uiState: StateFlow<NumberSequenceUiState> = _uiState

    private var fullSequence: List<Int> = emptyList()
    private var blankValues: Map<Int, Int> = emptyMap()
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

    fun checkAnswer(blankIndex: Int, tileId: Int): Boolean {
        val state = _uiState.value
        if (!state.inputEnabled) return false
        if (state.cells.getOrNull(blankIndex) != null) return false
        val tile = state.options.find { it.id == tileId } ?: return false
        if (tile.status != NumberTileStatus.AVAILABLE) return false
        return tile.value == blankValues[blankIndex]
    }

    fun placeTile(blankIndex: Int, tileId: Int) {
        val state = _uiState.value
        val newCells = state.cells.toMutableList().also { it[blankIndex] = blankValues[blankIndex] }
        val newOptions = state.options.map { if (it.id == tileId) it.copy(status = NumberTileStatus.PLACED) else it }
        _uiState.update { it.copy(cells = newCells, options = newOptions) }

        if (newCells.none { it == null }) {
            _uiState.update { it.copy(inputEnabled = false) }
            roundJob = viewModelScope.launch {
                ttsController.speak(correctPhrase)
                delay(CORRECT_DELAY_MS)
                if (state.roundNumber >= NUMBER_SEQUENCE_ROUND_LIMIT) {
                    completeSession()
                } else {
                    startRound(state.roundNumber + 1)
                }
            }
        } else {
            viewModelScope.launch { ttsController.speak(correctPhrase) }
        }
    }

    fun onWrongAttempt() {
        viewModelScope.launch { ttsController.speak(wrongPhrase) }
        _uiState.update { it.copy(wrongAttemptTick = it.wrongAttemptTick + 1) }
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

    private fun startRound(roundNumber: Int) {
        roundJob?.cancel()
        val count = level.numberCount
        val start = Random.nextInt(1, 50 - count)
        fullSequence = (0 until count).map { start + it }

        val blankPositions = when (level) {
            NumberSequenceLevel.BEGINNER -> listOf(count / 2)
            else -> (0 until count).shuffled().take(level.blankCount).sorted()
        }
        blankValues = blankPositions.associateWith { fullSequence[it] }

        val cells: List<Int?> = fullSequence.mapIndexed { index, value -> if (index in blankPositions) null else value }

        val distractorPool = (maxOf(1, start - 10)..(start + count + 9)).filter { it !in fullSequence }
        val distractors = distractorPool.shuffled().take(DISTRACTOR_COUNT)
        val options = (blankPositions.map { fullSequence[it] } + distractors)
            .shuffled()
            .mapIndexed { index, value -> NumberTile(id = index, value = value) }

        _uiState.update {
            it.copy(
                cells = cells,
                options = options,
                roundNumber = roundNumber,
                inputEnabled = false,
                isSessionComplete = false,
            )
        }
        roundJob = viewModelScope.launch {
            delay(300)
            ttsController.speak(missingPhrase)
            _uiState.update { it.copy(inputEnabled = true) }
        }
    }

    private fun completeSession() {
        _uiState.update { it.copy(isSessionComplete = true, inputEnabled = false) }
        viewModelScope.launch { ttsController.speak(completePhrase) }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class NumberSequenceGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: NumberSequenceLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NumberSequenceGameViewModel(application, language, level) as T
    }
}
