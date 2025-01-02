package com.example.patientrecords.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.FirebaseRepository
import com.example.patientrecords.data.FirebaseSyncManager
import com.example.patientrecords.data.PatientRepository
import kotlinx.coroutines.launch

class BackUpViewModel(private val repository: PatientRepository, private val firebaseRepository: FirebaseRepository) : ViewModel() {

    fun syncData(){
        syncPatients()
        syncPatientFollowUps()
    }

    private fun syncPatients() = viewModelScope.launch {
        FirebaseSyncManager(repository, firebaseRepository).syncPatientsBothWays()
    }

    private fun syncPatientFollowUps() = viewModelScope.launch {
        FirebaseSyncManager(repository, firebaseRepository).syncPatientFollowUpsBothWays()
    }
}