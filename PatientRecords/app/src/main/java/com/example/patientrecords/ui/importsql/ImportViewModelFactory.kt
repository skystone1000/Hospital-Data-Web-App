package com.example.patientrecords.ui.importsql

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.data.PatientDatabase
import com.example.patientrecords.data.sqlimport.ImportRepository

class ImportViewModelFactory(private val db: PatientDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(ImportRepository(db)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
