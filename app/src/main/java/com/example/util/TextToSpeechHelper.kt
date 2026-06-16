package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper
import java.util.Locale
import com.example.data.AppPreferences

class TextToSpeechHelper(private val context: Context, private val onInitCompleted: (Boolean) -> Unit) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())
    
    var onSpeechFinishedListener: (() -> Unit)? = null
    var onSentenceStartedListener: ((String) -> Unit)? = null
    var onSentenceFinishedListener: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    private fun applyNarratorVoiceSettings(prefs: AppPreferences) {
        val narrator = prefs.ttsNarrator.value
        var pitch = prefs.ttsPitch.value
        var rate = prefs.ttsRate.value
        
        when (narrator) {
            "cosmic_sage" -> {
                pitch = (pitch * 0.65f).coerceIn(0.5f, 2.0f)
                rate = (rate * 0.75f).coerceIn(0.5f, 2.0f)
            }
            "starlight_sprite" -> {
                pitch = (pitch * 1.4f).coerceIn(0.5f, 2.0f)
                rate = (rate * 1.15f).coerceIn(0.5f, 2.0f)
            }
            "gentle_dreamer" -> {
                pitch = (pitch * 0.85f).coerceIn(0.5f, 2.0f)
                rate = (rate * 0.7f).coerceIn(0.5f, 2.0f)
            }
            // default mode keeps normal values
        }
        
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                val prefs = AppPreferences.getInstance(context)
                applyNarratorVoiceSettings(prefs)
                setupProgressListener()
                onInitCompleted(true)
            } else {
                onInitCompleted(false)
            }
        } else {
            onInitCompleted(false)
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    utteranceId?.let { onSentenceStartedListener?.invoke(it) }
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    onSpeechFinishedListener?.invoke()
                    utteranceId?.let { onSentenceFinishedListener?.invoke(it) }
                }
            }

            @Deprecated("Deprecated in Java", ReplaceWith("onSpeechFinishedListener?.invoke()"))
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    onSpeechFinishedListener?.invoke()
                    utteranceId?.let { onSentenceFinishedListener?.invoke(it) }
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    onSpeechFinishedListener?.invoke()
                    utteranceId?.let { onSentenceFinishedListener?.invoke(it) }
                }
            }
        })
    }

    fun speak(text: String) {
        if (isInitialized) {
            val prefs = AppPreferences.getInstance(context)
            applyNarratorVoiceSettings(prefs)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        }
    }

    fun speakSentence(text: String, utteranceId: String) {
        if (isInitialized) {
            val prefs = AppPreferences.getInstance(context)
            applyNarratorVoiceSettings(prefs)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
    }
}
