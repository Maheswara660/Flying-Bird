package com.maheswara660.flyingbird.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.maheswara660.flyingbird.game.GameEngine
import com.maheswara660.flyingbird.game.GameState
import com.maheswara660.flyingbird.physics.PhysicsEngine
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.painterResource
import flyingbird.shared.generated.resources.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock

@Composable
fun App(
    engine: GameEngine,
    onClose: () -> Unit = {},
    onMinimize: () -> Unit = {},
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {}
) {
    val state by engine.state.collectAsState()
    val score by engine.score.collectAsState()
    val birdY by engine.birdY.collectAsState()
    val birdRotation by engine.birdRotation.collectAsState()
    val obstacles by engine.obstacles.collectAsState()
    val bestScore by engine.userProfile.collectAsState()
    val distance by engine.distance.collectAsState()
    val obstaclesPassed by engine.obstaclesPassed.collectAsState()
    val biome by engine.biome.collectAsState()
    val weather by engine.weather.collectAsState()
    val settings by engine.settings.collectAsState()
    val achievements by engine.achievements.collectAsState()
    val userProfile by engine.userProfile.collectAsState()

    val defaultBackground = painterResource(Res.drawable.default_background)
    val defaultBase = painterResource(Res.drawable.default_base)
    val defaultObstacle1Top = painterResource(Res.drawable.default_obstacle_1_top)
    val defaultObstacle1Bottom = painterResource(Res.drawable.default_obstacle_1_bottom)
    val defaultObstacle2Top = painterResource(Res.drawable.default_obstacle_2_top)
    val defaultObstacle2Bottom = painterResource(Res.drawable.default_obstacle_2_bottom)
    val defaultObstacle3Top = painterResource(Res.drawable.default_obstacle_3_top)
    val defaultObstacle3Bottom = painterResource(Res.drawable.default_obstacle_3_bottom)

    val sunsetBackground = painterResource(Res.drawable.sunset_background)
    val sunsetBase = painterResource(Res.drawable.sunset_base)
    val sunsetObstacle1Top = painterResource(Res.drawable.sunset_obstacle_1_top)
    val sunsetObstacle1Bottom = painterResource(Res.drawable.sunset_obstacle_1_bottom)
    val sunsetObstacle2Top = painterResource(Res.drawable.sunset_obstacle_2_top)
    val sunsetObstacle2Bottom = painterResource(Res.drawable.sunset_obstacle_2_bottom)
    val sunsetObstacle3Top = painterResource(Res.drawable.sunset_obstacle_3_top)
    val sunsetObstacle3Bottom = painterResource(Res.drawable.sunset_obstacle_3_bottom)

    val winterBackground = painterResource(Res.drawable.winter_background)
    val winterBase = painterResource(Res.drawable.winter_base)
    val winterObstacle1Top = painterResource(Res.drawable.winter_obstacle_1_top)
    val winterObstacle1Bottom = painterResource(Res.drawable.winter_obstacle_1_bottom)
    val winterObstacle2Top = painterResource(Res.drawable.winter_obstacle_2_top)
    val winterObstacle2Bottom = painterResource(Res.drawable.winter_obstacle_2_bottom)
    val winterObstacle3Top = painterResource(Res.drawable.winter_obstacle_3_top)
    val winterObstacle3Bottom = painterResource(Res.drawable.winter_obstacle_3_bottom)

    val numbers = listOf(
        painterResource(Res.drawable.num_0),
        painterResource(Res.drawable.num_1),
        painterResource(Res.drawable.num_2),
        painterResource(Res.drawable.num_3),
        painterResource(Res.drawable.num_4),
        painterResource(Res.drawable.num_5),
        painterResource(Res.drawable.num_6),
        painterResource(Res.drawable.num_7),
        painterResource(Res.drawable.num_8),
        painterResource(Res.drawable.num_9)
    )

    val bird1 = painterResource(Res.drawable.bird_1)
    val bird2 = painterResource(Res.drawable.bird_2)
    val bird3 = painterResource(Res.drawable.bird_3)
    val bird4 = painterResource(Res.drawable.bird_4)

    val gameAssets = remember(
        defaultBackground, defaultBase,
        defaultObstacle1Top, defaultObstacle1Bottom, defaultObstacle2Top, defaultObstacle2Bottom, defaultObstacle3Top, defaultObstacle3Bottom,
        sunsetBackground, sunsetBase,
        sunsetObstacle1Top, sunsetObstacle1Bottom, sunsetObstacle2Top, sunsetObstacle2Bottom, sunsetObstacle3Top, sunsetObstacle3Bottom,
        winterBackground, winterBase,
        winterObstacle1Top, winterObstacle1Bottom, winterObstacle2Top, winterObstacle2Bottom, winterObstacle3Top, winterObstacle3Bottom,
        numbers, bird1, bird2, bird3, bird4
    ) {
        GameAssets(
            defaultBackground = defaultBackground,
            defaultBase = defaultBase,
            defaultObstacle1Top = defaultObstacle1Top,
            defaultObstacle1Bottom = defaultObstacle1Bottom,
            defaultObstacle2Top = defaultObstacle2Top,
            defaultObstacle2Bottom = defaultObstacle2Bottom,
            defaultObstacle3Top = defaultObstacle3Top,
            defaultObstacle3Bottom = defaultObstacle3Bottom,
            sunsetBackground = sunsetBackground,
            sunsetBase = sunsetBase,
            sunsetObstacle1Top = sunsetObstacle1Top,
            sunsetObstacle1Bottom = sunsetObstacle1Bottom,
            sunsetObstacle2Top = sunsetObstacle2Top,
            sunsetObstacle2Bottom = sunsetObstacle2Bottom,
            sunsetObstacle3Top = sunsetObstacle3Top,
            sunsetObstacle3Bottom = sunsetObstacle3Bottom,
            winterBackground = winterBackground,
            winterBase = winterBase,
            winterObstacle1Top = winterObstacle1Top,
            winterObstacle1Bottom = winterObstacle1Bottom,
            winterObstacle2Top = winterObstacle2Top,
            winterObstacle2Bottom = winterObstacle2Bottom,
            winterObstacle3Top = winterObstacle3Top,
            winterObstacle3Bottom = winterObstacle3Bottom,
            numbers = numbers,
            bird = listOf(bird1, bird2, bird3, bird4)
        )
    }

    var canvasWidth by remember { mutableStateOf(800f) }
    var canvasHeight by remember { mutableStateOf(600f) }

    val focusRequester = remember { FocusRequester() }

    // Dynamically set system bar colors on mobile platforms
    val systemBarsBg = when (state) {
        GameState.SPLASH -> Color(0xFF1E272C)
        GameState.MENU -> Color(0xFF202C39)
        GameState.GAME_OVER -> Color(0xFF151D24)
        GameState.SETTINGS -> Color(0xFF202C39)
        else -> Color.Transparent
    }
    SetSystemBarsColor(
        statusBarColor = systemBarsBg,
        navigationBarColor = systemBarsBg,
        darkIcons = false
    )

    // Coroutine to drive the game loop updates at VSync rate
    LaunchedEffect(state) {
        if (state == GameState.PLAYING || state == GameState.MENU || state == GameState.SPLASH) {
            focusRequester.requestFocus()
            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    engine.tick(frameTimeNanos)
                }
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalTheme provides settings.theme) {
        PlatformWindowFrame(
            gameState = state,
            onClose = onClose,
            onMinimize = onMinimize,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Spacebar -> {
                                if (state == GameState.PLAYING) {
                                    engine.flap()
                                    true
                                } else if (state == GameState.MENU) {
                                    engine.startGame()
                                    true
                                } else if (state == GameState.GAME_OVER) {
                                    engine.startGame()
                                    true
                                } else {
                                    false
                                }
                            }
                            Key.Escape -> {
                                if (state == GameState.PLAYING) {
                                    engine.pauseGame()
                                    true
                                } else if (state == GameState.PAUSED) {
                                    engine.resumeGame()
                                    true
                                } else if (state == GameState.SETTINGS) {
                                    engine.transitionToMenu()
                                    true
                                } else {
                                    false
                                }
                            }
                            Key.F11 -> {
                                onToggleFullscreen()
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
                .pointerInput(state) {
                    detectTapGestures {
                        if (state == GameState.PLAYING) {
                            engine.flap()
                        }
                    }
                }
        ) {
            // Draw the full gameplay screen (it is always scrolling behind overlays!)
            val isPlaying = state == GameState.PLAYING
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isPlaying) Modifier else Modifier.blur(12.dp))
                    .onSizeChanged { size ->
                        canvasWidth = size.width.toFloat()
                        canvasHeight = size.height.toFloat()
                    }
            ) {
                val now = Clock.System.now().toEpochMilliseconds() * 1_000_000L
                GameRenderer.drawGame(
                    drawScope = this,
                    birdY = birdY,
                    rotation = birdRotation,
                    obstacles = obstacles,
                    particles = engine.particles,
                    score = score,
                    distance = distance,
                    biome = biome,
                    weather = weather,
                    settings = settings,
                    assets = gameAssets,
                    width = canvasWidth,
                    height = canvasHeight,
                    timeNanos = now
                )
            }

            // Overlay screens
            when (state) {
                GameState.SPLASH -> {
                    SplashScreenContent()
                }
                GameState.MENU -> {
                    MainMenuScreenContent(
                        engine = engine,
                        onPlay = { engine.startGame() },
                        onSettings = { engine.openSettings() }
                    )
                }
                GameState.PLAYING -> {
                    GameplayHUD(
                        theme = settings.theme,
                        highScore = userProfile.highScore,
                        onPause = { engine.pauseGame() }
                    )
                }
                GameState.PAUSED -> {
                    GameplayHUD(
                        theme = settings.theme,
                        highScore = userProfile.highScore,
                        onPause = {}
                    )
                    PauseOverlay(
                        onResume = { engine.resumeGame() },
                        onRestart = { engine.startGame() },
                        onMainMenu = { engine.transitionToMenu() }
                    )
                }
                GameState.GAME_OVER -> {
                    GameOverScreenContent(
                        score = score,
                        bestScore = bestScore.highScore,
                        distance = distance,
                        obstaclesPassed = obstaclesPassed,
                        onRetry = { engine.startGame() },
                        onMainMenu = { engine.transitionToMenu() }
                    )
                }
                GameState.SETTINGS -> {
                    SettingsScreenContent(
                        settings = settings,
                        onSaveVolumes = { m, e -> engine.saveVolumes(m, e) },
                        onSelectTheme = { engine.selectTheme(it) },
                        onSelectGraphics = { engine.selectGraphics(it) },
                        onBack = { engine.transitionToMenu() }
                    )
                }
            }
        }
    }
}
}
