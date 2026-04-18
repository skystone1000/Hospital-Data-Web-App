package com.example.patientrecords.ui.importsql

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.patientrecords.data.sqlimport.ImportProgress
import com.example.patientrecords.data.sqlimport.ImportRepository
import com.example.patientrecords.data.sqlimport.ImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportUiState {
    object Idle : ImportUiState()
    data class ReadingFile(val fileName: String) : ImportUiState()
    object Parsing : ImportUiState()
    data class InsertingPatients(val done: Int, val total: Int) : ImportUiState()
    data class InsertingFollowUps(val done: Int, val total: Int) : ImportUiState()
    data class Success(val result: ImportResult) : ImportUiState()
    data class PartialSuccess(val result: ImportResult) : ImportUiState()
    data class Failure(val reason: String) : ImportUiState()
}

class ImportViewModel(private val repo: ImportRepository) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun startImport(sqlContent: String, fileName: String) {
        if (_state.value != ImportUiState.Idle) return
        viewModelScope.launch {
            repo.importFromSql(sqlContent, fileName).collect { progress ->
                _state.value = when (progress) {
                    is ImportProgress.ReadingFile       -> ImportUiState.ReadingFile(progress.fileName)
                    is ImportProgress.Parsing           -> ImportUiState.Parsing
                    is ImportProgress.InsertingPatients -> ImportUiState.InsertingPatients(progress.done, progress.total)
                    is ImportProgress.InsertingFollowUps-> ImportUiState.InsertingFollowUps(progress.done, progress.total)
                    is ImportProgress.Completed         -> {
                        if (progress.result.rowErrors.isEmpty()) ImportUiState.Success(progress.result)
                        else ImportUiState.PartialSuccess(progress.result)
                    }
                    is ImportProgress.Failed            -> ImportUiState.Failure(progress.reason)
                }
            }
        }
    }
}
