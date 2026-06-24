package com.maheswara660.flyingbird.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.maheswara660.flyingbird.audio.AudioPlayer
import com.maheswara660.flyingbird.audio.AndroidAudioPlayer
import com.maheswara660.flyingbird.db.AppDatabase
import com.maheswara660.flyingbird.di.initKoin
import com.maheswara660.flyingbird.game.GameEngine
import com.maheswara660.flyingbird.presentation.App
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (GlobalContext.getOrNull() == null) {
            val platformModule = module {
                single<SqlDriver> {
                    AndroidSqliteDriver(AppDatabase.Schema, applicationContext, "flyingbird.db")
                }
                single<AudioPlayer> {
                    AndroidAudioPlayer(applicationContext)
                }
                single {
                    GameEngine(get(), get(), CoroutineScope(Dispatchers.Main + SupervisorJob()))
                }
            }
            initKoin(platformModule)
        }

        val engine = GlobalContext.get().get<GameEngine>()

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        setContent {
            App(engine)
        }
    }
}
