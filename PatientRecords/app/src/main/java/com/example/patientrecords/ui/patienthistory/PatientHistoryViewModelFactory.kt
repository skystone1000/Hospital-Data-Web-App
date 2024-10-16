package com.example.patientrecords.ui.patienthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.data.PatientRepository

class PatientHistoryViewModelFactory(
    private val patientId: Int,
    private val repository: PatientRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientHistoryViewModel(patientId, repository) as T
    }
}
