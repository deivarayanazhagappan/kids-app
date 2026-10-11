package com.deivarayan.kids.ui.phonics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.GameSpeed
import com.deivarayan.kids.data.PhonicsData
import com.deivarayan.kids.data.PhonicsEntry
import com.deivarayan.kids.network.WikimediaImageRepository
import com.deivarayan.kids.tts.SpellingTtsController
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhonicsUiState(
    val entry: PhonicsEntry? = null,
    val isSoundPlaying: Boolean = false,
    val isSpeakingWord: Boolean = false,
    val unitNumber: Int = 0,
    val totalUnits: Int = 0,
    val imageUrl: String? = null,
)

private const val PAUSE_BETWEEN_UNITS_MS = 3000L
private const val PAUSE_BEFORE_WORD_MS = 600L
private const val SOUND_REPEAT_COUNT = 2

/**
 * Loops through letter/digraph sounds: says the sound twice ("b... b...",
 * mirroring how phonics is taught out loud), then the example word - e.g.
 * "b, b, ball" - then moves to the next unit.
 */
class PhonicsGameViewModel(
    application: Application,
    private val language: GameLanguage,
    private val speed: GameSpeed,
) : AndroidViewModel(application) {

    private val ttsController = SpellingTtsController(application)
    private val imageRepository = WikimediaImageRepository()
    private val entries = PhonicsData.phonicsFor(language)
    private var imageRequestToken = 0

    private val _uiState = MutableStateFlow(PhonicsUiState(totalUnits = entries.size))
    val uiState: StateFlow<PhonicsUiState> = _uiState

    private var currentUnitJob: Job? = null
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

    /** Interrupts whatever is currently playing and jumps straight to the next unit. */
    fun skipToNext() {
        if (entries.isEmpty()) return
        ttsController.stop()
        currentIndex = (currentIndex + 1) % entries.size
        currentUnitJob?.cancel()
    }

    /** Interrupts whatever is currently playing and jumps back to the previous unit. */
    fun skipToPrevious() {
        if (entries.isEmpty()) return
        ttsController.stop()
        currentIndex = (currentIndex - 1 + entries.size) % entries.size
        currentUnitJob?.cancel()
    }

    /** Stops playback (e.g. app backgrounded); [onResume] replays the current unit. */
    fun onPause() {
        isPaused.value = true
        ttsController.stop()
        currentUnitJob?.cancel()
    }

    fun onResume() {
        isPaused.value = false
    }

    private suspend fun runLoop() {
        if (entries.isEmpty()) return
        while (true) {
            isPaused.first { !it }

            val entry = entries[currentIndex]
            _uiState.update {
                it.copy(
                    entry = entry,
                    isSoundPlaying = false,
                    isSpeakingWord = false,
                    unitNumber = currentIndex + 1,
                    imageUrl = null,
                )
            }
            requestImage(entry)

            val job = viewModelScope.launch {
                ttsController.setSpeechRate(speed.letterRate)
                for (repeatIndex in 0 until SOUND_REPEAT_COUNT) {
                    _uiState.update { it.copy(isSoundPlaying = true) }
                    ttsController.speak(entry.soundHint)
                    _uiState.update { it.copy(isSoundPlaying = false) }
                    if (repeatIndex < SOUND_REPEAT_COUNT - 1) {
                        delay(speed.letterPauseMs)
                    }
                }

                delay(PAUSE_BEFORE_WORD_MS)
                _uiState.update { it.copy(isSpeakingWord = true) }
                ttsController.setSpeechRate(speed.wordRate)
                ttsController.speak(entry.exampleWord)
                _uiState.update { it.copy(isSpeakingWord = false) }

                delay(PAUSE_BETWEEN_UNITS_MS)
            }
            currentUnitJob = job
            job.join()

            if (job.isCompleted && !job.isCancelled) {
                currentIndex = (currentIndex + 1) % entries.size
            }
        }
    }

    private fun requestImage(entry: PhonicsEntry) {
        val requestToken = ++imageRequestToken
        viewModelScope.launch {
            val url = imageRepository.fetchImageUrl(entry.exampleImageQuery)
            if (requestToken == imageRequestToken) {
                _uiState.update { it.copy(imageUrl = url) }
            }
        }
    }

    override fun onCleared() {
        ttsController.shutdown()
        super.onCleared()
    }
}

class PhonicsGameViewModelFactory(
    private val application: Application,
    private val language: GameLanguage,
    private val speed: GameSpeed,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PhonicsGameViewModel(application, language, speed) as T
    }
}
