package com.example.patientrecords.data.sqlimport

sealed class ImportProgress {
    data class ReadingFile(val fileName: String) : ImportProgress()
    object Parsing : ImportProgress()
    data class InsertingPatients(val done: Int, val total: Int) : ImportProgress()
    data class InsertingFollowUps(val done: Int, val total: Int) : ImportProgress()
    data class Completed(val result: ImportResult) : ImportProgress()
    data class Failed(val reason: String, val partial: ImportResult?) : ImportProgress()
}

data class ImportResult(
    val patientsInserted: Int = 0,
    val patientsSkipped: Int = 0,
    val followUpsInserted: Int = 0,
    val followUpsSkipped: Int = 0,
    val followUpsOmitted: Int = 0,
    val rowErrors: List<RowError> = emptyList(),
    val durationMs: Long = 0L
)

data class RowError(
    val table: String,
    val rowIndex: Int,
    val reason: String
)
