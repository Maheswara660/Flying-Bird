package com.maheswara660.flyingbird.data

import com.maheswara660.flyingbird.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.datetime.Clock

class GameRepositoryImpl(databaseHelper: DatabaseHelper) : GameRepository {
    private val db = databaseHelper.database
    private val dispatcher = Dispatchers.IO

    override fun getUserProfile(): Flow<UserProfile> {
        return db.appDatabaseQueries.getUserProfile()
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { entity ->
                if (entity != null) {
                    UserProfile(entity.name, entity.avatarId.toInt(), entity.highScore.toInt(), entity.gamesPlayed.toInt())
                } else {
                    UserProfile("Survivor", 0, 0, 0)
                }
            }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        db.appDatabaseQueries.insertUserProfile(
            profile.name,
            profile.avatarId.toLong(),
            profile.highScore.toLong(),
            profile.gamesPlayed.toLong()
        )
    }

    override suspend fun updateHighScore(score: Int) {
        val current = db.appDatabaseQueries.getUserProfile().executeAsOneOrNull()
        if (current == null) {
            db.appDatabaseQueries.insertUserProfile("Survivor", 0, score.toLong(), 1)
        } else if (score > current.highScore) {
            db.appDatabaseQueries.insertUserProfile(current.name, current.avatarId, score.toLong(), current.gamesPlayed)
        }
    }

    override suspend fun incrementGamesPlayed() {
        val current = db.appDatabaseQueries.getUserProfile().executeAsOneOrNull()
        if (current == null) {
            db.appDatabaseQueries.insertUserProfile("Survivor", 0, 0, 1)
        } else {
            db.appDatabaseQueries.insertUserProfile(current.name, current.avatarId, current.highScore, current.gamesPlayed + 1)
        }
    }

    override fun getSettings(): Flow<Settings> {
        return db.appDatabaseQueries.getSettings()
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { entity ->
                if (entity != null) {
                    Settings(
                        entity.musicVolume.toFloat(),
                        entity.effectsVolume.toFloat(),
                        entity.theme,
                        entity.graphicsQuality,
                        entity.reducedMotion == 1L,
                        entity.colorBlind == 1L,
                        entity.largeText == 1L,
                        entity.highContrast == 1L,
                        entity.birdColor
                    )
                } else {
                    Settings(80f, 90f, "default", "high", false, false, false, false, "yellow")
                }
            }
    }

    override suspend fun saveSettings(settings: Settings) {
        db.appDatabaseQueries.insertSettings(
            settings.musicVolume.toDouble(),
            settings.effectsVolume.toDouble(),
            settings.theme,
            settings.graphicsQuality,
            if (settings.reducedMotion) 1L else 0L,
            if (settings.colorBlind) 1L else 0L,
            if (settings.largeText) 1L else 0L,
            if (settings.highContrast) 1L else 0L,
            settings.birdColor
        )
    }

    override fun getAchievements(): Flow<List<Achievement>> {
        return db.appDatabaseQueries.getAchievements()
            .asFlow()
            .mapToList(dispatcher)
            .map { list ->
                list.map { entity ->
                    Achievement(
                        entity.id,
                        entity.title,
                        entity.description,
                        entity.unlocked == 1L,
                        entity.unlockedAt
                    )
                }
            }
    }

    override suspend fun unlockAchievement(id: String) {
        val dateString = Clock.System.now().toString().substring(0, 19).replace('T', ' ')
        db.appDatabaseQueries.unlockAchievement(dateString, id)
    }

    override suspend fun initializeAchievements() {
        val defaultAchievements = listOf(
            Achievement("first_flap", "First Wing Flap", "Take off for the first time", false, null),
            Achievement("score_10", "Survivor", "Reach a score of 10 points", false, null),
            Achievement("score_50", "Biome Explorer", "Reach a score of 50 points and explore new areas", false, null),
            Achievement("score_100", "Legend of the Fall", "Reach a score of 100 points", false, null),
            Achievement("survive_5m", "Patience of Nature", "Survive in a single run for 5 minutes", false, null),
            Achievement("play_10", "Dying Effort", "Play the game 10 times", false, null)
        )
        for (ach in defaultAchievements) {
            db.appDatabaseQueries.insertAchievement(
                ach.id,
                ach.title,
                ach.description,
                if (ach.unlocked) 1L else 0L,
                ach.unlockedAt
            )
        }
    }

    override fun getLeaderboard(): Flow<List<LeaderboardEntry>> {
        return db.appDatabaseQueries.getLeaderboard()
            .asFlow()
            .mapToList(dispatcher)
            .map { list ->
                list.mapIndexed { index, entity ->
                    LeaderboardEntry(
                        index + 1,
                        entity.playerName,
                        entity.score.toInt(),
                        entity.date
                    )
                }
            }
    }

    override suspend fun addLeaderboardEntry(name: String, score: Int) {
        val dateString = Clock.System.now().toString().substring(0, 10) // YYYY-MM-DD
        db.appDatabaseQueries.insertLeaderboard(name, score.toLong(), dateString)
    }
}
