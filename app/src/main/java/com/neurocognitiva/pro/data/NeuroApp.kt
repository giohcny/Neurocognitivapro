package com.neurocognitiva.pro

import android.app.Application
import com.neurocognitiva.pro.data.AppDatabase
import com.neurocognitiva.pro.data.Repository
import com.neurocognitiva.pro.data.SettingsStore

class NeuroApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val repository: Repository by lazy { Repository(database) }
    val settings: SettingsStore by lazy { SettingsStore(this) }
}
