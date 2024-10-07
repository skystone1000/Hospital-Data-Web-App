package com.example.patientrecords.ui.viewallpatient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.data.PatientRepository

class ViewAllPatientsViewModelFactory(private val repository: PatientRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewAllPatientsViewModel::class.java)) {
            return ViewAllPatientsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
