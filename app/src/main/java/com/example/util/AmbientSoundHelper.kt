package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.io.Serializable
import kotlin.math.sin

class AmbientSoundHelper {
    private var audioTrack: AudioTrack? = null
    @Volatile private var isPlaying = false
    private var thread: Thread? = null
    @Volatile private var volume = 0.5f // 0f to 1f
    @Volatile private var activeSoundscape = "Off" // "Off", "Gentle Rain", "Space Echoes", "Cozy Waves", "Forest Breeze", "Nebula Whisper", "Starfield Hum"
    @Volatile private var previousSoundscape = "Off"
    @Volatile private var fadeProgress = 1.0f // 1.0 means active only, < 1.0 means cross-fading

    fun setVolume(vol: Float) {
        this.volume = vol.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun start(soundscape: String) {
        if (activeSoundscape == soundscape && isPlaying) return
        
        if (soundscape == "Off") {
            if (!isPlaying) {
                activeSoundscape = "Off"
                previousSoundscape = "Off"
                fadeProgress = 1.0f
                return
            }
            // Smoothly fade to silence instead of sudden stop
            previousSoundscape = activeSoundscape
            activeSoundscape = "Off"
            fadeProgress = 0.0f
            return
        }
        
        if (isPlaying) {
            // Initiate a smooth cross-fade from previous active soundscape to the new one
            previousSoundscape = activeSoundscape
            activeSoundscape = soundscape
            fadeProgress = 0.0f
        } else {
            // Fresh start: play active directly with no cross-fade
            activeSoundscape = soundscape
            previousSoundscape = "Off"
            fadeProgress = 1.0f
            isPlaying = true
            
            thread = Thread {
                synthesizeLoop()
            }.apply {
                name = "AmbientSoundSynthesizer"
                priority = Thread.NORM_PRIORITY - 1
                start()
            }
        }
    }

    fun getCurrentSoundscape(): String = activeSoundscape

    fun stop() {
        isPlaying = false
        previousSoundscape = "Off"
        activeSoundscape = "Off"
        fadeProgress = 1.0f
        try {
            thread?.interrupt()
            thread?.join(200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        thread = null
        
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }

    private fun getSample(soundscape: String, t: Double, rand: java.util.Random, lpState: Float): Pair<Double, Float> {
        var currentLp = lpState
        val sample: Double = when (soundscape) {
            "Gentle Rain" -> {
                val rawNoise = (rand.nextFloat() * 2.0f - 1.0f)
                val alpha = 0.15f
                currentLp = currentLp + alpha * (rawNoise - currentLp)
                currentLp.toDouble() * 0.4
            }
            "Space Echoes" -> {
                val lfo1 = sin(2 * Math.PI * 0.08 * t)
                val lfo2 = sin(2 * Math.PI * 0.05 * t)
                val freq1 = 55.0 + 1.2 * lfo1
                val freq2 = 82.5 + 0.8 * lfo2
                val freq3 = 110.0 + 1.5 * lfo1
                
                val wave1 = sin(2 * Math.PI * freq1 * t)
                val wave2 = sin(2 * Math.PI * freq2 * t)
                val wave3 = sin(2 * Math.PI * freq3 * t)
                
                val rawNoise = (rand.nextFloat() * 2.0f - 1.0f)
                val alpha = 0.04f
                currentLp = currentLp + alpha * (rawNoise - currentLp)
                val spaceWindVol = 0.15 + 0.1 * sin(2 * Math.PI * 0.04 * t)
                
                (wave1 * 0.35 + wave2 * 0.25 + wave3 * 0.15 + (currentLp * spaceWindVol)) * 0.7
            }
            "Cozy Waves" -> {
                val waveEnv = 0.5 + 0.45 * sin(2 * Math.PI * (1.0 / 6.0) * t)
                val rawNoise = (rand.nextFloat() * 2.0f - 1.0f)
                val alpha = 0.10f
                currentLp = currentLp + alpha * (rawNoise - currentLp)
                currentLp.toDouble() * waveEnv * 0.9
            }
            "Forest Breeze" -> {
                val windBase = 0.2 + 0.15 * sin(2 * Math.PI * 0.07 * t * sin(2 * Math.PI * 0.01 * t))
                val flutter = 0.08 + 0.08 * sin(2 * Math.PI * 4.5 * t)
                val rawNoise = (rand.nextFloat() * 2.0f - 1.0f)
                val alpha = (0.05f + 0.04f * sin(2 * Math.PI * 0.15 * t).toFloat())
                currentLp = currentLp + alpha * (rawNoise - currentLp)
                currentLp.toDouble() * (windBase + flutter) * 1.0
            }
            "Nebula Whisper" -> {
                val whisperLfo = sin(2 * Math.PI * 0.12 * t)
                val whisperVol = 0.22 + 0.12 * whisperLfo
                val rawNoise = (rand.nextFloat() * 2.0f - 1.0f)
                val alpha = (0.018f + 0.012f * whisperLfo.toFloat())
                currentLp = currentLp + alpha * (rawNoise - currentLp)
                
                val hum1 = sin(2 * Math.PI * 146.83 * t)
                val hum2 = sin(2 * Math.PI * 220.0 * t)
                
                (currentLp * whisperVol * 1.6 + hum1 * 0.10 + hum2 * 0.06) * 0.65
            }
            "Starfield Hum" -> {
                val humLfo = sin(2 * Math.PI * 0.04 * t)
                val f1 = 57.8 + 0.5 * humLfo
                val f2 = 86.7 + 0.3 * sin(2 * Math.PI * 0.03 * t)
                val f3 = 115.6 + 0.4 * sin(2 * Math.PI * 0.05 * t)
                
                val w1 = sin(2 * Math.PI * f1 * t)
                val w2 = sin(2 * Math.PI * f2 * t)
                val w3 = sin(2 * Math.PI * f3 * t)
                
                val rawNoise = (rand.nextFloat() * 2.0f - 1.0f)
                val alpha = 0.04f
                currentLp = currentLp + alpha * (rawNoise - currentLp)
                
                val twinkle1 = sin(2 * Math.PI * 880.0 * t) * (0.012 * (1.0 + sin(2 * Math.PI * 0.25 * t)))
                val twinkle2 = sin(2 * Math.PI * 1320.0 * t) * (0.008 * (1.0 + sin(2 * Math.PI * 0.15 * t)))
                
                (w1 * 0.45 + w2 * 0.35 + w3 * 0.22 + currentLp * 0.12 + twinkle1 + twinkle2) * 0.65
            }
            else -> 0.0
        }
        return Pair(sample, currentLp)
    }

    private fun synthesizeLoop() {
        val sampleRate = 22050 // efficient and high quality enough for white/deep noise
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (bufferSize <= 0) return
        
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
                
            audioTrack = track
            track.setVolume(volume)
            track.play()
            
            val buffer = ShortArray(bufferSize / 2)
            val rand = java.util.Random()
            var phase = 0.0
            var lpStateActive = 0.0f
            var lpStatePrev = 0.0f
            
            // Fading duration in samples: 1.5 seconds at 22050Hz = 33075 samples
            val fadeSamplesCount = 22050 * 1.5
            
            while (isPlaying && !Thread.currentThread().isInterrupted) {
                val currentActive = activeSoundscape
                val currentPrev = previousSoundscape
                val currentFadeProgress = fadeProgress
                
                if (currentActive == "Off" && currentFadeProgress >= 1.0f) {
                    // Fully faded out to silence, stop completely
                    break
                }
                
                for (i in buffer.indices) {
                    val t = phase / sampleRate
                    
                    var activeSample = 0.0
                    var prevSample = 0.0
                    
                    if (currentActive != "Off") {
                        val (s, lp) = getSample(currentActive, t, rand, lpStateActive)
                        activeSample = s
                        lpStateActive = lp
                    }
                    
                    if (currentPrev != "Off" && currentFadeProgress < 1.0f) {
                        val (s, lp) = getSample(currentPrev, t, rand, lpStatePrev)
                        prevSample = s
                        lpStatePrev = lp
                    }
                    
                    // Cross-fade interpolation
                    var sample = if (currentFadeProgress < 1.0f) {
                        (1.0 - currentFadeProgress) * prevSample + currentFadeProgress * activeSample
                    } else {
                        activeSample
                    }
                    
                    // Smoothly increment the fade progress
                    if (fadeProgress < 1.0f) {
                        fadeProgress = (fadeProgress + (1.0 / fadeSamplesCount).toFloat()).coerceAtMost(1.0f)
                    } else if (previousSoundscape != "Off") {
                        previousSoundscape = "Off"
                    }
                    
                    sample = sample.coerceIn(-1.0, 1.0)
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    phase += 1.0
                    if (phase >= 1e9) {
                        phase = 0.0 // reset phase to prevent distortion/overflow
                    }
                }
                
                if (isPlaying) {
                    track.write(buffer, 0, buffer.size)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Cleanup if stopped naturally (e.g. fade out finished)
            if (activeSoundscape == "Off" && fadeProgress >= 1.0f) {
                stop()
            }
        }
    }
}
