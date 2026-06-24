package com.maheswara660.flyingbird.audio

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

class DesktopAudioPlayer : AudioPlayer {
    private var musicVol = 0.8f
    private var effectsVol = 0.9f
    private var musicClip: javax.sound.sampled.Clip? = null
    private var currentMusicKey: String? = null

    private fun playSound(filename: String) {
        if (effectsVol <= 0f) return
        Thread {
            try {
                val resource = javaClass.classLoader.getResource(filename)
                if (resource != null) {
                    val audioStream = AudioSystem.getAudioInputStream(resource)
                    val clip = AudioSystem.getClip()
                    clip.open(audioStream)
                    
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                        val dB = (Math.log10(effectsVol.toDouble().coerceAtLeast(0.0001)) * 20.0).toFloat()
                        gainControl.value = dB.coerceIn(gainControl.minimum, gainControl.maximum)
                    }
                    
                    clip.addLineListener { event ->
                        if (event.type == LineEvent.Type.STOP) {
                            clip.close()
                            audioStream.close()
                        }
                    }
                    clip.start()
                } else {
                    println("Audio resource not found: $filename")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun playFlap() {
        playSound("wing.wav")
    }

    override fun playHit() {
        playSound("hit.wav")
    }

    override fun playPoint() {
        playSound("point.wav")
    }

    override fun playGameOver() {
        playSound("die.wav")
    }

    override fun playClick() {
        playSound("swoosh.wav")
    }

    override fun playMusic(key: String) {
        if (currentMusicKey == key && musicClip?.isRunning == true) {
            return
        }
        stopMusic()
        currentMusicKey = key
        
        Thread {
            try {
                val resource = javaClass.classLoader.getResource("background_music.wav")
                if (resource != null) {
                    val audioStream = AudioSystem.getAudioInputStream(resource)
                    val clip = AudioSystem.getClip()
                    clip.open(audioStream)
                    
                    musicClip = clip
                    updateMusicVolume()
                    
                    clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY)
                    clip.start()
                } else {
                    println("Music resource not found: background_music.wav")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun stopMusic() {
        try {
            musicClip?.let {
                if (it.isRunning) {
                    it.stop()
                }
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        musicClip = null
        currentMusicKey = null
    }

    override fun setMusicVolume(vol: Float) {
        musicVol = vol / 100f
        updateMusicVolume()
    }

    override fun setEffectsVolume(vol: Float) {
        effectsVol = vol / 100f
    }

    private fun updateMusicVolume() {
        try {
            musicClip?.let { clip ->
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                    val dB = (Math.log10(musicVol.toDouble().coerceAtLeast(0.0001)) * 20.0).toFloat()
                    gainControl.value = dB.coerceIn(gainControl.minimum, gainControl.maximum)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
