package com.example.patientrecords.ui.addpatient

import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.Patient
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.example.patientrecords.data.PatientRepository

class AddPatientViewModel(private val repository: PatientRepository) : ViewModel() {
    fun insertPatient(patient: Patient) {
        viewModelScope.launch {
            repository.insertPatient(patient)
        }
    }
}
