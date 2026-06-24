package com.maheswara660.flyingbird.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
    private var musicVol = 0.8f
    private var effectsVol = 0.9f
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<String, Int>()
    private var mediaPlayer: android.media.MediaPlayer? = null
    private var currentMusicKey: String? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()

        // Dynamically load sound resource IDs from res/raw to keep code package-name agnostic
        try {
            val packageName = context.packageName
            val res = context.resources
            soundIds["wing"] = soundPool.load(context, res.getIdentifier("wing", "raw", packageName), 1)
            soundIds["hit"] = soundPool.load(context, res.getIdentifier("hit", "raw", packageName), 1)
            soundIds["point"] = soundPool.load(context, res.getIdentifier("point", "raw", packageName), 1)
            soundIds["die"] = soundPool.load(context, res.getIdentifier("die", "raw", packageName), 1)
            soundIds["swoosh"] = soundPool.load(context, res.getIdentifier("swoosh", "raw", packageName), 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSound(key: String) {
        val soundId = soundIds[key] ?: return
        if (effectsVol <= 0f) return
        soundPool.play(soundId, effectsVol, effectsVol, 1, 0, 1.0f)
    }

    override fun playFlap() {
        playSound("wing")
    }

    override fun playHit() {
        playSound("hit")
    }

    override fun playPoint() {
        playSound("point")
    }

    override fun playGameOver() {
        playSound("die")
    }

    override fun playClick() {
        playSound("swoosh")
    }

    override fun playMusic(key: String) {
        if (currentMusicKey == key && mediaPlayer?.isPlaying == true) {
            return
        }
        stopMusic()
        currentMusicKey = key
        
        try {
            val packageName = context.packageName
            val res = context.resources
            val resId = res.getIdentifier("background_music", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = android.media.MediaPlayer.create(context, resId).apply {
                    isLooping = true
                    setVolume(musicVol, musicVol)
                    start()
                }
            } else {
                println("Android music resource not found: background_music")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        currentMusicKey = null
    }

    override fun setMusicVolume(vol: Float) {
        musicVol = vol / 100f
        try {
            mediaPlayer?.setVolume(musicVol, musicVol)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setEffectsVolume(vol: Float) {
        effectsVol = vol / 100f
    }
}
