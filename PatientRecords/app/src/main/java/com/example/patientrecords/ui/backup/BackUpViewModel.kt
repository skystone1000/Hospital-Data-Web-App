package com.example.patientrecords.ui.backup

import androidx.lifecycle.ViewModel
import com.example.patientrecords.data.FirebaseRepository
import com.example.patientrecords.data.FirebaseSyncManager
import com.example.patientrecords.data.PatientRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class BackUpViewModel(
    private val repository: PatientRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    // Sync patients and follow-ups concurrently.
    // coroutineScope suspends until both child coroutines finish, so the completion Toast
    // in BackUpActivity fires only after both syncs are done. Any exception propagates to
    // the caller and is shown as a Toast error message.
    suspend fun syncData() {
        val syncManager = FirebaseSyncManager(repository, firebaseRepository)
        coroutineScope {
            launch { syncManager.syncPatientsBothWays() }
            launch { syncManager.syncPatientFollowUpsBothWays() }
        }
    }
}
