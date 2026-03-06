package com.example.mindbox.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    data class Partial(val text: String) : VoiceState()
    data class Final(val text: String) : VoiceState()
    data class Error(val code: Int, val message: String) : VoiceState()
    object PermissionDenied : VoiceState()
    object Unavailable : VoiceState()
}

@Singleton
class VoiceRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ERROR_MESSAGES = mapOf(
            SpeechRecognizer.ERROR_AUDIO to "Audio recording error",
            SpeechRecognizer.ERROR_CLIENT to "Client-side error",
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS to "Microphone permission denied",
            SpeechRecognizer.ERROR_NETWORK to "Network error",
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT to "Network timeout",
            SpeechRecognizer.ERROR_NO_MATCH to "No speech recognized",
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY to "Speech recognizer busy",
            SpeechRecognizer.ERROR_SERVER to "Server error",
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT to "No speech detected"
        )
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Returns a [Flow] of [VoiceState] events.
     * Collect this flow on the Main dispatcher.
     * The recognizer is automatically released when collection is cancelled.
     */
    fun listen(locale: Locale = Locale.getDefault()): Flow<VoiceState> = callbackFlow {
        if (!isAvailable()) {
            trySend(VoiceState.Unavailable)
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(VoiceState.Listening)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    trySend(VoiceState.PermissionDenied)
                } else {
                    trySend(VoiceState.Error(error, ERROR_MESSAGES[error] ?: "Unknown error"))
                }
                trySend(VoiceState.Idle)
                close()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                trySend(VoiceState.Final(text))
                trySend(VoiceState.Idle)
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return
                trySend(VoiceState.Partial(partial))
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer.startListening(intent)

        awaitClose {
            recognizer.stopListening()
            recognizer.destroy()
        }
    }
}
