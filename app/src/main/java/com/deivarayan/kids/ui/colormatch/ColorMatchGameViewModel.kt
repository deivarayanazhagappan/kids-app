package com.deivarayan.kids.ui.colormatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.ColorMatchData
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.NamedColor
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ColorOptionStatus { NEUTRAL, CORRECT, WRONG }

data class ColorMatchUiState(
    val target: NamedColor? = null,
    val options: List<NamedColor> = emptyList(),
    val optionStatuses: List<ColorOptionStatus> = emptyList(),
    val roundNumber: Int = 0,
    val totalRounds: Int = 0,
    val correctCount: Int = 0,
    val inputEnabled: Boolean = false,
    val isSessionComplete: Boolean = false,
)

private const val ROUND_LIMIT = 15
private const val OPTION_COUNT = 4
private const val FEEDBACK_DELAY_MS = 900L

class ColorMatchGameViewModel(
    application: Application,
    private val language: GameLanguage,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    private val correctPhrase = if (language == GameLanguage.ENGLISH) "Correct!" else "Richtig!"
    private val wrongPhrase = if (language == GameLanguage.ENGLISH) "Wrong!" else "Falsch!"
    private fun completePhrase(correctCount: Int) = if (language == GameLanguage.ENGLISH) {
        "You got $correctCount out of $ROUND_LIMIT right!"
    } else {
        "Du hast $correctCount von $ROUND_LIMIT richtig!"
    }
    private fun findPhrase(target: NamedColor) = if (language == GameLanguage.ENGLISH) {
        "Find ${target.english}!"
    } else {
        "Finde ${target.german}!"
    }

    private val _uiState = MutableStateFlow(ColorMatchUiState())
    val uiState: StateFlow<ColorMatchUiState> = _uiState

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
        if (state.optionStatuses.getOrNull(index) != ColorOptionStatus.NEUTRAL) return
        val chosen = state.options.getOrNull(index) ?: return
        val target = state.target ?: return
        val isCorrect = chosen == target

        val newStatuses = state.optionStatuses.toMutableList()
        newStatuses[index] = if (isCorrect) ColorOptionStatus.CORRECT else ColorOptionStatus.WRONG
        if (!isCorrect) {
            val correctIndex = state.options.indexOf(target)
            if (correctIndex >= 0) newStatuses[correctIndex] = ColorOptionStatus.CORRECT
        }

        _uiState.update {
            it.copy(
                optionStatuses = newStatuses,
                inputEnabled = false,
                correctCount = it.correctCount + if (isCorrect) 1 else 0,
            )
        }
        roundJob = viewModelScope.launch {
            ttsController.speak(if (isCorrect) correctPhrase else wrongPhrase)
            delay(FEEDBACK_DELAY_MS)
            if (state.roundNumber >= ROUND_LIMIT) {
                completeSession()
            } else {
                startRound(state.roundNumber + 1)
            }
        }
    }

    fun playAgain() {
        roundJob?.cancel()
        _uiState.value = ColorMatchUiState()
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
        val target = ColorMatchData.colors.random()
        val distractors = ColorMatchData.colors.filter { it != target }.shuffled().take(OPTION_COUNT - 1)
        val options = (distractors + target).shuffled()

        _uiState.update {
            it.copy(
                target = target,
                options = options,
                optionStatuses = List(options.size) { ColorOptionStatus.NEUTRAL },
                roundNumber = roundNumber,
                totalRounds = ROUND_LIMIT,
                inputEnabled = false,
                isSessionComplete = false,
            )
        }
        roundJob = viewModelScope.launch {
            delay(300)
            ttsController.speak(findPhrase(target))
            _uiState.update { it.copy(inputEnabled = true) }
        }
    }

    private fun completeSession() {
        val state = _uiState.value
        _uiState.update { it.copy(isSessionComplete = true, inputEnabled = false) }
        viewModelScope.launch { ttsController.speak(completePhrase(state.correctCount)) }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class ColorMatchGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ColorMatchGameViewModel(application, language) as T
    }
}
