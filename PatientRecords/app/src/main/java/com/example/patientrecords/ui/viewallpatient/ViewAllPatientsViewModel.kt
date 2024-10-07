package com.example.patientrecords.ui.viewallpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.Patient
import com.example.patientrecords.data.PatientRepository
import kotlinx.coroutines.launch

class ViewAllPatientsViewModel(private val repository: PatientRepository) : ViewModel() {

    val allPatients: LiveData<List<Patient>> = repository.getAllPatients().asLiveData()

}
