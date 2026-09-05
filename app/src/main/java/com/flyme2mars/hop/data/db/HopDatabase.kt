package com.flyme2mars.hop.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PostEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class HopDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE posts ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE posts SET updatedAtMillis = createdAtMillis WHERE updatedAtMillis = 0")
            }
        }

        fun create(context: Context): HopDatabase =
            Room.databaseBuilder(context.applicationContext, HopDatabase::class.java, "hop.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
