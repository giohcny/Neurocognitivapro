package com.neurocognitiva.pro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PatientEntity::class, SessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun sessionDao(): SessionDao

    companion object {
        // Importante: no se usa fallbackToDestructiveMigration() para no perder datos clínicos.
        // Al cambiar el esquema, incrementa "version" y agrega una Migration explícita.
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "neurocognitiva.db").build()
    }
}
