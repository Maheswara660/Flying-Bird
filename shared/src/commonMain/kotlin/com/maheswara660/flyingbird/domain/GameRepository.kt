package com.maheswara660.flyingbird.domain

import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updateHighScore(score: Int)
    suspend fun incrementGamesPlayed()

    fun getSettings(): Flow<Settings>
    suspend fun saveSettings(settings: Settings)

    fun getAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(id: String)
    suspend fun initializeAchievements()

    fun getLeaderboard(): Flow<List<LeaderboardEntry>>
    suspend fun addLeaderboardEntry(name: String, score: Int)
}
