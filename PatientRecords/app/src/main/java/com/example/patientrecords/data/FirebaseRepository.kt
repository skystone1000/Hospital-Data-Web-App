package com.example.patientrecords.data

import android.util.Log
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientDao
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.data.localdb.PatientFollowUpDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseRepository(private val patientDao: PatientDao, private val patientFollowUpDao: PatientFollowUpDao) {

    private val db = FirebaseDatabase.getInstance().getReference("patients")

    fun uploadPatients(patients: List<Patient>) {
        patients.forEach { patient ->
            db.child(patient.id.toString()).setValue(patient)
                .addOnSuccessListener {
                    Log.d("Firebase", "Test data written successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to write test data: ${e.message}")
                }
        }

    }

    suspend fun downloadPatients(): List<Patient> = suspendCoroutine { cont ->
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Patient::class.java) }
                cont.resume(list)
            }

            override fun onCancelled(error: DatabaseError) {
                cont.resume(emptyList())
            }
        })
    }

    fun sampleData(){

        // Reference to Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val patientsRef = database.getReference("patients")

        // Sample test data
        val patient1 = Patient(
            id = 132,
            firstName = "fwe",
            middleName = "asef",
            lastName = "sfed",
            age = 324,
            sex = "Sef",
            occupation = "efs",
            address = "TODO()",
            phone = "TODO()",
            regno = "TODO()",
            height = 342,
            weight = 234
        )
        val patient2 = Patient(
            id = 132321,
            firstName = "2",
            middleName = "2",
            lastName = "2",
            age = 324,
            sex = "2",
            occupation = "efs",
            address = "TODO()",
            phone = "TODO()",
            regno = "TODO()",
            height = 342,
            weight = 234
        )

        // Write data
        patientsRef.child(patient1.id.toString()).setValue(patient1)
        patientsRef.child(patient2.id.toString()).setValue(patient2)
            .addOnSuccessListener {
                Log.d("Firebase", "Test data written successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to write test data: ${e.message}")
            }
    }

}
