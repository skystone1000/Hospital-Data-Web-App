package com.example.patientrecords.data.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.patientrecords.utils.Extensions.Companion.get7DaysAgo
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientFollowUpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: PatientFollowUp)

    @Update
    suspend fun updateFollowUp(followUp: PatientFollowUp)

    @Query("SELECT * FROM follow_up_data WHERE id = :patientId ORDER BY follow_up_num DESC")
    fun getFollowUpsForPatient(patientId: Int): Flow<List<PatientFollowUp>>

    @Query("SELECT * FROM follow_up_data")
    suspend fun getAllFollowUps() : List<PatientFollowUp>

    @Query("SELECT * FROM follow_up_data WHERE date >= :date")
    suspend fun getFollowUpsFromDay(date: Long): List<PatientFollowUp>

    @Query(
        """
        SELECT DISTINCT p.* 
        FROM patient_data p 
        INNER JOIN follow_up_data f 
        ON p.id = f.id 
        WHERE f.date >= :date
        """
    )
    suspend fun getPatientsWithFollowUpsFromDay(date: Long): List<Patient>
}
