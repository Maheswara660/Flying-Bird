package com.maheswara660.flyingbird.audio

class IosAudioPlayer : AudioPlayer {
    override fun playFlap() {}
    override fun playHit() {}
    override fun playPoint() {}
    override fun playGameOver() {}
    override fun playClick() {}
    override fun playMusic(key: String) {}
    override fun stopMusic() {}
    override fun setMusicVolume(vol: Float) {}
    override fun setEffectsVolume(vol: Float) {}
}
