package com.example.patientrecords.data.localdb

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_up_data",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["id"])]
)
data class PatientFollowUp(
    @PrimaryKey(autoGenerate = true) val followUpId: Int = 0,
    val id: Int = -1,
    val date: Long = System.currentTimeMillis(),  // Store in millis
    val regno: String = "",
    val follow_up_num: Int = 0,
    val weight: Int = -1,
    val treatment_output: String = "",
    val other_complains: String = "",
    val treatment: String = "",
    val medicine_duration: String = "",
    val paid: String = "",
    val balance: Int = -1
)
