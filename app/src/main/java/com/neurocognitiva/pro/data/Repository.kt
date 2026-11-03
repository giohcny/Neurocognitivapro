package com.neurocognitiva.pro.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {
    private val patientDao = db.patientDao()
    private val sessionDao = db.sessionDao()

    val patients: Flow<List<PatientEntity>> = patientDao.observeAll()

    fun sessions(patientId: Long): Flow<List<SessionEntity>> = sessionDao.observeByPatient(patientId)

    suspend fun getPatient(id: Long): PatientEntity? = patientDao.get(id)

    /** Inserta (id = 0) o actualiza. Devuelve el id resultante. */
    suspend fun savePatient(patient: PatientEntity): Long =
        if (patient.id == 0L) patientDao.insert(patient) else { patientDao.update(patient); patient.id }

    suspend fun deletePatient(id: Long) = patientDao.delete(id)

    suspend fun saveSession(session: SessionEntity) { sessionDao.insert(session) }

    suspend fun saveSessions(list: List<SessionEntity>) = sessionDao.insertAll(list)

    suspend fun deleteSimulated(patientId: Long) = sessionDao.deleteSimulated(patientId)

    suspend fun deleteSessions(patientId: Long) = sessionDao.deleteAllFor(patientId)
}
