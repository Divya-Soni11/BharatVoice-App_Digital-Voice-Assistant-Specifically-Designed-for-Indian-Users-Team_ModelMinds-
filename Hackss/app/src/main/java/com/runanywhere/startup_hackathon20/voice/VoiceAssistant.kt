package com.runanywhere.startup_hackathon20.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * Voice Assistant for speech recognition and text-to-speech
 */
class VoiceAssistant(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isListening = false
    private var isTtsReady = false
    private var commandCallback: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "VoiceAssistant"
    }

    /**
     * Initialize speech recognition and TTS
     */
    fun initialize(onReady: () -> Unit = {}) {
        // Initialize Speech Recognition
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(recognitionListener)
            Log.d(TAG, "Speech Recognition initialized")
        } else {
            Log.e(TAG, "Speech Recognition not available on this device")
        }

        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                isTtsReady = true
                Log.d(TAG, "Text-to-Speech initialized")
                onReady()
            } else {
                Log.e(TAG, "Text-to-Speech initialization failed")
            }
        }
    }

    /**
     * Start listening for voice commands
     */
    fun startListening(onCommand: (String) -> Unit) {
        if (isListening) {
            Log.d(TAG, "Already listening")
            return
        }

        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not initialized")
            return
        }

        commandCallback = onCommand

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.startListening(intent)
        isListening = true
        Log.d(TAG, "Started listening for voice commands")
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        Log.d(TAG, "Stopped listening")
    }

    /**
     * Speak text using TTS
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady) {
            Log.e(TAG, "TTS not ready")
            onComplete?.invoke()
            return
        }

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        Log.d(TAG, "Speaking: $text")

        // Simple completion callback (in real implementation, use UtteranceProgressListener)
        onComplete?.invoke()
    }

    /**
     * Speech recognition listener
     */
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Voice volume changed (can be used for UI feedback)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech")
            isListening = false
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Unknown error"
            }
            Log.e(TAG, "Recognition error: $errorMessage")
            isListening = false
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { command ->
                Log.d(TAG, "Recognized command: $command")
                commandCallback?.invoke(command)
            }
            isListening = false
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )
            matches?.firstOrNull()?.let { partial ->
                Log.d(TAG, "Partial result: $partial")
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Custom event
        }
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
        speechRecognizer = null
        textToSpeech = null
        commandCallback = null
        Log.d(TAG, "Voice Assistant destroyed")
    }

    /**
     * Check if currently listening
     */
    fun isListening(): Boolean = isListening

    /**
     * Check if TTS is ready
     */
    fun isTtsReady(): Boolean = isTtsReady
}
