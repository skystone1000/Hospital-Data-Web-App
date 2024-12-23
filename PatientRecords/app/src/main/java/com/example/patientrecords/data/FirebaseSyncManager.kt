package com.example.patientrecords.data

import kotlinx.coroutines.flow.first

class FirebaseSyncManager(
    private val localRepo: PatientRepository,
    private val firebaseRepo: FirebaseRepository
) {
    suspend fun syncPatientsBothWays() {


        val localPatients = localRepo.getAllPatients().first()
        val remotePatients = firebaseRepo.downloadPatients()

        /*val remoteIds = remotePatients.map { it.id }.toSet()
        val localIds = localPatients.map { it.id }.toSet()

        val toFirebase = localPatients.filter { it.id !in remoteIds }
        val toRoom = remotePatients.filter { it.id !in localIds }*/

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
                    (local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()
        }



        firebaseRepo.uploadPatients(toFirebase)
        toRoom.forEach{
            localRepo.insertPatient(it)
        }

//        firebaseRepo.sampleData()
//        localRepo.insertAll(toRoom)
    }
}
