package com.example.patientrecords.data

import kotlinx.coroutines.flow.first

class FirebaseSyncManager(
    private val localRepo: PatientRepository,
    private val firebaseRepo: FirebaseRepository
) {
    suspend fun syncPatientsBothWays() {
        val localPatients = localRepo.getAllPatients().first()
        val remotePatients = firebaseRepo.downloadPatients()

        /*
        // Comparing just with IDs
        val remoteIds = remotePatients.map { it.id }.toSet()
        val localIds = localPatients.map { it.id }.toSet()

        val toFirebase = localPatients.filter { it.id !in remoteIds }
        val toRoom = remotePatients.filter { it.id !in localIds }
        */

        // Detect new or updated patients to push to Firebase
        val toFirebase = localPatients.filter { local ->
            val remote = remotePatients.find { it.id == local.id }
            remote == null || local != remote &&
                    (local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()
        }

        // Detect new or updated patients to insert into Room
        val toRoom = remotePatients.filter { remote ->
            val local = localPatients.find { it.id == remote.id }
            local == null || remote != local &&
                    (local.dateJoined ?: 0).toString() < (remote.dateJoined ?: 0).toString()
        }

        firebaseRepo.uploadPatients(toFirebase)
        toRoom.forEach{
            localRepo.insertPatient(it)
        }
    }

    suspend fun syncPatientFollowUpsBothWays(){
        val localPatientFollowUps = localRepo.getAllFollowUps()
        val remotePatientFollowUps = firebaseRepo.downloadFollowUps()

        // Detect new or updated patients to push to Firebase
        val toFirebase = localPatientFollowUps.filter { local ->
            val remote = remotePatientFollowUps.find { it.followUpId == local.followUpId }
            remote == null || local != remote &&
                    (local.date ?: 0).toString() > (remote.date ?: 0).toString()
        }

        // Detect new or updated patients to insert into Room
        val toRoom = remotePatientFollowUps.filter { remote ->
            val local = localPatientFollowUps.find { it.followUpId == remote.followUpId }
            local == null || remote != local &&
                    (local.date ?: 0).toString() < (remote.date ?: 0).toString()
        }

        // Update whichever is not synced
        firebaseRepo.uploadPatientFollowUps(toFirebase)
        toRoom.forEach {
            localRepo.addFollowUp(it)
        }
    }
}
