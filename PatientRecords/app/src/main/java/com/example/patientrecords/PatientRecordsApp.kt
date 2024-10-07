package com.example.patientrecords

import android.app.Application
import com.example.patientrecords.data.PatientDatabase
import com.example.patientrecords.data.PatientRepository

class PatientRecordsApp : Application() {

    // Expose repository for use across the app
    lateinit var repository: PatientRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Room Database
        val database = PatientDatabase.getInstance(this)

        // Initialize Repository
        repository = PatientRepository(database.patientDao(), database.patientFollowUpDao())

        // You can initialize other libraries here if needed in future
        // Timber.plant(Timber.DebugTree())
        // FirebaseApp.initializeApp(this)
    }
}