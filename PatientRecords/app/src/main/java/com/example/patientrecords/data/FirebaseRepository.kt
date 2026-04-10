package com.example.patientrecords.data

import android.util.Log
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRepository {

    private val dbPatientRef = FirebaseDatabase.getInstance().getReference("patients")
    private val dbPatientFollowUpRef = FirebaseDatabase.getInstance().getReference("patient_follow_ups")

    // Upload Patients — fire-and-forget; failures are logged but do not throw
    fun uploadPatients(patients: List<Patient>) {
        patients.forEach { patient ->
            dbPatientRef.child(patient.id.toString()).setValue(patient)
                .addOnSuccessListener {
                    Log.d("Firebase", "Patient ${patient.id} uploaded.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to upload patient ${patient.id}: ${e.message}")
                }
        }
    }

    // Download Patients — suspends until Firebase responds; propagates errors via resumeWithException
    // so callers (FirebaseSyncManager → BackUpViewModel → BackUpActivity) can show a Toast on failure.
    // invokeOnCancellation removes the listener if the coroutine is cancelled before the callback fires.
    suspend fun downloadPatients(): List<Patient> = suspendCancellableCoroutine { cont ->
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Patient::class.java) }
                cont.resume(list)
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        }
        dbPatientRef.addListenerForSingleValueEvent(listener)
        cont.invokeOnCancellation { dbPatientRef.removeEventListener(listener) }
    }

    // Upload Follow-ups — fire-and-forget; failures are logged but do not throw
    fun uploadPatientFollowUps(patientFollowUps: List<PatientFollowUp>) {
        patientFollowUps.forEach { followUp ->
            dbPatientFollowUpRef.child(followUp.followUpId.toString()).setValue(followUp)
                .addOnSuccessListener {
                    Log.d("Firebase", "Follow-up ${followUp.followUpId} uploaded.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to upload follow-up ${followUp.followUpId}: ${e.message}")
                }
        }
    }

    // Download Follow-ups — same cancellation-safe pattern as downloadPatients
    suspend fun downloadFollowUps(): List<PatientFollowUp> = suspendCancellableCoroutine { cont ->
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(PatientFollowUp::class.java) }
                cont.resume(list)
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        }
        dbPatientFollowUpRef.addListenerForSingleValueEvent(listener)
        cont.invokeOnCancellation { dbPatientFollowUpRef.removeEventListener(listener) }
    }
}
