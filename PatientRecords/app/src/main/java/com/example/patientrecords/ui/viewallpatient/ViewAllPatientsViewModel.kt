package com.example.patientrecords.ui.viewallpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.PatientRepository

class ViewAllPatientsViewModel(private val repository: PatientRepository) : ViewModel() {

    val allPatients: LiveData<List<Patient>> = repository.getAllPatients().asLiveData()

}
