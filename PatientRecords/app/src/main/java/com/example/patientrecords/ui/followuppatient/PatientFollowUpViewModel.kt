package com.example.patientrecords.ui.followuppatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.data.PatientRepository
import kotlinx.coroutines.launch

class PatientFollowUpViewModel(
    private val repository: PatientRepository,
    private val patientId: Int
) : ViewModel() {

    val patient : LiveData<Patient> = repository.getPatientById(patientId).asLiveData()
    val patientFollowUps: LiveData<List<PatientFollowUp>> = repository.getFollowUps(patientId).asLiveData()

    fun insertFollowUp(patientFollowUp : PatientFollowUp) {
        viewModelScope.launch {
            repository.addFollowUp(patientFollowUp)
        }
    }

    fun updateFollowUp(patientFollowUp: PatientFollowUp){
        viewModelScope.launch {
            repository.updateFollowUp(patientFollowUp)
        }
    }
}
