package com.maheswara660.flyingbird.domain

data class UserProfile(
    val name: String,
    val avatarId: Int,
    val highScore: Int,
    val gamesPlayed: Int
)

data class Settings(
    val musicVolume: Float,     // 0f to 100f
    val effectsVolume: Float,   // 0f to 100f
    val theme: String,          // "default", "sunset", "winter"
    val graphicsQuality: String,// "low", "medium", "high", "ultra"
    val reducedMotion: Boolean,
    val colorBlind: Boolean,
    val largeText: Boolean,
    val highContrast: Boolean,
    val birdColor: String       // "yellow", "blue", "red"
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAt: String?
)

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val date: String
)
