package com.example.patientrecords.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.FirebaseRepository
import com.example.patientrecords.data.FirebaseSyncManager
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.utils.Extensions.Companion.get1DayAgo
import com.example.patientrecords.utils.Extensions.Companion.get31DaysAgo
import com.example.patientrecords.utils.Extensions.Companion.get365DaysAgo
import com.example.patientrecords.utils.Extensions.Companion.get7DaysAgo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: PatientRepository, private val firebaseRepository: FirebaseRepository) : ViewModel() {

    private val _patientsLastWeek = MutableStateFlow<List<Patient>>(emptyList())
    val patientsLastWeek: StateFlow<List<Patient>> = _patientsLastWeek

    private val _patientsWithFollowUpsLastWeek = MutableStateFlow<List<Patient>>(emptyList())
    val patientsWithFollowUpsLastWeek: StateFlow<List<Patient>> = _patientsWithFollowUpsLastWeek

    private val _patients1DayCount = MutableLiveData<Int>()
    val patients1DayCount: LiveData<Int> = _patients1DayCount

    private val _patients7DayCount = MutableLiveData<Int>()
    val patients7DayCount: LiveData<Int> = _patients7DayCount

    private val _patients31DayCount = MutableLiveData<Int>()
    val patients31DayCount: LiveData<Int> = _patients31DayCount

    private val _patients365DayCount = MutableLiveData<Int>()
    val patients365DayCount: LiveData<Int> = _patients365DayCount

    private val _followUps1DayCount = MutableLiveData<Int>()
    val followUps1DayCount: LiveData<Int> = _followUps1DayCount

    private val _followUps7DayCount = MutableLiveData<Int>()
    val followUps7DayCount: LiveData<Int> = _followUps7DayCount

    private val _followUps31DayCount = MutableLiveData<Int>()
    val followUps31DayCount: LiveData<Int> = _followUps31DayCount

    private val _followUps365DayCount = MutableLiveData<Int>()
    val followUps365DayCount: LiveData<Int> = _followUps365DayCount

    fun loadDashboardData() {
        viewModelScope.launch {
            _patientsLastWeek.value = repository.getPatientsFromDay(get7DaysAgo())
            _patientsWithFollowUpsLastWeek.value = repository.getPatientWithFollowUpFromDay(get7DaysAgo())
        }
    }

    fun loadSummaryData(){
        viewModelScope.launch {
            _patients1DayCount.value = repository.getPatientsFromDay(get1DayAgo()).size
            _patients7DayCount.value = repository.getPatientsFromDay(get7DaysAgo()).size
            _patients31DayCount.value = repository.getPatientsFromDay(get31DaysAgo()).size
            _patients365DayCount.value = repository.getPatientsFromDay(get365DaysAgo()).size

            _followUps1DayCount.value = repository.getFollowUpsFromDay(get1DayAgo()).size
            _followUps7DayCount.value = repository.getFollowUpsFromDay(get7DaysAgo()).size
            _followUps31DayCount.value = repository.getFollowUpsFromDay(get31DaysAgo()).size
            _followUps365DayCount.value = repository.getFollowUpsFromDay(get365DaysAgo()).size
        }
    }

    fun syncData(){
        syncPatients()
        syncPatientFollowUps()
    }

    fun syncPatients() = viewModelScope.launch {
        FirebaseSyncManager(repository, firebaseRepository).syncPatientsBothWays()
    }

    fun syncPatientFollowUps() = viewModelScope.launch {
        FirebaseSyncManager(repository, firebaseRepository).syncPatientFollowUpsBothWays()
    }
}
