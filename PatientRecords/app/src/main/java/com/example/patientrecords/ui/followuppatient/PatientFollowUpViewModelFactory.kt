package com.example.patientrecords.ui.followuppatient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.data.PatientRepository

class PatientFollowUpViewModelFactory(
    private val repository: PatientRepository,
    private val patientId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientFollowUpViewModel(repository, patientId) as T
    }
}
