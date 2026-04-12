package com.example.patientrecords

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.patientrecords.data.FirebaseRepository
import com.example.patientrecords.data.PatientDatabase
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.utils.ThemePreferences

class PatientRecordsApp : Application() {

    // Expose repository for use across the app
    lateinit var repository: PatientRepository
        private set

    // Expose firebase repository for use across the app
    lateinit var firebaseRepository: FirebaseRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Apply saved theme before any Activity inflates its window
        AppCompatDelegate.setDefaultNightMode(ThemePreferences(this).themeMode)

        // Initialize Room Database
        val database = PatientDatabase.getInstance(this)

        // Initialize Repository
        repository = PatientRepository(database.patientDao(), database.patientFollowUpDao())

        // You can initialize other libraries here if needed in future
        // Timber.plant(Timber.DebugTree())

        // Initialize Firebase Repository
        firebaseRepository = FirebaseRepository()
    }
}