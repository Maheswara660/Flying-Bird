package com.maheswara660.flyingbird.physics

data class AABB(val x: Float, val y: Float, val width: Float, val height: Float) {
    fun intersects(other: AABB): Boolean {
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y
    }
}

object PhysicsEngine {
    const val LOGICAL_WIDTH = 800f
    const val LOGICAL_HEIGHT = 600f
    const val GROUND_HEIGHT = 80f
    const val GROUND_Y = LOGICAL_HEIGHT - GROUND_HEIGHT
    const val CEILING_Y = 0f

    const val BIRD_WIDTH = 56f
    const val BIRD_HEIGHT = 46f
    const val BIRD_X = LOGICAL_WIDTH * 0.35f

    const val OBSTACLE_WIDTH = 80f

    fun updateBird(
        posY: Float,
        velocity: Float,
        gravity: Float,
        maxFallSpeed: Float,
        dt: Float // delta time in seconds
    ): Pair<Float, Float> {
        val cappedDt = dt.coerceAtMost(0.1f)
        
        // standard acceleration formula
        val newVelocity = (velocity + gravity * 60f * cappedDt).coerceAtMost(maxFallSpeed)
        val newPosY = posY + newVelocity * cappedDt

        return Pair(newPosY, newVelocity)
    }

    fun getBirdRotation(velocity: Float, rotationMin: Float, rotationMax: Float): Float {
        return if (velocity < 0) {
            val ratio = (velocity / -220f).coerceIn(0f, 1f)
            ratio * rotationMin
        } else {
            val ratio = (velocity / 250f).coerceIn(0f, 1f)
            ratio * rotationMax
        }
    }

    fun checkCollision(
        birdY: Float,
        obstacles: List<ObstacleInstance>
    ): Boolean {
        val birdBox = AABB(BIRD_X, birdY, BIRD_WIDTH, BIRD_HEIGHT)

        if (birdY + BIRD_HEIGHT >= GROUND_Y) {
            println("COLLISION: hit ground! birdY=$birdY, groundY=$GROUND_Y, birdHeight=$BIRD_HEIGHT")
            return true
        }
        if (birdY <= CEILING_Y) {
            println("COLLISION: hit ceiling! birdY=$birdY, ceilingY=$CEILING_Y")
            return true
        }

        for (obs in obstacles) {
            val topBox = AABB(obs.x, 0f, OBSTACLE_WIDTH, obs.topHeight)
            val bottomY = obs.topHeight + obs.gapSize
            val bottomHeight = GROUND_Y - bottomY
            val bottomBox = AABB(obs.x + obs.bottomXOffset, bottomY, OBSTACLE_WIDTH, bottomHeight)

            if (birdBox.intersects(topBox)) {
                println("COLLISION: hit top box! birdY=$birdY, obs=$obs")
                return true
            }
            if (birdBox.intersects(bottomBox)) {
                println("COLLISION: hit bottom box! birdY=$birdY, obs=$obs")
                return true
            }
        }

        return false
    }
}

data class ObstacleInstance(
    val id: String,
    val type: ObstacleType,
    var x: Float,
    val topHeight: Float,
    val gapSize: Float,
    val bottomXOffset: Float = 0f,
    val imageIndex: Int = 0,
    var passed: Boolean = false
)

enum class ObstacleType {
    PIPE,
    TOWER,
    POLE,
    MAST,
    CHIMNEY
}
