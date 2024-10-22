package com.example.patientrecords.ui.addpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.localdb.Patient
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.example.patientrecords.data.PatientRepository

class AddPatientViewModel(private val repository: PatientRepository) : ViewModel() {
    private val _patientLiveData = MutableLiveData<Patient?>()
    val patientLiveData: LiveData<Patient?> = _patientLiveData

    fun insertPatient(patient: Patient) {
        viewModelScope.launch {
            repository.insertPatient(patient)
        }
    }

    fun getPatientById(patientId: Int) {
        viewModelScope.launch {
            repository.getPatientById(patientId).collect { patientObj ->
                _patientLiveData.value = patientObj
            }
        }
    }
}
