package com.deivarayan.kids.ui.simon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SimonPhase { SHOWING, WAITING_INPUT, GAME_OVER }

data class SimonUiState(
    val sequence: List<Int> = emptyList(),
    val activeTileIndex: Int = -1,
    val phase: SimonPhase = SimonPhase.SHOWING,
    val playerProgress: Int = 0,
    val reachedLength: Int = 0,
)

private const val TILE_COUNT = 4
private const val PRE_SHOW_DELAY_MS = 500L
private const val SHOW_ON_MS = 500L
private const val SHOW_GAP_MS = 250L
private const val ROUND_COMPLETE_DELAY_MS = 600L

class SimonSaysViewModel(
    application: Application,
    private val language: GameLanguage,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)
    private val tileNames = if (language == GameLanguage.ENGLISH) {
        listOf("Red", "Blue", "Green", "Yellow")
    } else {
        listOf("Rot", "Blau", "Grün", "Gelb")
    }
    private val correctPhrase = if (language == GameLanguage.ENGLISH) "Correct!" else "Richtig!"
    private val gameOverPrefix = if (language == GameLanguage.ENGLISH) "Game over! You reached length" else "Spiel vorbei! Du hast Länge"

    private val _uiState = MutableStateFlow(SimonUiState())
    val uiState: StateFlow<SimonUiState> = _uiState

    private var showJob: Job? = null

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
                startNewGame()
            }
        }
    }

    fun onTileClicked(index: Int) {
        val state = _uiState.value
        if (state.phase != SimonPhase.WAITING_INPUT) return

        val expected = state.sequence[state.playerProgress]
        if (index != expected) {
            gameOver(state.sequence.size - 1)
            return
        }

        val newProgress = state.playerProgress + 1
        if (newProgress == state.sequence.size) {
            _uiState.update { it.copy(phase = SimonPhase.SHOWING, reachedLength = state.sequence.size) }
            showJob = viewModelScope.launch {
                ttsController.speak(correctPhrase)
                delay(ROUND_COMPLETE_DELAY_MS)
                val newSequence = state.sequence + Random.nextInt(TILE_COUNT)
                _uiState.update { it.copy(sequence = newSequence, playerProgress = 0) }
                playSequence()
            }
        } else {
            _uiState.update { it.copy(playerProgress = newProgress) }
        }
    }

    fun playAgain() {
        showJob?.cancel()
        startNewGame()
    }

    fun onPause() {
        ttsController.stop()
        showJob?.cancel()
    }

    fun onResume() {
        val state = _uiState.value
        if (state.phase == SimonPhase.GAME_OVER) return
        _uiState.update { it.copy(phase = SimonPhase.SHOWING, playerProgress = 0) }
        playSequence()
    }

    private fun startNewGame() {
        showJob?.cancel()
        _uiState.value = SimonUiState(sequence = listOf(Random.nextInt(TILE_COUNT)))
        playSequence()
    }

    private fun playSequence() {
        showJob?.cancel()
        showJob = viewModelScope.launch {
            delay(PRE_SHOW_DELAY_MS)
            for (tileIndex in _uiState.value.sequence) {
                _uiState.update { it.copy(activeTileIndex = tileIndex) }
                ttsController.speak(tileNames[tileIndex])
                delay(SHOW_ON_MS)
                _uiState.update { it.copy(activeTileIndex = -1) }
                delay(SHOW_GAP_MS)
            }
            _uiState.update { it.copy(phase = SimonPhase.WAITING_INPUT, playerProgress = 0) }
        }
    }

    private fun gameOver(reachedLength: Int) {
        showJob?.cancel()
        _uiState.update { it.copy(phase = SimonPhase.GAME_OVER, reachedLength = reachedLength) }
        viewModelScope.launch { ttsController.speak("$gameOverPrefix $reachedLength") }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class SimonSaysViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SimonSaysViewModel(application, language) as T
    }
}
