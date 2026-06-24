package com.maheswara660.flyingbird.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.maheswara660.flyingbird.game.GameState

@Composable
expect fun SetSystemBarsColor(statusBarColor: Color, navigationBarColor: Color, darkIcons: Boolean)

@Composable
expect fun PlatformWindowFrame(
    gameState: GameState,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    content: @Composable () -> Unit
)

val LocalTheme = androidx.compose.runtime.staticCompositionLocalOf { "default" }

expect val isDesktop: Boolean
