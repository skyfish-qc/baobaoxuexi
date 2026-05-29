package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.math.sin

class KidsSoundEngine(private val context: Context) {
    private val soundScope = CoroutineScope(Dispatchers.IO)
    private var currentMediaPlayer: MediaPlayer? = null
    var isMute = false

    private val cacheDir = File(context.cacheDir, "audio_cache").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    /**
     * MD5 Hashing helper to generate uniquely recognizable, clean file keys for text sentences.
     */
    private fun String.md5(): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            this.hashCode().toString()
        }
    }

    /**
     * Securely fetches and caches high-fidelity MP3 voice outputs from Youdao's speech synthesis engine.
     */
    private fun downloadAudioFile(text: String, file: File): Boolean {
        var connection: java.net.URLConnection? = null
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://dict.youdao.com/dictvoice?audio=$encodedText&le=zh"
            val url = URL(urlString)
            connection = url.openConnection()
            connection.connectTimeout = 4000
            connection.readTimeout = 4000

            connection.getInputStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return file.exists() && file.length() > 0
        } catch (e: Exception) {
            Log.e("KidsSoundEngine", "Failed to download pronunciation audio for: \"$text\". Error: ${e.message}")
            if (file.exists()) {
                file.delete()
            }
            return false
        }
    }

    /**
     * Speaks the sentence by loading from persistent cache or fetching it over the network.
     * Stops any currently playing audio if stopPrevious is enabled.
     */
    fun speak(text: String, stopPrevious: Boolean = true) {
        if (isMute) return

        soundScope.launch {
            if (stopPrevious) {
                stopPlaybackInternal()
            }

            val key = text.md5()
            val cachedFile = File(cacheDir, "$key.mp3")

            // Determine if asset is ready locally
            val isReady = if (cachedFile.exists() && cachedFile.length() > 0) {
                true
            } else {
                downloadAudioFile(text, cachedFile)
            }

            if (isReady && cachedFile.exists()) {
                playMp3FileInternal(cachedFile)
            } else {
                Log.w("KidsSoundEngine", "Audios cache miss and offline. Falling back to synth chime.")
                // Play short synth melody as resilient secondary indicator
                playTone(550.0, 120)
            }
        }
    }

    /**
     * Play a list of text sentences in background on demand to build a smooth local sound buffer.
     */
    fun preCache(texts: List<String>) {
        soundScope.launch {
            for (text in texts) {
                val key = text.md5()
                val cachedFile = File(cacheDir, "$key.mp3")
                if (!cachedFile.exists() || cachedFile.length() == 0L) {
                    val success = downloadAudioFile(text, cachedFile)
                    if (success) {
                        Log.d("KidsSoundEngine", "Pre-cached: \"$text\"")
                    }
                    kotlinx.coroutines.delay(120)
                }
            }
        }
    }

    private fun stopPlaybackInternal() {
        synchronized(this) {
            try {
                currentMediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                }
                currentMediaPlayer = null
            } catch (e: Exception) {
                Log.e("KidsSoundEngine", "Error stopping playback: ${e.message}")
            }
        }
    }

    private fun playMp3FileInternal(file: File) {
        synchronized(this) {
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                }
                currentMediaPlayer = player
                player.setOnCompletionListener {
                    synchronized(this) {
                        if (currentMediaPlayer == player) {
                            currentMediaPlayer = null
                        }
                    }
                    player.release()
                }
            } catch (e: Exception) {
                Log.e("KidsSoundEngine", "Error playing MP3 file: ${e.message}")
                playTone(600.0, 100)
            }
        }
    }

    /**
     * Play reward sound: A cheerful, rising retro synth arpeggio (C5 -> E5 -> G5 -> C6).
     */
    fun playSuccess() {
        if (isMute) return
        soundScope.launch {
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
        soundScope.launch {
            playTone(196.00, 150) // G3
            playTone(164.81, 250) // E3
        }
    }

    /**
     * Play a cute, high-frequency bubble clicking pop sound (B5 quick pop).
     */
    fun playClick() {
        if (isMute) return
        soundScope.launch {
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
        stopPlaybackInternal()
    }
}
