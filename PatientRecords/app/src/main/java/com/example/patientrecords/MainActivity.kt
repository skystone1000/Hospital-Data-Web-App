package com.example.patientrecords

import android.content.Intent
import android.os.Bundle
import com.example.patientrecords.databinding.ActivityMainBinding
import com.example.patientrecords.ui.addpatient.AddPatientActivity
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.viewallpatient.ViewAllPatientsActivity

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding and ViewModel
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Updating Lifecycle Owners
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Mahajan Homeo Clinic")

        // Button Clicks
        binding.btnPatientRecords.setOnClickListener {
            startActivity(Intent(this, ViewAllPatientsActivity::class.java))
        }

        binding.btnAddPatient.setOnClickListener {
            startActivity(Intent(this, AddPatientActivity::class.java))
        }
    }
}