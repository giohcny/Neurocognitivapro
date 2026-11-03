package com.neurocognitiva.pro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun get(id: Long): PatientEntity?

    @Insert
    suspend fun insert(patient: PatientEntity): Long

    @Update
    suspend fun update(patient: PatientEntity)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE patientId = :patientId ORDER BY timestamp ASC")
    fun observeByPatient(patientId: Long): Flow<List<SessionEntity>>

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Insert
    suspend fun insertAll(sessions: List<SessionEntity>)

    @Query("DELETE FROM sessions WHERE patientId = :patientId AND simulated = 1")
    suspend fun deleteSimulated(patientId: Long)

    @Query("DELETE FROM sessions WHERE patientId = :patientId")
    suspend fun deleteAllFor(patientId: Long)
}
