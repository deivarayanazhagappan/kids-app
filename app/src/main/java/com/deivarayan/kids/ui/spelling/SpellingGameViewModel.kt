package com.deivarayan.kids.ui.spelling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.GameSpeed
import com.deivarayan.kids.data.WordData
import com.deivarayan.kids.data.WordEntry
import com.deivarayan.kids.network.WikimediaImageRepository
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpellingUiState(
    val entry: WordEntry? = null,
    val highlightedLetterIndex: Int = -1,
    val isSpeakingWholeWord: Boolean = false,
    val wordNumber: Int = 0,
    val totalWords: Int = 0,
    val imageUrl: String? = null,
)

private const val PAUSE_BETWEEN_WORDS_MS = 3000L
private const val PAUSE_BETWEEN_REPEATS_MS = 2000L
private const val SPELLING_REPEAT_COUNT = 2

class SpellingGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val speed: GameSpeed,
    private val category: String?,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)
    private val imageRepository = WikimediaImageRepository()

    // Category picks where to start, not what to loop forever: after the last
    // word in that category the loop continues into the next category, wrapping
    // all the way back around - so we rotate the full list to start there.
    private val words = WordData.wordsFor(language).let { all ->
        if (category == null) {
            all
        } else {
            val startIndex = all.indexOfFirst { it.category == category }
            if (startIndex <= 0) all else all.drop(startIndex) + all.take(startIndex)
        }
    }
    private var imageRequestToken = 0

    private val _uiState = MutableStateFlow(SpellingUiState(totalWords = words.size))
    val uiState: StateFlow<SpellingUiState> = _uiState

    private val _categoryCompletedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val categoryCompletedEvent: SharedFlow<Unit> = _categoryCompletedEvent

    private var currentWordJob: Job? = null
    private val isPaused = MutableStateFlow(false)
    private var currentIndex = 0

    init {
        viewModelScope.launch {
            val ready = ttsController.initialize()
            if (ready) {
                val locale = when (language) {
                    GameLanguage.ENGLISH -> Locale.US
                    GameLanguage.GERMAN -> Locale.GERMANY
                }
                ttsController.setLocale(locale)
                runLoop()
            }
        }
    }

    /** Interrupts whatever is currently playing and jumps straight to the next word. */
    fun skipToNext() {
        if (words.isEmpty()) return
        ttsController.stop()
        currentIndex = (currentIndex + 1) % words.size
        currentWordJob?.cancel()
    }

    /** Interrupts whatever is currently playing and jumps back to the previous word. */
    fun skipToPrevious() {
        if (words.isEmpty()) return
        ttsController.stop()
        currentIndex = (currentIndex - 1 + words.size) % words.size
        currentWordJob?.cancel()
    }

    /** Stops playback (e.g. app backgrounded); [onResume] replays the current word. */
    fun onPause() {
        isPaused.value = true
        ttsController.stop()
        currentWordJob?.cancel()
    }

    fun onResume() {
        isPaused.value = false
    }

    private suspend fun runLoop() {
        if (words.isEmpty()) return
        while (true) {
            isPaused.first { !it }

            val entry = words[currentIndex]
            _uiState.update {
                it.copy(
                    entry = entry,
                    highlightedLetterIndex = -1,
                    isSpeakingWholeWord = false,
                    wordNumber = currentIndex + 1,
                    imageUrl = null,
                )
            }
            requestImage(entry)

            val job = viewModelScope.launch {
                for (repeatIndex in 0 until SPELLING_REPEAT_COUNT) {
                    spellOnce(entry)
                    if (repeatIndex < SPELLING_REPEAT_COUNT - 1) {
                        delay(PAUSE_BETWEEN_REPEATS_MS)
                    }
                }
                delay(PAUSE_BETWEEN_WORDS_MS)
            }
            currentWordJob = job
            job.join()

            // Advance only on natural completion. A skip already moved currentIndex
            // itself; a pause-triggered cancellation should replay the same word.
            if (job.isCompleted && !job.isCancelled) {
                val nextIndex = (currentIndex + 1) % words.size
                if (words[nextIndex].category != entry.category) {
                    _categoryCompletedEvent.tryEmit(Unit)
                }
                currentIndex = nextIndex
            }
        }
    }

    private fun requestImage(entry: WordEntry) {
        val requestToken = ++imageRequestToken
        viewModelScope.launch {
            val url = imageRepository.fetchImageUrl(entry.imageQuery)
            if (requestToken == imageRequestToken) {
                _uiState.update { it.copy(imageUrl = url) }
            }
        }
    }

    private suspend fun spellOnce(entry: WordEntry) {
        ttsController.setSpeechRate(speed.letterRate)
        val letterIndices = entry.text.indices.filter { entry.text[it].isLetter() }
        for ((position, charIndex) in letterIndices.withIndex()) {
            _uiState.update { it.copy(highlightedLetterIndex = charIndex) }
            ttsController.speak(entry.text[charIndex].toString())
            if (position < letterIndices.lastIndex) {
                delay(speed.letterPauseMs)
            }
        }

        _uiState.update { it.copy(highlightedLetterIndex = -1, isSpeakingWholeWord = true) }
        ttsController.setSpeechRate(speed.wordRate)
        ttsController.speak(entry.text)
        _uiState.update { it.copy(isSpeakingWholeWord = false) }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class SpellingGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val speed: GameSpeed,
    private val category: String?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SpellingGameViewModel(application, language, speed, category) as T
    }
}
