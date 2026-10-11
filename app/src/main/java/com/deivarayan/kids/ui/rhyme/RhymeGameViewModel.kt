package com.deivarayan.kids.ui.rhyme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.RhymeData
import com.deivarayan.kids.data.RhymeLevel
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RhymeUiState(
    val words: List<String> = emptyList(),
    val roundNumber: Int = 0,
    val inputEnabled: Boolean = false,
    val isSessionComplete: Boolean = false,
)

const val RHYME_ROUND_LIMIT = 50
private const val WORD_GAP_MS = 500L

class RhymeGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: RhymeLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)
    private val pool = RhymeData.groupsFor(language, level)
    private var order: List<List<String>> = pool.shuffled()

    val nextLevel: RhymeLevel? = when (level) {
        RhymeLevel.BEGINNER -> RhymeLevel.INTERMEDIATE
        RhymeLevel.INTERMEDIATE -> RhymeLevel.EXPERT
        RhymeLevel.EXPERT -> null
    }

    private val completePhrase = if (language == GameLanguage.ENGLISH) "Great job! You're a rhyming star!" else "Toll gemacht! Du bist ein Reim-Profi!"

    private val _uiState = MutableStateFlow(RhymeUiState())
    val uiState: StateFlow<RhymeUiState> = _uiState

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

    fun onNextClicked() {
        val state = _uiState.value
        if (!state.inputEnabled) return
        if (state.roundNumber >= RHYME_ROUND_LIMIT) {
            completeSession()
        } else {
            startRound(state.roundNumber + 1)
        }
    }

    fun onReplayClicked() {
        val state = _uiState.value
        if (!state.inputEnabled || state.words.isEmpty()) return
        roundJob?.cancel()
        roundJob = viewModelScope.launch {
            _uiState.update { it.copy(inputEnabled = false) }
            speakWords(state.words)
            _uiState.update { it.copy(inputEnabled = true) }
        }
    }

    fun playAgain() {
        roundJob?.cancel()
        order = pool.shuffled()
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
        val words = order[roundNumber - 1]
        _uiState.update {
            it.copy(
                words = words,
                roundNumber = roundNumber,
                inputEnabled = false,
                isSessionComplete = false,
            )
        }
        roundJob = viewModelScope.launch {
            delay(300)
            speakWords(words)
            _uiState.update { it.copy(inputEnabled = true) }
        }
    }

    private suspend fun speakWords(words: List<String>) {
        words.forEachIndexed { index, word ->
            ttsController.speak(word)
            if (index != words.lastIndex) delay(WORD_GAP_MS)
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

class RhymeGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: RhymeLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RhymeGameViewModel(application, language, level) as T
    }
}
