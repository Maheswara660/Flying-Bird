package com.maheswara660.flyingbird

import com.maheswara660.flyingbird.physics.PhysicsEngine
import com.maheswara660.flyingbird.physics.ObstacleInstance
import com.maheswara660.flyingbird.physics.ObstacleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PhysicsEngineTest {

    @Test
    fun testUpdateBird() {
        val startY = 300f
        val startVelocity = 0f
        val gravity = 8.5f
        val maxFall = 450f
        val dt = 1f / 60f // standard frame delta time (0.0166s)
        
        val (newY, newVelocity) = PhysicsEngine.updateBird(startY, startVelocity, gravity, maxFall, dt)
        
        // velocity increases: newVelocity = 0 + 8.5 * 60 * (1/60) = 8.5
        assertEquals(8.5f, newVelocity)
        // position increases: newY = 300 + 8.5 * (1/60) = 300.1416f
        assertTrue(newY > startY)
    }

    @Test
    fun testCollisionWithGround() {
        val collides = PhysicsEngine.checkCollision(490f, emptyList())
        assertTrue(collides, "Bird should collide with ground when at Y=490")
    }

    @Test
    fun testCollisionWithCeiling() {
        val collides = PhysicsEngine.checkCollision(-5f, emptyList())
        assertTrue(collides, "Bird should collide with ceiling when at Y=-5")
    }

    @Test
    fun testNoCollision() {
        val collides = PhysicsEngine.checkCollision(250f, emptyList())
        assertFalse(collides, "Bird should not collide when at safe position with no obstacles")
    }

    @Test
    fun testCollisionWithObstacle() {
        val obstacles = listOf(
            ObstacleInstance(
                id = "test_obs",
                type = ObstacleType.PIPE,
                x = 280f,
                topHeight = 200f,
                gapSize = 150f
            )
        )
        
        val collideTop = PhysicsEngine.checkCollision(100f, obstacles)
        assertTrue(collideTop, "Bird at Y=100 should collide with top obstacle")
        
        val collideBottom = PhysicsEngine.checkCollision(400f, obstacles)
        assertTrue(collideBottom, "Bird at Y=400 should collide with bottom obstacle")

        val collideGap = PhysicsEngine.checkCollision(250f, obstacles)
        assertFalse(collideGap, "Bird at Y=250 should be safe inside the obstacle gap")
    }
}
