package com.example.patientrecords.data.sqlimport

import androidx.room.withTransaction
import com.example.patientrecords.data.PatientDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

class ImportRepository(private val db: PatientDatabase) {

    fun importFromSql(sqlContent: String, fileName: String): Flow<ImportProgress> = channelFlow {
        val startMs = System.currentTimeMillis()

        send(ImportProgress.ReadingFile(fileName))
        send(ImportProgress.Parsing)

        val patientRows  = SqlImportParser.parseTable(sqlContent, "patient_data")
        val followUpRows = SqlImportParser.parseTable(sqlContent, "follow_up_data")

        if (patientRows.isEmpty()) {
            send(ImportProgress.Failed("No patient_data INSERT statements found in this file.", null))
            return@channelFlow
        }

        // Pre-compute patient IDs present in the backup so we can skip follow-ups
        // that reference patients deleted from the web DB (avoids FK constraint errors).
        val backupPatientIds = patientRows.mapNotNull { it["id"]?.trim()?.toIntOrNull() }.toSet()
        val validFollowUpRows = followUpRows.filter {
            it["id"]?.trim()?.toIntOrNull()?.let { id -> id in backupPatientIds } == true
        }
        val omittedFollowUps = followUpRows.size - validFollowUpRows.size

        val errors = mutableListOf<RowError>()
        var patientsOk = 0; var patientsFail = 0
        var followUpsOk = 0; var followUpsFail = 0

        db.withTransaction {
            patientRows.forEachIndexed { i, row ->
                runCatching {
                    db.patientDao().insert(SqlImportMapper.toPatient(row))
                }.onSuccess { patientsOk++ }
                 .onFailure { errors += RowError("patient_data", i, it.message ?: "unknown"); patientsFail++ }
                send(ImportProgress.InsertingPatients(i + 1, patientRows.size))
            }

            validFollowUpRows.forEachIndexed { i, row ->
                runCatching {
                    db.patientFollowUpDao().insertFollowUp(SqlImportMapper.toFollowUp(row))
                }.onSuccess { followUpsOk++ }
                 .onFailure { errors += RowError("follow_up_data", i, it.message ?: "unknown"); followUpsFail++ }
                send(ImportProgress.InsertingFollowUps(i + 1, validFollowUpRows.size))
            }
        }

        send(
            ImportProgress.Completed(
                ImportResult(
                    patientsInserted  = patientsOk,
                    patientsSkipped   = patientsFail,
                    followUpsInserted = followUpsOk,
                    followUpsSkipped  = followUpsFail,
                    followUpsOmitted  = omittedFollowUps,
                    rowErrors         = errors,
                    durationMs        = System.currentTimeMillis() - startMs
                )
            )
        )
    }.flowOn(Dispatchers.IO)
}
