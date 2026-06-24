package com.maheswara660.flyingbird.presentation

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.painter.Painter
import com.maheswara660.flyingbird.game.GameEngine
import com.maheswara660.flyingbird.game.Particle
import com.maheswara660.flyingbird.physics.ObstacleInstance
import com.maheswara660.flyingbird.physics.ObstacleType
import com.maheswara660.flyingbird.physics.PhysicsEngine
import com.maheswara660.flyingbird.domain.Settings
import kotlin.math.cos
import kotlin.math.sin

data class GameAssets(
    val defaultBackground: Painter,
    val defaultBase: Painter,
    val defaultObstacle1Top: Painter,
    val defaultObstacle1Bottom: Painter,
    val defaultObstacle2Top: Painter,
    val defaultObstacle2Bottom: Painter,
    val defaultObstacle3Top: Painter,
    val defaultObstacle3Bottom: Painter,
    
    val sunsetBackground: Painter,
    val sunsetBase: Painter,
    val sunsetObstacle1Top: Painter,
    val sunsetObstacle1Bottom: Painter,
    val sunsetObstacle2Top: Painter,
    val sunsetObstacle2Bottom: Painter,
    val sunsetObstacle3Top: Painter,
    val sunsetObstacle3Bottom: Painter,
    
    val winterBackground: Painter,
    val winterBase: Painter,
    val winterObstacle1Top: Painter,
    val winterObstacle1Bottom: Painter,
    val winterObstacle2Top: Painter,
    val winterObstacle2Bottom: Painter,
    val winterObstacle3Top: Painter,
    val winterObstacle3Bottom: Painter,

    val numbers: List<Painter>,
    val bird: List<Painter>
)

