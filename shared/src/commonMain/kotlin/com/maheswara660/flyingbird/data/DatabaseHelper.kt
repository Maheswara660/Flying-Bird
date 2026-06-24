package com.maheswara660.flyingbird.data

import com.maheswara660.flyingbird.db.AppDatabase
import app.cash.sqldelight.db.SqlDriver

class DatabaseHelper(sqlDriver: SqlDriver) {
    init {
        try {
            sqlDriver.execute(null, "ALTER TABLE Settings ADD COLUMN birdColor TEXT NOT NULL DEFAULT 'yellow'", 0)
        } catch (e: Exception) {
            // Already exists or fails, ignore
        }
    }
    val database = AppDatabase(sqlDriver)
}
