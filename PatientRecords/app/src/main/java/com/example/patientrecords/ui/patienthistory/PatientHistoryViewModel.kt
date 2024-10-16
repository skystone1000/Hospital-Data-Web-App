package com.example.patientrecords.ui.patienthistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PatientHistoryViewModel(
    private val patientId: Int,
    private val repository: PatientRepository
) : ViewModel() {

    val Patient: LiveData<Patient> = repository.getPatientById(patientId).asLiveData()

    val followUps: StateFlow<List<PatientFollowUp>> =
        repository.getFollowUps(patientId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
