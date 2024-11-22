package com.example.patientrecords.ui.viewallpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.PatientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class ViewAllPatientsViewModel(private val repository: PatientRepository) : ViewModel() {

    val allPatients: LiveData<List<Patient>> = repository.getAllPatients().asLiveData()

    // Method 2 - Mutable LiveDate
    val searchQuery = MutableLiveData("")

    val filteredPatients = searchQuery.asFlow()
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.searchPatients(query ?: "")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(newQuery: String) {
        searchQuery.value = newQuery
    }

}
