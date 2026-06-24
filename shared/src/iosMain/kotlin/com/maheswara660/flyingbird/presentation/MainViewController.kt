package com.maheswara660.flyingbird.presentation

import androidx.compose.ui.window.ComposeUIViewController
import com.maheswara660.flyingbird.audio.AudioPlayer
import com.maheswara660.flyingbird.audio.IosAudioPlayer
import com.maheswara660.flyingbird.db.AppDatabase
import com.maheswara660.flyingbird.di.initKoin
import com.maheswara660.flyingbird.di.isKoinInitialized
import com.maheswara660.flyingbird.di.getGameEngine
import com.maheswara660.flyingbird.game.GameEngine
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.dsl.module
import platform.UIKit.UIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun MainViewController(): UIViewController {
    if (!isKoinInitialized()) {
        val platformModule = module {
            single<SqlDriver> {
                NativeSqliteDriver(AppDatabase.Schema, "flyingbird.db")
            }
            single<AudioPlayer> {
                IosAudioPlayer()
            }
            single {
                GameEngine(get(), get(), CoroutineScope(Dispatchers.Main + SupervisorJob()))
            }
        }
        initKoin(platformModule)
    }
    
    val engine = getGameEngine()
    return ComposeUIViewController {
        App(engine)
    }
}
