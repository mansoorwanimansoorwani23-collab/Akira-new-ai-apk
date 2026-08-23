package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AkiraVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class AkiraAudioEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // 0.0f to 1.0f reactive audio energy amplitude for visualizer orb & waveforms
    private val _audioAmplitude = MutableStateFlow(0.15f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private var amplitudeSimulationJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            Log.e("AkiraAudioEngine", "ToneGenerator init error: ${e.message}")
        }
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTtsReady = true
                        tts.setPitch(1.15f) // Akira's bright, lively female pitch
                        tts.setSpeechRate(1.05f)
                    }
                }
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                startSpeakingAmplitudeSimulation()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                stopAmplitudeSimulation()
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                stopAmplitudeSimulation()
            }
        })
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSpeechError?.invoke("Voice recognition is not supported on this device.")
            return
        }

        stopSpeaking() // Barge-in interruption
        playCyberChime(ToneGenerator.TONE_PROP_BEEP)

        coroutineScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                _isListening.value = true
                _liveTranscript.value = ""
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("AkiraAudioEngine", "startListening error: ${e.message}")
                _isListening.value = false
                onSpeechError?.invoke(e.localizedMessage ?: "Failed to start listening")
            }
        }
    }

    fun stopListening() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("AkiraAudioEngine", "stopListening error: ${e.message}")
            } finally {
                _isListening.value = false
                stopAmplitudeSimulation()
            }
        }
    }

    fun speak(text: String, voice: AkiraVoice? = null) {
        if (!isTtsReady || textToSpeech == null) {
            return
        }

        coroutineScope.launch(Dispatchers.Main) {
            voice?.let {
                textToSpeech?.setPitch(it.pitch)
                textToSpeech?.setSpeechRate(it.speed)
            }

            val utteranceId = "akira_${System.currentTimeMillis()}"
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }

            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun stopSpeaking() {
        if (_isSpeaking.value) {
            textToSpeech?.stop()
            _isSpeaking.value = false
            stopAmplitudeSimulation()
        }
    }

    fun playCyberChime(toneType: Int = ToneGenerator.TONE_PROP_ACK) {
        try {
            toneGenerator?.startTone(toneType, 80)
        } catch (e: Exception) {
            Log.e("AkiraAudioEngine", "Chime error: ${e.message}")
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Map RMS dB (typically -2 to 10) to 0.1f - 1.0f amplitude
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                _audioAmplitude.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                stopAmplitudeSimulation()
            }

            override fun onError(error: Int) {
                _isListening.value = false
                stopAmplitudeSimulation()
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Voice link interrupted ($error)"
                }
                if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    onSpeechError?.invoke(message)
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                stopAmplitudeSimulation()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""
                if (spokenText.isNotBlank()) {
                    _liveTranscript.value = spokenText
                    onSpeechRecognized?.invoke(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull() ?: ""
                if (partialText.isNotBlank()) {
                    _liveTranscript.value = partialText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun startSpeakingAmplitudeSimulation() {
        amplitudeSimulationJob?.cancel()
        amplitudeSimulationJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive && _isSpeaking.value) {
                val nextAmp = Random.nextFloat() * 0.7f + 0.3f
                _audioAmplitude.value = nextAmp
                delay(75)
            }
            _audioAmplitude.value = 0.15f
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeSimulationJob?.cancel()
        _audioAmplitude.value = 0.15f
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            toneGenerator?.release()
        } catch (e: Exception) {
            Log.e("AkiraAudioEngine", "Release error: ${e.message}")
        }
    }
}
