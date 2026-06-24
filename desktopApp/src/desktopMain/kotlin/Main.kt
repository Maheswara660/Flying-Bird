package com.maheswara660.flyingbird.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import java.lang.reflect.Proxy
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import com.maheswara660.flyingbird.audio.AudioPlayer
import com.maheswara660.flyingbird.audio.DesktopAudioPlayer
import com.maheswara660.flyingbird.db.AppDatabase
import com.maheswara660.flyingbird.di.initKoin
import com.maheswara660.flyingbird.game.GameEngine
import com.maheswara660.flyingbird.presentation.App
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun main() = application {
    val platformModule = module {
        single<SqlDriver> {
            val databaseFile = File(System.getProperty("user.home"), ".flyingbird.db")
            val createSchema = !databaseFile.exists()
            val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
            if (createSchema) {
                AppDatabase.Schema.create(driver)
            }
            driver
        }
        single<AudioPlayer> { DesktopAudioPlayer() }
        single { GameEngine(get(), get(), CoroutineScope(Dispatchers.Main + SupervisorJob())) }
    }

    // Initialize DI
    initKoin(platformModule)

    val engine = GlobalContext.get().get<GameEngine>()

    val windowState = rememberWindowState(
        size = DpSize(800.dp, 600.dp),
        position = WindowPosition(Alignment.Center)
    )

    val osName = remember { System.getProperty("os.name").lowercase() }
    val isMac = remember { osName.contains("mac") }
    var isFullscreen by remember { mutableStateOf(false) }
    var savedSize by remember { mutableStateOf(DpSize(800.dp, 600.dp)) }
    var savedPosition by remember { mutableStateOf<WindowPosition>(WindowPosition(Alignment.Center)) }
    val alwaysOnTopValue = if (isMac) false else isFullscreen

    Window(
        onCloseRequest = ::exitApplication,
        title = "Flying Bird - Survive The Fallen World",
        state = windowState,
        icon = painterResource("AppIcon.png"),
        undecorated = true,
        resizable = true,
        alwaysOnTop = alwaysOnTopValue
    ) {
        val windowInstance = this.window

        LaunchedEffect(windowInstance) {
            if (isMac) {
                MacFullScreenHelper.enableFullscreen(windowInstance) { state ->
                    isFullscreen = state
                }
            }
        }

        App(
            engine = engine,
            onClose = ::exitApplication,
            onMinimize = { windowState.isMinimized = true },
            isFullscreen = isFullscreen,
            onToggleFullscreen = {
                if (isMac) {
                    val success = MacFullScreenHelper.toggleFullscreen(windowInstance)
                    if (!success) {
                        // Fallback to manual borderless if native macOS call fails
                        if (isFullscreen) {
                            isFullscreen = false
                            windowState.position = savedPosition
                            windowState.size = savedSize
                        } else {
                            val config = windowInstance.graphicsConfiguration
                            val bounds = config?.bounds
                            if (bounds != null) {
                                savedSize = windowState.size
                                savedPosition = windowState.position
                                
                                isFullscreen = true
                                windowState.position = WindowPosition(bounds.x.dp, bounds.y.dp)
                                windowState.size = DpSize(bounds.width.dp, bounds.height.dp)
                            }
                        }
                    }
                } else {
                    // Windows & Linux: Borderless fullscreen via manual bounds resizing
                    if (isFullscreen) {
                        isFullscreen = false
                        windowState.position = savedPosition
                        windowState.size = savedSize
                    } else {
                        val config = windowInstance.graphicsConfiguration
                        val bounds = config?.bounds
                        if (bounds != null) {
                            savedSize = windowState.size
                            savedPosition = windowState.position
                            
                            isFullscreen = true
                            windowState.position = WindowPosition(bounds.x.dp, bounds.y.dp)
                            windowState.size = DpSize(bounds.width.dp, bounds.height.dp)
                        }
                    }
                }
            }
        )
    }
}

object MacFullScreenHelper {
    fun enableFullscreen(window: java.awt.Window, onStateChange: (Boolean) -> Unit) {
        try {
            // 1. Mark window as fullscreenable using client property on RootPane
            if (window is javax.swing.JFrame) {
                window.rootPane.putClientProperty("apple.awt.fullscreenable", true)
            }
            
            // 2. Mark window as capable of fullscreen via FullScreenUtilities
            val utilClass = Class.forName("com.apple.eawt.FullScreenUtilities")
            val setCanFullScreen = utilClass.getMethod("setWindowCanFullScreen", java.awt.Window::class.java, Boolean::class.javaPrimitiveType)
            setCanFullScreen.invoke(null, window, true)
            
            // 3. Register FullScreenListener via reflection & dynamic proxy to update Kotlin state
            val listenerClass = Class.forName("com.apple.eawt.FullScreenListener")
            val proxyInstance = Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass),
                object : InvocationHandler {
                    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
                        when (method?.name) {
                            "windowEnteredFullScreen" -> onStateChange(true)
                            "windowExitedFullScreen" -> onStateChange(false)
                        }
                        return null
                    }
                }
            )
            
            val addListenerMethod = utilClass.getMethod("addFullScreenListener", java.awt.Window::class.java, listenerClass)
            addListenerMethod.invoke(null, window, proxyInstance)
        } catch (e: Throwable) {
            // Ignore if not supported on this platform
        }
    }

    fun toggleFullscreen(window: java.awt.Window): Boolean {
        try {
            val appClass = Class.forName("com.apple.eawt.Application")
            val getApp = appClass.getMethod("getApplication")
            val appInstance = getApp.invoke(null)
            val requestToggle = appClass.getMethod("requestToggleFullScreen", java.awt.Window::class.java)
            requestToggle.invoke(appInstance, window)
            return true
        } catch (e: Throwable) {
            return false
        }
    }
}
