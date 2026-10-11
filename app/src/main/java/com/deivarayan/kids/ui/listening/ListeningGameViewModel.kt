package com.deivarayan.kids.ui.listening

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.ListeningLevel
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TileStatus { NEUTRAL, CORRECT, WRONG }

data class ListeningUiState(
    val options: List<Int> = emptyList(),
    val tileStatuses: List<TileStatus> = emptyList(),
    val target: Int = 0,
    val score: Int = 0,
    val roundsCompleted: Int = 0,
    val inputEnabled: Boolean = false,
    val showThumbsUp: Boolean = false,
    val isSessionComplete: Boolean = false,
    val isFinalLevel: Boolean = false,
    val isShowingIntro: Boolean = false,
)

private const val ROUND_LIMIT = 30
private const val PRE_SPEAK_DELAY_MS = 400L
private const val CORRECT_FEEDBACK_DELAY_MS = 1200L
private const val THUMBS_UP_DURATION_MS = 1200L

class ListeningGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: ListeningLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    private val _uiState = MutableStateFlow(ListeningUiState())
    val uiState: StateFlow<ListeningUiState> = _uiState

    val nextLevel: ListeningLevel? = when (level) {
        ListeningLevel.BEGINNER -> ListeningLevel.INTERMEDIATE
        ListeningLevel.INTERMEDIATE -> ListeningLevel.EXPERT
        ListeningLevel.EXPERT -> null
    }

    private val correctPhrase = if (language == GameLanguage.ENGLISH) "Correct!" else "Richtig!"
    private val notCorrectPhrase = if (language == GameLanguage.ENGLISH) "Not correct." else "Nicht richtig."
    private val levelCompletePhrase = if (language == GameLanguage.ENGLISH) {
        "Great job! Try the next level!"
    } else {
        "Toll gemacht! Versuch das nächste Level!"
    }
    private val allLevelsCompletePhrase = if (language == GameLanguage.ENGLISH) {
        "You have completed all levels!"
    } else {
        "Du hast alle Level geschafft!"
    }
    val introText = if (language == GameLanguage.ENGLISH) {
        "Listen carefully! I will say a number. Tap the tile that shows the number you heard. " +
            "If you're right, the tile turns green. If you're wrong, try again!"
    } else {
        "Hör gut zu! Ich sage eine Zahl. Tippe auf die Kachel mit der Zahl, die du gehört hast. " +
            "Wenn du richtig liegst, wird die Kachel grün. Wenn nicht, versuch es nochmal!"
    }

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
                if (level == ListeningLevel.BEGINNER) {
                    showIntroThenStart()
                } else {
                    startNewRound()
                }
            }
        }
    }

    fun onTileSelected(index: Int) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        val selected = state.options[index]

        if (selected == state.target) {
            val roundsCompleted = state.roundsCompleted + 1
            _uiState.update {
                it.copy(
                    tileStatuses = it.tileStatuses.replacedAt(index, TileStatus.CORRECT),
                    inputEnabled = false,
                    score = it.score + 1,
                    roundsCompleted = roundsCompleted,
                    showThumbsUp = level.showThumbsUpOnCorrect,
                )
            }
            roundJob = viewModelScope.launch {
                ttsController.speak(correctPhrase)
                if (level.showThumbsUpOnCorrect) {
                    delay(THUMBS_UP_DURATION_MS)
                    _uiState.update { it.copy(showThumbsUp = false) }
                }
                delay(CORRECT_FEEDBACK_DELAY_MS)
                if (roundsCompleted >= ROUND_LIMIT) {
                    completeSession()
                } else {
                    startNewRound()
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    tileStatuses = it.tileStatuses.replacedAt(index, TileStatus.WRONG),
                    inputEnabled = false,
                )
            }
            roundJob = viewModelScope.launch {
                ttsController.speak(notCorrectPhrase)
                ttsController.speak(state.target.toString())
                _uiState.update {
                    it.copy(
                        tileStatuses = List(it.options.size) { TileStatus.NEUTRAL },
                        inputEnabled = true,
                    )
                }
            }
        }
    }

    fun playAgain() {
        _uiState.value = ListeningUiState()
        startNewRound()
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
        // If backgrounding interrupted mid-feedback (tiles disabled), start fresh
        // rather than risk getting stuck; if we were just waiting for a tap,
        // leave the round as-is.
        if (!state.inputEnabled) {
            startNewRound()
        }
    }

    private fun showIntroThenStart() {
        roundJob?.cancel()
        _uiState.update { it.copy(isShowingIntro = true) }
        roundJob = viewModelScope.launch {
            ttsController.speak(introText)
            _uiState.update { it.copy(isShowingIntro = false) }
            startNewRound()
        }
    }

    private fun completeSession() {
        val isFinalLevel = level == ListeningLevel.EXPERT
        _uiState.update {
            it.copy(isSessionComplete = true, inputEnabled = false, isFinalLevel = isFinalLevel)
        }
        roundJob = viewModelScope.launch {
            ttsController.speak(if (isFinalLevel) allLevelsCompletePhrase else levelCompletePhrase)
        }
    }

    private fun startNewRound() {
        roundJob?.cancel()
        val options = (1..level.maxNumber).shuffled().take(level.tileCount)
        val target = options.random()
        _uiState.value = ListeningUiState(
            options = options,
            tileStatuses = List(options.size) { TileStatus.NEUTRAL },
            target = target,
            score = _uiState.value.score,
            roundsCompleted = _uiState.value.roundsCompleted,
            inputEnabled = false,
            showThumbsUp = false,
        )
        roundJob = viewModelScope.launch {
            delay(PRE_SPEAK_DELAY_MS)
            ttsController.speak(target.toString())
            _uiState.update { it.copy(inputEnabled = true) }
        }
    }

    private fun List<TileStatus>.replacedAt(index: Int, value: TileStatus): List<TileStatus> =
        toMutableList().also { it[index] = value }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class ListeningGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: ListeningLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ListeningGameViewModel(application, language, level) as T
    }
}
