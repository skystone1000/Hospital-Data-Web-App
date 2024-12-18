package com.example.patientrecords.data.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.patientrecords.utils.Extensions
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Query("SELECT * FROM patient_data")
    fun getAll(): Flow<List<Patient>>

    @Query("SELECT * FROM patient_data WHERE id = :patientId")
    fun getPatientById(patientId: Int): Flow<Patient>

    @Query("SELECT * FROM patient_data WHERE firstName LIKE '%' || :query || '%' OR middleName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<Patient>>

    @Query("SELECT * FROM patient_data WHERE dateJoined >= :date")
    suspend fun getPatientsFromDay(date: Long): List<Patient>


}
