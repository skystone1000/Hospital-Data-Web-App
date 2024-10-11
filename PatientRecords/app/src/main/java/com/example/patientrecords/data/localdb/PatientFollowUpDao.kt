package com.example.patientrecords.data.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientFollowUpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: PatientFollowUp)

    @Query("SELECT * FROM follow_up_data WHERE id = :patientId")
    fun getFollowUpsForPatient(patientId: Int): Flow<List<PatientFollowUp>>
}
