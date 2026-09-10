package com.eon.futuresimulator.data.database

import androidx.room.migration.Migration

/**
 * Real migrations live here as the schema evolves — e.g.:
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE goals ADD COLUMN colorTag TEXT NOT NULL DEFAULT ''")
 *     }
 * }
 *
 * Keep every migration in [ALL_MIGRATIONS] and add a matching
 * MigrationTestHelper test (see androidTest/DatabaseMigrationTest.kt).
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf()
