package com.flyme2mars.hop

import android.app.Application
import com.flyme2mars.hop.data.HopSettingsStore
import com.flyme2mars.hop.data.RoomHopRepository
import com.flyme2mars.hop.data.db.HopDatabase

class HopApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    val settings = HopSettingsStore(application)
    val database: HopDatabase = HopDatabase.create(application)
    val repository: RoomHopRepository = RoomHopRepository(database.postDao(), settings)
}
