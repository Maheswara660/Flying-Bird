package com.maheswara660.flyingbird.game

import com.maheswara660.flyingbird.domain.*
import com.maheswara660.flyingbird.physics.*
import com.maheswara660.flyingbird.audio.AudioPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlin.math.sin
import kotlin.random.Random

enum class GameState {
    SPLASH,
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    SETTINGS
}

class GameEngine(
    private val repository: GameRepository,
    private val audioPlayer: AudioPlayer,
    private val scope: CoroutineScope
) {
    // Game States (reactive flows for UI integration)
    private val _state = MutableStateFlow(GameState.SPLASH)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _birdY = MutableStateFlow(300f)
    val birdY: StateFlow<Float> = _birdY.asStateFlow()

    private val _birdVelocity = MutableStateFlow(0f)
    val birdVelocity: StateFlow<Float> = _birdVelocity.asStateFlow()

    private val _birdRotation = MutableStateFlow(0f)
    val birdRotation: StateFlow<Float> = _birdRotation.asStateFlow()

    private val _obstacles = MutableStateFlow<List<ObstacleInstance>>(emptyList())
    val obstacles: StateFlow<List<ObstacleInstance>> = _obstacles.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _obstaclesPassed = MutableStateFlow(0)
    val obstaclesPassed: StateFlow<Int> = _obstaclesPassed.asStateFlow()

    // Persistent State
    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .stateIn(scope, SharingStarted.Eagerly, UserProfile("Survivor", 0, 0, 0))

    val settings: StateFlow<Settings> = repository.getSettings()
        .stateIn(scope, SharingStarted.Eagerly, Settings(80f, 90f, "default", "high", false, false, false, false, "yellow"))

    val achievements: StateFlow<List<Achievement>> = repository.getAchievements()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())


    // Visuals & Weather
    private val _weather = MutableStateFlow("clear")
    val weather: StateFlow<String> = _weather.asStateFlow()

    private val _biome = MutableStateFlow("city")
    val biome: StateFlow<String> = _biome.asStateFlow()

    // Particles list
    val particles = mutableListOf<Particle>()

    // Local loop constants
    private var lastTimeNanos = 0L
    private var spawnTimer = 0f
    private var totalRunTime = 0f
    private var firstFlapDone = false

    // Constants
    private val gravity = 8.5f
    private val flapImpulse = -220f
    private val maxFallSpeed = 450f
    private val rotationMax = 45f
    private val rotationMin = -25f
    private val baseObstacleSpeed = 270f // Units per second (4.5 per frame at 60 FPS)

    init {
        scope.launch {
            repository.initializeAchievements()
            // Load audio volumes
            settings.collect { currentSettings ->
                audioPlayer.setMusicVolume(currentSettings.musicVolume)
                audioPlayer.setEffectsVolume(currentSettings.effectsVolume)
            }
        }
        setupSplash()
    }

    private fun setupSplash() {
        _state.value = GameState.SPLASH
        initializeParticles()
        scope.launch {
            delay(2500) // Splash duration 2.5s
            transitionToMenu()
        }
    }

    fun transitionToMenu() {
        _state.value = GameState.MENU
        audioPlayer.playMusic("bg_music")
    }

    private fun initializeParticles() {
        particles.clear()
        val currentTheme = settings.value.theme
        val count = when (currentTheme) {
            "sunset" -> 40
            "winter" -> 60
            else -> 80
        }
        repeat(count) {
            val x = Random.nextFloat() * PhysicsEngine.LOGICAL_WIDTH
            val y = Random.nextFloat() * PhysicsEngine.LOGICAL_HEIGHT
            var vx = 0f
            var vy = 0f
            var size = 0f
            var alpha = 0f
            when (currentTheme) {
                "sunset" -> {
                    vx = -(60f + Random.nextFloat() * 80f)
                    vy = -(30f + Random.nextFloat() * 40f)
                    size = 2f + Random.nextFloat() * 4f
                    alpha = 0.4f + Random.nextFloat() * 0.5f
                }
                "winter" -> {
                    vx = -(30f + Random.nextFloat() * 50f)
                    vy = 50f + Random.nextFloat() * 70f
                    size = 2f + Random.nextFloat() * 5f
                    alpha = 0.4f + Random.nextFloat() * 0.5f
                }
                else -> {
                    vx = -(80f + Random.nextFloat() * 60f)
                    vy = 400f + Random.nextFloat() * 200f
                    size = 1f + Random.nextFloat() * 2f
                    alpha = 0.3f + Random.nextFloat() * 0.4f
                }
            }
            particles.add(Particle(x, y, vx, vy, size, alpha))
        }
    }

    fun startGame() {
        // Reset game stats
        _score.value = 0
        _distance.value = 0f
        _obstaclesPassed.value = 0
        _birdY.value = 250f
        _birdVelocity.value = 0f
        _birdRotation.value = 0f
        _obstacles.value = emptyList()
        spawnTimer = 0f
        totalRunTime = 0f
        _biome.value = "city"
        _weather.value = "clear"

        initializeParticles()

        lastTimeNanos = 0L
        firstFlapDone = false
        _state.value = GameState.PLAYING
        audioPlayer.playMusic("bg_music")
    }

    fun flap() {
        if (_state.value != GameState.PLAYING) return
        if (!firstFlapDone) {
            firstFlapDone = true
        }
        _birdVelocity.value = flapImpulse
        audioPlayer.playFlap()
        
        scope.launch {
            repository.unlockAchievement("first_flap")
        }
    }

    fun pauseGame() {
        if (_state.value == GameState.PLAYING) {
            _state.value = GameState.PAUSED
        }
    }

    fun resumeGame() {
        if (_state.value == GameState.PAUSED) {
            _state.value = GameState.PLAYING
            lastTimeNanos = 0L // reset timer to prevent huge time skip
        }
    }

    fun openSettings() {
        _state.value = GameState.SETTINGS
    }

    fun selectTheme(themeName: String) {
        scope.launch {
            repository.saveSettings(settings.value.copy(theme = themeName))
            initializeParticles()
        }
    }

    fun selectBirdColor(color: String) {
        scope.launch {
            repository.saveSettings(settings.value.copy(birdColor = color))
        }
    }

    fun selectGraphics(graphicsQuality: String) {
        scope.launch {
            repository.saveSettings(settings.value.copy(graphicsQuality = graphicsQuality))
        }
    }

    fun toggleAccessibilitySetting(type: String) {
        scope.launch {
            val curr = settings.value
            val updated = when (type) {
                "reducedMotion" -> curr.copy(reducedMotion = !curr.reducedMotion)
                "colorBlind" -> curr.copy(colorBlind = !curr.colorBlind)
                "largeText" -> curr.copy(largeText = !curr.largeText)
                "highContrast" -> curr.copy(highContrast = !curr.highContrast)
                else -> curr
            }
            repository.saveSettings(updated)
        }
    }

    fun saveVolumes(music: Float, effects: Float) {
        scope.launch {
            repository.saveSettings(settings.value.copy(musicVolume = music, effectsVolume = effects))
        }
    }


    fun tick(nanos: Long) {
        if (lastTimeNanos == 0L) {
            lastTimeNanos = nanos
            return
        }

        val dt = ((nanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
        lastTimeNanos = nanos

        if (_state.value == GameState.PLAYING) {
            updatePlaying(dt)
        } else if (_state.value == GameState.MENU || _state.value == GameState.SPLASH) {
            updateMenuBackground(dt)
        }
    }

    private fun updateMenuBackground(dt: Float) {
        val speedFactor = if (settings.value.reducedMotion) 0.1f else 1.0f
        val currentTheme = settings.value.theme
        for (p in particles) {
            p.x += p.vx * dt * speedFactor
            p.y += p.vy * dt * speedFactor
            if (p.x < -10f || p.y < -10f || p.y > PhysicsEngine.LOGICAL_HEIGHT + 10f) {
                if (currentTheme == "sunset") {
                    if (Random.nextBoolean()) {
                        p.x = PhysicsEngine.LOGICAL_WIDTH + 5f
                        p.y = Random.nextFloat() * PhysicsEngine.LOGICAL_HEIGHT
                    } else {
                        p.x = Random.nextFloat() * PhysicsEngine.LOGICAL_WIDTH
                        p.y = PhysicsEngine.LOGICAL_HEIGHT + 5f
                    }
                } else {
                    if (Random.nextBoolean()) {
                        p.x = PhysicsEngine.LOGICAL_WIDTH + 5f
                        p.y = Random.nextFloat() * PhysicsEngine.LOGICAL_HEIGHT
                    } else {
                        p.x = Random.nextFloat() * PhysicsEngine.LOGICAL_WIDTH
                        p.y = -5f
                    }
                }
            }
        }
    }

    private fun updatePlaying(dt: Float) {
        if (!firstFlapDone) {
            totalRunTime += dt
            _birdY.value = 250f + sin(totalRunTime * 5f) * 15f
            _birdVelocity.value = 0f
            _birdRotation.value = 0f
            return
        }

        val speedMultiplier = 1f + _score.value * 0.015f // Speed scales 1.5% per point
        val currentSpeed = baseObstacleSpeed * speedMultiplier
        val currentGap = (200f - _score.value * 2f).coerceAtLeast(100f) // Gap shrinks 2px per point, min 100px

        println("TICK PLAYING: birdY=${_birdY.value}, velocity=${_birdVelocity.value}, dt=$dt")
        totalRunTime += dt

        // 1. Update Bird
        val (newY, newV) = PhysicsEngine.updateBird(_birdY.value, _birdVelocity.value, gravity, maxFallSpeed, dt)
        _birdY.value = newY
        _birdVelocity.value = newV
        
        val targetRotation = PhysicsEngine.getBirdRotation(newV, rotationMin, rotationMax)
        val currentRotation = _birdRotation.value
        val lerpFactor = (dt * 12f).coerceIn(0f, 1f)
        _birdRotation.value = currentRotation + (targetRotation - currentRotation) * lerpFactor

        // 2. Spawn Obstacles
        spawnTimer += dt
        val spawnInterval = 1.7f / speedMultiplier
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            spawnObstacle(currentGap)
        }

        // 3. Move and Update Obstacles
        val activeObstacles = _obstacles.value.toMutableList()
        val iterator = activeObstacles.listIterator()
        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.x -= currentSpeed * dt

            // Check if passed bird
            if (!obs.passed && obs.x + PhysicsEngine.OBSTACLE_WIDTH < PhysicsEngine.BIRD_X) {
                obs.passed = true
                _score.value += 1
                _obstaclesPassed.value += 1
                audioPlayer.playPoint()
                checkAchievements()
                updateProgression()
            }

            // Remove out of screen
            if (obs.x + PhysicsEngine.OBSTACLE_WIDTH < 0) {
                iterator.remove()
            }
        }
        _obstacles.value = activeObstacles

        // 4. Update Particles (reduced motion applies)
        val particleSpeedFactor = if (settings.value.reducedMotion) 0.1f else 1.0f
        val currentTheme = settings.value.theme
        for (p in particles) {
            p.x += p.vx * dt * speedMultiplier * particleSpeedFactor
            p.y += p.vy * dt * speedMultiplier * particleSpeedFactor
            if (p.x < -10f || p.y < -10f || p.y > PhysicsEngine.LOGICAL_HEIGHT + 10f) {
                if (currentTheme == "sunset") {
                    if (Random.nextBoolean()) {
                        p.x = PhysicsEngine.LOGICAL_WIDTH + 5f
                        p.y = Random.nextFloat() * PhysicsEngine.LOGICAL_HEIGHT
                    } else {
                        p.x = Random.nextFloat() * PhysicsEngine.LOGICAL_WIDTH
                        p.y = PhysicsEngine.LOGICAL_HEIGHT + 5f
                    }
                } else {
                    if (Random.nextBoolean()) {
                        p.x = PhysicsEngine.LOGICAL_WIDTH + 5f
                        p.y = Random.nextFloat() * PhysicsEngine.LOGICAL_HEIGHT
                    } else {
                        p.x = Random.nextFloat() * PhysicsEngine.LOGICAL_WIDTH
                        p.y = -5f
                    }
                }
            }
        }

        // 5. Update distance
        _distance.value += currentSpeed * dt * 0.02f // Convert logical pixels to logical meters

        // 6. Check collisions
        if (PhysicsEngine.checkCollision(_birdY.value, _obstacles.value)) {
            triggerGameOver()
        }
    }

    private fun spawnObstacle(gapSize: Float) {
        val id = "obs_${Clock.System.now().toEpochMilliseconds()}"
        val types = ObstacleType.entries
        val type = types[Random.nextInt(types.size)]

        // Random height for top pipe. Ground is at 520. Remaining is 520.
        // Min pipe height is 50f. Max is 520 - gapSize - 50.
        val minHeight = 60f
        val maxHeight = PhysicsEngine.GROUND_Y - gapSize - 60f
        val topHeight = minHeight + Random.nextFloat() * (maxHeight - minHeight)

        // Diagonal offset: starting from score >= 5, 40% chance of diagonal obstacle
        val bottomXOffset = if (_score.value >= 5 && Random.nextFloat() < 0.4f) {
            val direction = if (Random.nextBoolean()) 1f else -1f
            direction * (30f + Random.nextFloat() * 40f)
        } else {
            0f
        }

        val imageIndex = Random.nextInt(3)

        val newObs = ObstacleInstance(
            id = id,
            type = type,
            x = PhysicsEngine.LOGICAL_WIDTH,
            topHeight = topHeight,
            gapSize = gapSize,
            bottomXOffset = bottomXOffset,
            imageIndex = imageIndex,
            passed = false
        )
        _obstacles.value = _obstacles.value + newObs
    }

    private fun updateProgression() {
        // Biome changes every 50 points
        val currentScore = _score.value
        _biome.value = when {
            currentScore >= 100 -> "wilderness"
            currentScore >= 50 -> "industrial"
            else -> "city"
        }

        // Weather progression based on score
        _weather.value = when {
            currentScore in 20..39 -> "dust_storm"
            currentScore in 60..79 -> "rain"
            currentScore >= 120 -> "dust_storm"
            else -> "clear"
        }
    }

    private fun checkAchievements() {
        val currentScore = _score.value
        scope.launch {
            if (currentScore >= 10) repository.unlockAchievement("score_10")
            if (currentScore >= 50) repository.unlockAchievement("score_50")
            if (currentScore >= 100) repository.unlockAchievement("score_100")
            if (totalRunTime >= 300f) repository.unlockAchievement("survive_5m") // 300 seconds
        }
    }

    private fun triggerGameOver() {
        _state.value = GameState.GAME_OVER
        audioPlayer.playHit()
        audioPlayer.playGameOver()

        scope.launch {
            // Save settings high score and insert in leaderboard
            repository.updateHighScore(_score.value)
            repository.incrementGamesPlayed()
            repository.addLeaderboardEntry(userProfile.value.name, _score.value)
            
            // Check play_10 achievement
            val profile = repository.getUserProfile().first()
            if (profile.gamesPlayed >= 10) {
                repository.unlockAchievement("play_10")
            }
        }
    }
}

class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val alpha: Float
)
