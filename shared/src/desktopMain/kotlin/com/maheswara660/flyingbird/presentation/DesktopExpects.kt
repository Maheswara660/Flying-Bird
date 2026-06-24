@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.maheswara660.flyingbird.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.LocalWindow
import com.maheswara660.flyingbird.game.GameState
import java.awt.MouseInfo
import java.awt.Point

@Composable
fun Modifier.windowDrag(enabled: Boolean = true): Modifier {
    if (!enabled) return this
    val window = LocalWindow.current ?: return this
    var dragStartScreenPoint by remember { mutableStateOf<Point?>(null) }
    var dragStartWindowLocation by remember { mutableStateOf<Point?>(null) }
    
    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                val mouseLoc = MouseInfo.getPointerInfo().location
                dragStartScreenPoint = mouseLoc
                dragStartWindowLocation = window.location
            },
            onDrag = { change, _ ->
                change.consume()
                val startScreen = dragStartScreenPoint
                val startWindow = dragStartWindowLocation
                if (startScreen != null && startWindow != null) {
                    val currentScreen = MouseInfo.getPointerInfo().location
                    val deltaX = currentScreen.x - startScreen.x
                    val deltaY = currentScreen.y - startScreen.y
                    window.setLocation(startWindow.x + deltaX, startWindow.y + deltaY)
                }
            },
            onDragEnd = {
                dragStartScreenPoint = null
                dragStartWindowLocation = null
            },
            onDragCancel = {
                dragStartScreenPoint = null
                dragStartWindowLocation = null
            }
        )
    }
}

@Composable
actual fun SetSystemBarsColor(statusBarColor: Color, navigationBarColor: Color, darkIcons: Boolean) {
    // No-op for desktop
}

@Composable
actual fun PlatformWindowFrame(
    gameState: GameState,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentTheme = LocalTheme.current

    val frameBg = when (gameState) {
        GameState.SPLASH -> Color(0xFF1E272C)
        GameState.GAME_OVER -> Color(0xFF151D24)
        GameState.MENU -> when (currentTheme) {
            "sunset" -> Color(0xDD2A1414)
            "winter" -> Color(0xDD0F1F2C)
            else -> Color(0xCC202C39)
        }
        else -> Color(0xFF151D24)
    }

    val borderColor = when (gameState) {
        GameState.GAME_OVER -> BrandRed.copy(0.7f)
        else -> when (currentTheme) {
            "sunset" -> Color(0xFFE67E22).copy(alpha = 0.5f)
            "winter" -> Color(0xFF81D4FA).copy(alpha = 0.5f)
            else -> BrandAmber.copy(alpha = 0.4f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isFullscreen) Modifier else Modifier.border(1.5.dp, borderColor))
    ) {
        if (!isFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(frameBg)
                    .windowDrag(enabled = !isFullscreen)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val themeAccentColor = when (currentTheme) {
                        "sunset" -> Color(0xFFE67E22)
                        "winter" -> Color(0xFF81D4FA)
                        else -> BrandCyan
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (gameState == GameState.GAME_OVER) BrandRed else themeAccentColor, RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FLYING BIRD - SURVIVAL TERMINAL",
                        color = TextLight.copy(0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Fullscreen button
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                            .border(0.5.dp, TextLight.copy(0.2f), RoundedCornerShape(4.dp))
                            .clickable { onToggleFullscreen() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFullscreen) "❐" else "⛶",
                            color = TextLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Minimize button
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                            .border(0.5.dp, TextLight.copy(0.2f), RoundedCornerShape(4.dp))
                            .clickable { onMinimize() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "—", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Maximize button
                    val window = LocalWindow.current
                    val isMaximized = (window as? java.awt.Frame)?.extendedState?.let { (it and java.awt.Frame.MAXIMIZED_BOTH) != 0 } == true
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                            .border(0.5.dp, TextLight.copy(0.2f), RoundedCornerShape(4.dp))
                            .clickable {
                                if (window is java.awt.Frame) {
                                    val state = window.extendedState
                                    if ((state and java.awt.Frame.MAXIMIZED_BOTH) != 0) {
                                        window.extendedState = java.awt.Frame.NORMAL
                                    } else {
                                        window.extendedState = java.awt.Frame.MAXIMIZED_BOTH
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isMaximized) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .border(1.dp, TextLight)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .border(1.dp, TextLight)
                            )
                        }
                    }
                    
                    // Close button
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(if (gameState == GameState.GAME_OVER) BrandRed.copy(0.3f) else Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                            .border(0.5.dp, if (gameState == GameState.GAME_OVER) BrandRed else TextLight.copy(0.2f), RoundedCornerShape(4.dp))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", color = if (gameState == GameState.GAME_OVER) BrandRed else TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor.copy(0.3f))
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

actual val isDesktop: Boolean = true
