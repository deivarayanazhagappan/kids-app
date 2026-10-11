package com.deivarayan.kids.ui.memory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.MemoryLevel
import com.deivarayan.kids.data.WordData
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TileFace { DOWN, UP, MATCHED }

data class MemoryTile(val id: Int, val emoji: String, val word: String)

data class MemoryUiState(
    val tiles: List<MemoryTile> = emptyList(),
    val faces: List<TileFace> = emptyList(),
    val matchedPairs: Int = 0,
    val totalPairs: Int = 0,
    val moves: Int = 0,
    val inputEnabled: Boolean = true,
    val isSessionComplete: Boolean = false,
)

private const val MISMATCH_DELAY_MS = 900L

class MemoryGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val level: MemoryLevel,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<MemoryUiState> = _uiState

    val nextLevel: MemoryLevel? = when (level) {
        MemoryLevel.BEGINNER -> MemoryLevel.INTERMEDIATE
        MemoryLevel.INTERMEDIATE -> MemoryLevel.EXPERT
        MemoryLevel.EXPERT -> null
    }

    private val matchedPhrase = if (language == GameLanguage.ENGLISH) "Match!" else "Passt!"
    private val completePhrase = if (language == GameLanguage.ENGLISH) {
        "You found all the pairs!"
    } else {
        "Du hast alle Paare gefunden!"
    }

    private var firstFlippedIndex: Int? = null
    private var pendingMismatch: Pair<Int, Int>? = null
    private var mismatchJob: Job? = null

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
            }
        }
    }

    fun onTileClicked(index: Int) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        if (state.faces[index] != TileFace.DOWN) return

        val first = firstFlippedIndex
        if (first == null) {
            firstFlippedIndex = index
            _uiState.update { it.copy(faces = it.faces.replacedAt(index, TileFace.UP)) }
            return
        }

        firstFlippedIndex = null
        _uiState.update { it.copy(faces = it.faces.replacedAt(index, TileFace.UP), moves = it.moves + 1) }

        val tiles = state.tiles
        if (tiles[first].emoji == tiles[index].emoji) {
            val matchedPairs = state.matchedPairs + 1
            _uiState.update {
                it.copy(
                    faces = it.faces.replacedAt(first, TileFace.MATCHED).replacedAt(index, TileFace.MATCHED),
                    matchedPairs = matchedPairs,
                )
            }
            viewModelScope.launch {
                ttsController.speak(tiles[index].word)
                ttsController.speak(matchedPhrase)
            }
            if (matchedPairs >= state.totalPairs) {
                completeSession()
            }
        } else {
            pendingMismatch = first to index
            _uiState.update { it.copy(inputEnabled = false) }
            mismatchJob = viewModelScope.launch {
                delay(MISMATCH_DELAY_MS)
                resolveMismatch()
            }
        }
    }

    fun playAgain() {
        firstFlippedIndex = null
        pendingMismatch = null
        mismatchJob?.cancel()
        _uiState.value = buildInitialState()
    }

    fun onPause() {
        ttsController.stop()
        mismatchJob?.cancel()
        if (pendingMismatch != null) resolveMismatch()
    }

    fun onResume() {
        // Nothing to auto-resume - the board just sits there waiting for a tap.
    }

    private fun resolveMismatch() {
        val pair = pendingMismatch ?: return
        pendingMismatch = null
        _uiState.update {
            it.copy(
                faces = it.faces.replacedAt(pair.first, TileFace.DOWN).replacedAt(pair.second, TileFace.DOWN),
                inputEnabled = true,
            )
        }
    }

    private fun completeSession() {
        _uiState.update { it.copy(isSessionComplete = true, inputEnabled = false) }
        viewModelScope.launch { ttsController.speak(completePhrase) }
    }

    private fun buildInitialState(): MemoryUiState {
        val pairs = WordData.randomDistinctPictures(language, level.pairCount)
        val tiles = (pairs + pairs)
            .mapIndexed { index, entry -> MemoryTile(index, entry.emoji, entry.text) }
            .shuffled()
        return MemoryUiState(
            tiles = tiles,
            faces = List(tiles.size) { TileFace.DOWN },
            totalPairs = level.pairCount,
        )
    }

    private fun List<TileFace>.replacedAt(index: Int, value: TileFace): List<TileFace> =
        toMutableList().also { it[index] = value }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class MemoryGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val level: MemoryLevel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MemoryGameViewModel(application, language, level) as T
    }
}
