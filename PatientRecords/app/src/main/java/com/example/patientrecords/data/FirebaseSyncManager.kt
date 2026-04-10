package com.example.patientrecords.data

import kotlinx.coroutines.flow.first

class FirebaseSyncManager(
    private val localRepo: PatientRepository,
    private val firebaseRepo: FirebaseRepository
) {
    suspend fun syncPatientsBothWays() {
        val localPatients = localRepo.getAllPatients().first()
        val remotePatients = firebaseRepo.downloadPatients()

        // Detect new or updated patients to push to Firebase:
        // push if no remote copy exists, or if the local record is strictly newer (Long comparison)
        val toFirebase = localPatients.filter { local ->
            val remote = remotePatients.find { it.id == local.id }
            remote == null || (local != remote && (local.dateJoined ?: 0L) > (remote.dateJoined ?: 0L))
        }

        // Detect new or updated patients to pull into Room:
        // insert if no local copy exists, or if the remote record is strictly newer
        val toRoom = remotePatients.filter { remote ->
            val local = localPatients.find { it.id == remote.id }
            local == null || (remote != local && (remote.dateJoined ?: 0L) > (local.dateJoined ?: 0L))
        }

        // Update whichever side is not synced
        firebaseRepo.uploadPatients(toFirebase)
        toRoom.forEach { localRepo.insertPatient(it) }
    }

    suspend fun syncPatientFollowUpsBothWays() {
        val localFollowUps = localRepo.getAllFollowUps()
        val remoteFollowUps = firebaseRepo.downloadFollowUps()

        // Detect new or updated follow-ups to push to Firebase
        val toFirebase = localFollowUps.filter { local ->
            val remote = remoteFollowUps.find { it.followUpId == local.followUpId }
            remote == null || (local != remote && local.date > remote.date)
        }

        // Detect new or updated follow-ups to pull into Room
        val toRoom = remoteFollowUps.filter { remote ->
            val local = localFollowUps.find { it.followUpId == remote.followUpId }
            local == null || (remote != local && remote.date > local.date)
        }

        // Update whichever side is not synced
        firebaseRepo.uploadPatientFollowUps(toFirebase)
        toRoom.forEach { localRepo.addFollowUp(it) }
    }
}
