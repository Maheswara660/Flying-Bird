package com.maheswara660.flyingbird.audio

interface AudioPlayer {
    fun playFlap()
    fun playHit()
    fun playPoint()
    fun playGameOver()
    fun playClick()
    fun playMusic(key: String)
    fun stopMusic()
    fun setMusicVolume(vol: Float)
    fun setEffectsVolume(vol: Float)
}
