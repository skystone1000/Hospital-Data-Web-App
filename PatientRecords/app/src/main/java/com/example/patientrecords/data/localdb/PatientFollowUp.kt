package com.example.patientrecords.data.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follow_up_data")
data class PatientFollowUp(
    @PrimaryKey(autoGenerate = true) val followUpId: Int,
    val id: Int,
    val date: String,
    val regno: String,
    val follow_up_num: String,
    val weight: Int,
    val treatment_output: String,
    val other_complains: String,
    val treatment: String,
    val medicine_duration: String,
    val paid: String,
    val balance: Int
)
