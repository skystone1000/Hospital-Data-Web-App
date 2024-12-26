package com.example.patientrecords.data

import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientDao
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.data.localdb.PatientFollowUpDao
import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao, private val patientFollowUpDao: PatientFollowUpDao) {

    // Patient Methods
    suspend fun insertPatient(patient: Patient) = patientDao.insert(patient)
    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)
    suspend fun deletePatient(patient: Patient) = patientDao.delete(patient)
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAll()

    fun getPatientById(patientId: Int) : Flow<Patient>{
        return patientDao.getPatientById(patientId)
    }

    // Follow Up Methods
    suspend fun addFollowUp(followUp: PatientFollowUp) {
        patientFollowUpDao.insertFollowUp(followUp)
    }

    suspend fun updateFollowUp(followUp: PatientFollowUp) {
        patientFollowUpDao.updateFollowUp(followUp)
    }

    fun getFollowUps(patientId: Int): Flow<List<PatientFollowUp>> {
        return patientFollowUpDao.getFollowUpsForPatient(patientId)
    }

    suspend fun getAllFollowUps(): List<PatientFollowUp> {
        return patientFollowUpDao.getAllFollowUps()
    }

    // Search Method
    fun searchPatients(query: String): Flow<List<Patient>> {
        return patientDao.searchPatients(query)
    }

    // Dashboard Methods
    suspend fun getPatientsFromDay(date: Long): List<Patient> = patientDao.getPatientsFromDay(date)
    suspend fun getFollowUpsFromDay(date: Long): List<PatientFollowUp> = patientFollowUpDao.getFollowUpsFromDay(date)
    suspend fun getPatientWithFollowUpFromDay(date: Long): List<Patient> = patientFollowUpDao.getPatientsWithFollowUpsFromDay(date)

}