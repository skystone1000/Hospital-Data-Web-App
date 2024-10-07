package com.example.patientrecords.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)

    @Query("SELECT * FROM patient_data")
    fun getAll(): Flow<List<Patient>>

    @Query("SELECT * FROM patient_data WHERE id = :patientId")
    fun getPatientById(patientId: Int): Flow<Patient>
}
