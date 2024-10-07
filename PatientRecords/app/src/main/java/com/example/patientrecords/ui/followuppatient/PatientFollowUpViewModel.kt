package com.example.patientrecords.ui.followuppatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.Patient
import com.example.patientrecords.data.PatientFollowUp
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.databinding.ActivityPatientFollowUpBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PatientFollowUpViewModel(
    private val repository: PatientRepository,
    private val patientId: Int
) : ViewModel() {

    val patient : LiveData<Patient> = repository.getPatientById(patientId).asLiveData()

    fun insertFollowUp(patientFollowUp : PatientFollowUp) {
        viewModelScope.launch {
            repository.addFollowUp(patientFollowUp)
        }
    }
}
