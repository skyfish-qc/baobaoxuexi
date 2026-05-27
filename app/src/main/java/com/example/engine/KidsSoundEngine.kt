package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class KidsSoundEngine(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    var isMute = false

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.CHINA)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("KidsSoundEngine", "Chinese language pack is missing or not supported on this device. Trying default Locale.")
                        val defaultResult = tts?.setLanguage(Locale.CHINESE)
                        isTtsReady = (defaultResult != TextToSpeech.LANG_MISSING_DATA && defaultResult != TextToSpeech.LANG_NOT_SUPPORTED)
                    } else {
                        isTtsReady = true
                    }
                } else {
                    Log.e("KidsSoundEngine", "TTS initialization failed.")
                }
            }
        } catch (e: Exception) {
            Log.e("KidsSoundEngine", "Error initializing TTS: ${e.message}")
        }
    }

    /**
     * Synthesize spoken words with automatic fallback logging.
     */
    fun speak(text: String, stopPrevious: Boolean = true) {
        if (isMute) return
        if (isTtsReady) {
            try {
                val queueMode = if (stopPrevious) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(text, queueMode, null, "ToddlerSpeakId")
            } catch (e: Exception) {
                Log.e("KidsSoundEngine", "TTS speak failed: ${e.message}")
            }
        } else {
            Log.w("KidsSoundEngine", "TTS is not ready yet or unsupported. Text requested: $text")
        }
    }

    /**
     * Play reward sound: A cheerful, rising retro synth arpeggio (C5 -> E5 -> G5 -> C6).
     */
    fun playSuccess() {
        if (isMute) return
        CoroutineScope(Dispatchers.Default).launch {
            playTone(523.25, 120) // C5
            playTone(659.25, 120) // E5
            playTone(783.99, 120) // G5
            playTone(1046.50, 220) // C6
        }
    }

    /**
     * Play incorrect chime: A soft, low falling "boing" (G3 -> E3).
     */
    fun playIncorrect() {
        if (isMute) return
        CoroutineScope(Dispatchers.Default).launch {
            playTone(196.00, 150) // G3
            playTone(164.81, 250) // E3
        }
    }

    /**
     * Play a cute, high-frequency bubble clicking pop sound (C6 for a short slice).
     */
    fun playClick() {
        if (isMute) return
        CoroutineScope(Dispatchers.Default).launch {
            playTone(987.77, 60) // B5 quick pop
        }
    }

    /**
     * DSP Tone synthesis helper generating instant, artifact-free sinusoidal waves on child DeX support.
     */
    private fun playTone(frequency: Double, durationMs: Int) {
        try {
            val sampleRate = 8000
            val numSamples = (durationMs * sampleRate) / 1000
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)

            for (i in 0 until numSamples) {
                // simple sine wave formula with a short volume envelope (fade-out in last 20%)
                val fadeEnvelope = if (i > numSamples * 0.8) {
                    (numSamples - i).toDouble() / (numSamples * 0.2)
                } else {
                    1.0
                }
                sample[i] = sin(2 * Math.PI * i / (sampleRate / frequency)) * fadeEnvelope
            }

            var idx = 0
            for (dVal in sample) {
                val valShort = (dVal * 25000).toInt().toShort() // Amplitude scaled below clipping limits
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 10)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("KidsSoundEngine", "Error generating synth tone: ${e.message}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("KidsSoundEngine", "Error shutting down TTS: ${e.message}")
        }
    }
}
