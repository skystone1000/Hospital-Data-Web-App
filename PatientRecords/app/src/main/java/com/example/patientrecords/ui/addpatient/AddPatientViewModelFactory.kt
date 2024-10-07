package com.example.patientrecords.ui.addpatient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.data.PatientRepository

class AddPatientViewModelFactory(private val repository: PatientRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPatientViewModel::class.java)) {
            return AddPatientViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