object GameRenderer {
    private val gameElementFilter = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.25f, 0f, 0f, 0f, 15f,
        0f, 1.25f, 0f, 0f, 15f,
        0f, 0f, 1.25f, 0f, 15f,
        0f, 0f, 0f, 1f, 0f
    )))

    fun drawGame(
        drawScope: DrawScope,
        birdY: Float,
        rotation: Float,
        obstacles: List<ObstacleInstance>,
        particles: List<Particle>,
        score: Int,
        distance: Float,
        biome: String,
        weather: String,
        settings: Settings,
        assets: GameAssets,
        width: Float,
        height: Float,
        timeNanos: Long
    ) {
        val theme = settings.theme // "default", "sunset", "winter"

        // Calculate scale factors from logical size (800x600) to actual canvas size
        val scaleX = width / PhysicsEngine.LOGICAL_WIDTH
        val scaleY = height / PhysicsEngine.LOGICAL_HEIGHT
        
        drawScope.drawIntoCanvas {
            drawScope.scale(scaleX, scaleY, Offset.Zero) {
                // 1. Draw Background
                drawSky(this, assets, theme, weather)

                // 2. Draw Dim Background Overlay
                drawScope.drawRect(
                    color = Color.Black.copy(alpha = 0.35f),
                    size = Size(PhysicsEngine.LOGICAL_WIDTH, PhysicsEngine.LOGICAL_HEIGHT)
                )

                // 3. Draw Weather Particles
                drawWeatherParticles(this, particles, theme)

                // 4. Draw Obstacles
                drawObstacles(this, assets, obstacles, settings, timeNanos)

                // 5. Draw Ground
                drawGround(this, assets, distance, theme, biome, settings)

                // 6. Draw Player Bird
                drawBird(this, assets, birdY, rotation, settings, timeNanos)

                // 7. Draw Score using number assets
                drawScore(this, assets, score)
            }
        }
    }

    private fun drawSky(drawScope: DrawScope, assets: GameAssets, theme: String, weather: String) {
        val painter = when (theme) {
            "sunset" -> assets.sunsetBackground
            "winter" -> assets.winterBackground
            else -> assets.defaultBackground
        }
        with(painter) {
            drawScope.draw(size = Size(PhysicsEngine.LOGICAL_WIDTH, PhysicsEngine.LOGICAL_HEIGHT))
        }
    }

    private fun drawWeatherParticles(
        drawScope: DrawScope,
        particles: List<Particle>,
        theme: String
    ) {
        for (p in particles) {
            when (theme) {
                "sunset" -> {
                    // Fire ashes / embers: Orange/Red glowing circles
                    drawScope.drawCircle(
                        color = Color(0xFFFF5722).copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(p.x, p.y)
                    )
                }
                "winter" -> {
                    // Winter snowflakes: White circles
                    drawScope.drawCircle(
                        color = Color.White.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(p.x, p.y)
                    )
                }
                else -> {
                    // Rain drops: Slanted blue/white lines pointing down/left
                    drawScope.drawLine(
                        color = Color(0xFFE0F7FA).copy(alpha = p.alpha),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + p.vx * 0.04f, p.y + p.vy * 0.04f),
                        strokeWidth = p.size
                    )
                }
            }
        }
    }

    private fun drawObstacles(
        drawScope: DrawScope,
        assets: GameAssets,
        obstacles: List<ObstacleInstance>,
        settings: Settings,
        timeNanos: Long
    ) {
        for (obs in obstacles) {
            drawSingleObstacle(drawScope, assets, obs, true, settings, timeNanos)
            drawSingleObstacle(drawScope, assets, obs, false, settings, timeNanos)
        }
    }

    private fun drawSingleObstacle(
        drawScope: DrawScope,
        assets: GameAssets,
        obs: ObstacleInstance,
        isTop: Boolean,
        settings: Settings,
        timeNanos: Long
    ) {
        val x = if (isTop) obs.x else obs.x + obs.bottomXOffset
        val width = PhysicsEngine.OBSTACLE_WIDTH
        val height = if (isTop) obs.topHeight else PhysicsEngine.LOGICAL_HEIGHT - (obs.topHeight + obs.gapSize)
        val y = if (isTop) 0f else obs.topHeight + obs.gapSize

        if (settings.highContrast) {
            drawScope.drawRect(
                color = Color.Black,
                topLeft = Offset(x, y),
                size = Size(width, height)
            )
            drawScope.drawRect(
                color = Color.Red,
                topLeft = Offset(x + 2f, y + 2f),
                size = Size(width - 4f, height - 4f),
                style = Stroke(3f)
            )
            return
        }

        val pipePainter = when (settings.theme) {
            "sunset" -> when (obs.imageIndex) {
                0 -> if (isTop) assets.sunsetObstacle1Top else assets.sunsetObstacle1Bottom
                1 -> if (isTop) assets.sunsetObstacle2Top else assets.sunsetObstacle2Bottom
                else -> if (isTop) assets.sunsetObstacle3Top else assets.sunsetObstacle3Bottom
            }
            "winter" -> when (obs.imageIndex) {
                0 -> if (isTop) assets.winterObstacle1Top else assets.winterObstacle1Bottom
                1 -> if (isTop) assets.winterObstacle2Top else assets.winterObstacle2Bottom
                else -> if (isTop) assets.winterObstacle3Top else assets.winterObstacle3Bottom
            }
            else -> when (obs.imageIndex) {
                0 -> if (isTop) assets.defaultObstacle1Top else assets.defaultObstacle1Bottom
                1 -> if (isTop) assets.defaultObstacle2Top else assets.defaultObstacle2Bottom
                else -> if (isTop) assets.defaultObstacle3Top else assets.defaultObstacle3Bottom
            }
        }

        drawScope.withTransform({
            translate(left = x, top = y)
        }) {
            with(pipePainter) {
                draw(size = Size(width, height), colorFilter = gameElementFilter)
            }
        }

        if (settings.colorBlind) {
            val stripePath = Path()
            var stripeY = y
            val spacing = 20f
            while (stripeY < y + height) {
                stripePath.moveTo(x, stripeY)
                stripePath.lineTo(x + width, stripeY + width)
                stripeY += spacing
            }
            drawScope.drawPath(
                path = stripePath,
                color = Color(0x33FFFFFF),
                style = Stroke(3f)
            )
        }
    }

    private fun drawGround(
        drawScope: DrawScope,
        assets: GameAssets,
        distance: Float,
        theme: String,
        biome: String,
        settings: Settings
    ) {
        val groundY = PhysicsEngine.GROUND_Y
        val groundHeight = PhysicsEngine.GROUND_HEIGHT

        if (settings.highContrast) {
            drawScope.drawRect(
                color = Color.Black,
                topLeft = Offset(0f, groundY),
                size = Size(PhysicsEngine.LOGICAL_WIDTH, groundHeight)
            )
            drawScope.drawRect(
                color = Color.White,
                topLeft = Offset(0f, groundY + 2f),
                size = Size(PhysicsEngine.LOGICAL_WIDTH, 4f)
            )
            return
        }

        val basePainter = when (theme) {
            "sunset" -> assets.sunsetBase
            "winter" -> assets.winterBase
            else -> assets.defaultBase
        }

        val baseWidth = 336f
        val scrollOffset = if (settings.reducedMotion) 0f else -(distance * 50f) % baseWidth
        var x = scrollOffset
        while (x < PhysicsEngine.LOGICAL_WIDTH + baseWidth) {
            drawScope.withTransform({
                translate(left = x, top = groundY)
            }) {
                with(basePainter) {
                    draw(size = Size(baseWidth + 2f, groundHeight), colorFilter = gameElementFilter)
                }
            }
            x += baseWidth
        }
    }

    private fun drawBird(
        drawScope: DrawScope,
        assets: GameAssets,
        birdY: Float,
        rotation: Float,
        settings: Settings,
        timeNanos: Long
    ) {
        val cx = PhysicsEngine.BIRD_X + PhysicsEngine.BIRD_WIDTH / 2f
        val cy = birdY + PhysicsEngine.BIRD_HEIGHT / 2f

        drawScope.withTransform({
            translate(left = PhysicsEngine.BIRD_X, top = birdY)
            rotate(degrees = rotation, pivot = Offset(PhysicsEngine.BIRD_WIDTH / 2f, PhysicsEngine.BIRD_HEIGHT / 2f))
        }) {
            if (settings.highContrast) {
                drawScope.drawOval(
                    color = Color.Black,
                    topLeft = Offset(-2f, -2f),
                    size = Size(PhysicsEngine.BIRD_WIDTH + 4f, PhysicsEngine.BIRD_HEIGHT + 4f)
                )
                drawScope.drawOval(
                    color = Color(0xFF53D8FB),
                    topLeft = Offset.Zero,
                    size = Size(PhysicsEngine.BIRD_WIDTH, PhysicsEngine.BIRD_HEIGHT)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(PhysicsEngine.BIRD_WIDTH / 2f + 10f, PhysicsEngine.BIRD_HEIGHT / 2f - 4f)
                )
                drawScope.drawCircle(
                    color = Color.Black,
                    radius = 3f,
                    center = Offset(PhysicsEngine.BIRD_WIDTH / 2f + 11f, PhysicsEngine.BIRD_HEIGHT / 2f - 4f)
                )
                return@withTransform
            }

            val flapState = (timeNanos / 100_000_000) % 6
            val flapIndex = when (flapState) {
                0L -> 0
                1L -> 1
                2L -> 2
                3L -> 3
                4L -> 1
                else -> 0
            }
            val birdPainter = assets.bird[flapIndex]

            with(birdPainter) {
                draw(
                    size = Size(PhysicsEngine.BIRD_WIDTH, PhysicsEngine.BIRD_HEIGHT),
                    colorFilter = gameElementFilter
                )
            }
        }
    }

    private fun drawScore(drawScope: DrawScope, assets: GameAssets, score: Int) {
        val scoreStr = score.toString()
        val numDigits = scoreStr.length
        
        val digitWidth = 24f
        val digitHeight = 36f
        val spacing = 2f
        
        val totalWidth = numDigits * digitWidth + (numDigits - 1) * spacing
        var startX = (PhysicsEngine.LOGICAL_WIDTH - totalWidth) / 2f
        val startY = 40f
        
        for (char in scoreStr) {
            val digit = char - '0'
            if (digit in 0..9) {
                val painter = assets.numbers[digit]
                drawScope.withTransform({
                    translate(left = startX, top = startY)
                }) {
                    with(painter) {
                        draw(size = Size(digitWidth, digitHeight), colorFilter = gameElementFilter)
                    }
                }
            }
            startX += digitWidth + spacing
        }
    }
}
