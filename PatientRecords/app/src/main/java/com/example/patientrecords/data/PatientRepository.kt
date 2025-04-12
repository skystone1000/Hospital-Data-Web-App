package com.example.patientrecords.data

import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao, private val patientFollowUpDao: PatientFollowUpDao) {

    suspend fun insertPatient(patient: Patient) = patientDao.insert(patient)
    suspend fun deletePatient(patient: Patient) = patientDao.delete(patient)
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAll()

    fun getPatientById(patientId: Int) : Flow<Patient>{
        return patientDao.getPatientById(patientId)
    }

    suspend fun addFollowUp(followUp: PatientFollowUp) {
        patientFollowUpDao.insertFollowUp(followUp)
    }

    fun getFollowUps(patientId: Int): Flow<List<PatientFollowUp>> {
        return patientFollowUpDao.getFollowUpsForPatient(patientId)
    }
}