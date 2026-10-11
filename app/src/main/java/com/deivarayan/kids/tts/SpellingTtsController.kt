package com.deivarayan.kids.tts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Thin coroutine wrapper around [TextToSpeech] that lets callers `speak()` one
 * utterance at a time and suspend until the engine has actually finished it -
 * needed so letter highlighting can stay in lock-step with the audio.
 */
class SpellingTtsController(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pending = ConcurrentHashMap<String, CancellableContinuation<Unit>>()
    private var idCounter = 0
    private var tts: TextToSpeech? = null

    suspend fun initialize(): Boolean = suspendCancellableCoroutine { cont ->
        lateinit var instance: TextToSpeech
        instance = TextToSpeech(appContext) { status ->
            if (cont.isActive) cont.resume(status == TextToSpeech.SUCCESS)
        }
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = Unit
            override fun onDone(utteranceId: String) = resumePending(utteranceId)
            override fun onStop(utteranceId: String, interrupted: Boolean) = resumePending(utteranceId)

            @Deprecated("Deprecated in Java", ReplaceWith(""))
            override fun onError(utteranceId: String) = resumePending(utteranceId)

            override fun onError(utteranceId: String, errorCode: Int) = resumePending(utteranceId)
        })
        tts = instance
    }

    private fun resumePending(utteranceId: String) {
        val cont = pending.remove(utteranceId) ?: return
        mainHandler.post { if (cont.isActive) cont.resume(Unit) }
    }

    fun setLocale(locale: Locale) {
        tts?.language = locale
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    suspend fun speak(text: String) {
        val engine = tts ?: return
        if (text.isBlank()) return
        val utteranceId = "utt-${idCounter++}"
        suspendCancellableCoroutine { cont ->
            pending[utteranceId] = cont
            val result = engine.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
            if (result == TextToSpeech.ERROR) {
                pending.remove(utteranceId)
                if (cont.isActive) cont.resume(Unit)
            }
            cont.invokeOnCancellation { pending.remove(utteranceId) }
        }
    }

    fun stop() {
        tts?.stop()
        pending.keys.toList().forEach { id ->
            pending.remove(id)?.let { cont -> mainHandler.post { if (cont.isActive) cont.resume(Unit) } }
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
