package com.maheswara660.flyingbird.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.maheswara660.flyingbird.game.GameState

@Composable
actual fun SetSystemBarsColor(statusBarColor: Color, navigationBarColor: Color, darkIcons: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (context as? Activity)?.window
        if (window != null) {
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
            
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = darkIcons
            controller.isAppearanceLightNavigationBars = darkIcons
        }
    }
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
    content()
}

actual val isDesktop: Boolean = false
