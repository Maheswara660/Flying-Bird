package com.maheswara660.flyingbird.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import com.maheswara660.flyingbird.data.DatabaseHelper
import com.maheswara660.flyingbird.data.GameRepositoryImpl
import com.maheswara660.flyingbird.domain.GameRepository
import com.maheswara660.flyingbird.game.GameEngine
import org.koin.core.Koin

private var koinInstance: Koin? = null

val commonModule = module {
    single<GameRepository> { GameRepositoryImpl(get()) }
    single { DatabaseHelper(get()) }
}

fun initKoin(platformModule: Module) {
    val koinApp = startKoin {
        modules(commonModule, platformModule)
    }
    koinInstance = koinApp.koin
}

fun getKoin(): Koin {
    return koinInstance ?: error("Koin is not initialized")
}

fun getGameEngine(): GameEngine {
    return getKoin().get()
}

fun isKoinInitialized(): Boolean {
    return koinInstance != null
}
