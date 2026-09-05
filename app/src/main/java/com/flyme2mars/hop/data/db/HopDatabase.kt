package com.flyme2mars.hop.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HopDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        fun create(context: Context): HopDatabase =
            Room.databaseBuilder(context.applicationContext, HopDatabase::class.java, "hop.db")
                .build()
    }
}
