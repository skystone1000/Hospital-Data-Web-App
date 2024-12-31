package com.example.patientrecords.data.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_data")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String = "",
    val middleName: String? = null,
    val lastName: String = "",
    val age: Int = 0,
    val sex: String? = null,
    val occupation: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val regno: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val cc1: String? = null,
    val cc2: String? = null,
    val cc3: String? = null,
    val appetite: String? = null,
    val desire: String? = null,
    val aversions: String? = null,
    val thirst: String? = null,
    val perspiration: String? = null,
    val sleep: String? = null,
    val stool: String? = null,
    val urine: String? = null,
    val menses: String? = null,
    val thermal: String? = null,
    val mind: String? = null,
    val hobbies: String? = null,
    val particulars: String? = null,
    val on_examination: String? = null,
    val path_inv: String? = null,
    val previous_rx: String? = null,
    val past_history: String? = null,
    val family_history: String? = null,
    val treatment: String? = null,
    val paid: String? = null,
    val balance: String? = null,
    val followUp1: String? = null,
    val followUp2: String? = null,
    val followUp3: String? = null,
    val followUp4: String? = null,
    val dateJoined: Long? = System.currentTimeMillis(),  // Store in millis
    val urlToImage: String? = null
)
