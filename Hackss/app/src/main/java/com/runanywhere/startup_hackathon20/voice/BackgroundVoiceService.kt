package com.runanywhere.startup_hackathon20.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.runanywhere.startup_hackathon20.MainActivity
import com.runanywhere.startup_hackathon20.R

class BackgroundVoiceService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningForWakeWord = false
    private val CHANNEL_ID = "voice_assistant_channel"
    private val NOTIFICATION_ID = 1001

    companion object {
        const val ACTION_START_LISTENING = "com.runanywhere.ACTION_START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.runanywhere.ACTION_STOP_LISTENING"

        fun start(context: Context) {
            val intent = Intent(context, BackgroundVoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BackgroundVoiceService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(wakeWordListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        when (intent?.action) {
            ACTION_START_LISTENING -> startListeningForWakeWord()
            ACTION_STOP_LISTENING -> stopListeningForWakeWord()
            else -> startListeningForWakeWord()
        }

        return START_STICKY
    }

    private fun startListeningForWakeWord() {
        if (isListeningForWakeWord) return

        isListeningForWakeWord = true
        startSpeechRecognition()
        updateNotification("Listening for 'Hey Assistant'...")
    }

    private fun stopListeningForWakeWord() {
        isListeningForWakeWord = false
        speechRecognizer?.cancel()
        updateNotification("Voice Assistant (Tap to activate)")
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Retry after delay
            android.os.Handler(mainLooper).postDelayed({
                if (isListeningForWakeWord) startSpeechRecognition()
            }, 1000)
        }
    }

    private val wakeWordListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            // Ready to listen
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            // Restart listening for wake word
            if (isListeningForWakeWord) {
                android.os.Handler(mainLooper).postDelayed({
                    startSpeechRecognition()
                }, 500)
            }
        }

        override fun onError(error: Int) {
            // Restart listening after error
            if (isListeningForWakeWord) {
                android.os.Handler(mainLooper).postDelayed({
                    startSpeechRecognition()
                }, 1000)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spokenText = matches?.firstOrNull()?.lowercase() ?: ""

            // Check for wake words
            if (spokenText.contains("hey assistant") ||
                spokenText.contains("ok assistant") ||
                spokenText.contains("hello assistant")
            ) {

                // Wake word detected! Open the assistant
                onWakeWordDetected()
            }

            // Continue listening
            if (isListeningForWakeWord) {
                android.os.Handler(mainLooper).postDelayed({
                    startSpeechRecognition()
                }, 500)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull()?.lowercase() ?: ""

            // Check partial results for faster response
            if (partialText.contains("hey assistant") ||
                partialText.contains("ok assistant")
            ) {
                updateNotification("Wake word detected! 🎙️")
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun onWakeWordDetected() {
        // Show notification that wake word was heard
        updateNotification("Voice command ready! Speak now...")

        // Open the app and activate listening
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("auto_start_listening", true)
            putExtra("tab_index", 1) // Go to Assistant tab
        }
        startActivity(intent)

        // Give haptic feedback
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(
                    100,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background voice assistant service"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tab_index", 1)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Assistant Active")
            .setContentText("Say 'Hey Assistant' to activate")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Assistant")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isListeningForWakeWord = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
