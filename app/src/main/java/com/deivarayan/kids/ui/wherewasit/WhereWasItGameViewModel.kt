package com.deivarayan.kids.ui.wherewasit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.WhereWasItLevel
import com.deivarayan.kids.data.WordData
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WhereWasItPhase { WAITING, FLASHING, GUESSING }

data class WhereWasItUiState(
    val phase: WhereWasItPhase = WhereWasItPhase.WAITING,
    val flashCellIndex: Int = -1,
    val flashEmoji: String = "",
    val guessedIndex: Int? = null,
    val guessWasCorrect: Boolean? = null,
    val roundNumber: Int = 0,
    val totalRounds: Int = 0,
    val correctCount: Int = 0,
    val isSessionComplete: Boolean = false,
    val isShowingIntro: Boolean = false,
) {
    val inputEnabled: Boolean get() = phase == WhereWasItPhase.GUESSING && guessedIndex == null
}

private const val FEEDBACK_DELAY_MS = 1000L

class WhereWasItGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: WhereWasItLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    val nextLevel: WhereWasItLevel? = when (level) {
        WhereWasItLevel.BEGINNER -> WhereWasItLevel.INTERMEDIATE
        WhereWasItLevel.INTERMEDIATE -> WhereWasItLevel.EXPERT
        WhereWasItLevel.EXPERT -> null
    }

    private val introText = if (language == GameLanguage.ENGLISH) {
        "Watch closely, then tap where the picture was!"
    } else {
        "Schau genau hin und tippe dann, wo das Bild war!"
    }
    private val correctPhrase = if (language == GameLanguage.ENGLISH) "Correct!" else "Richtig!"
    private val wrongPhrase = if (language == GameLanguage.ENGLISH) "Wrong! It was here." else "Falsch! Es war hier."
    private fun completePhrase(correctCount: Int) = if (language == GameLanguage.ENGLISH) {
        "You got $correctCount out of ${level.roundCount} right!"
    } else {
        "Du hast $correctCount von ${level.roundCount} richtig!"
    }

    private val _uiState = MutableStateFlow(WhereWasItUiState())
    val uiState: StateFlow<WhereWasItUiState> = _uiState

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
                showIntroThenStart()
            }
        }
    }

    fun onCellTapped(index: Int) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        roundJob?.cancel()

        val isCorrect = index == state.flashCellIndex
        _uiState.update { it.copy(guessedIndex = index, guessWasCorrect = isCorrect, correctCount = it.correctCount + if (isCorrect) 1 else 0) }
        roundJob = viewModelScope.launch {
            ttsController.speak(if (isCorrect) correctPhrase else wrongPhrase)
            delay(FEEDBACK_DELAY_MS)
            if (state.roundNumber >= level.roundCount) {
                completeSession()
            } else {
                startRound(state.roundNumber + 1)
            }
        }
    }

    fun playAgain() {
        roundJob?.cancel()
        _uiState.value = WhereWasItUiState()
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

    private fun startRound(roundNumber: Int) {
        roundJob?.cancel()
        val cellCount = level.gridColumns * level.gridColumns
        val targetCell = Random.nextInt(cellCount)
        val emoji = WordData.randomDistinctPictures(language, 1).first().emoji

        _uiState.update {
            it.copy(
                phase = WhereWasItPhase.WAITING,
                flashCellIndex = targetCell,
                flashEmoji = emoji,
                guessedIndex = null,
                guessWasCorrect = null,
                roundNumber = roundNumber,
                totalRounds = level.roundCount,
                isSessionComplete = false,
            )
        }
        val delayMs = Random.nextLong(level.minDelayMs, level.maxDelayMs + 1)
        roundJob = viewModelScope.launch {
            delay(delayMs)
            _uiState.update { it.copy(phase = WhereWasItPhase.FLASHING) }
            delay(level.showDurationMs)
            _uiState.update { it.copy(phase = WhereWasItPhase.GUESSING) }
        }
    }

    private fun completeSession() {
        val state = _uiState.value
        _uiState.update { it.copy(isSessionComplete = true) }
        viewModelScope.launch { ttsController.speak(completePhrase(state.correctCount)) }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class WhereWasItGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: WhereWasItLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WhereWasItGameViewModel(application, language, level) as T
    }
}
