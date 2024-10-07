package com.example.patientrecords

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.patientrecords.databinding.ActivityMainBinding
import com.example.patientrecords.ui.addpatient.AddPatientActivity
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.followuppatient.PatientFollowUpActivity
import com.example.patientrecords.ui.viewallpatient.ViewAllPatientsActivity

class MainActivity : BaseActivity(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle clicks
        binding.btnPatientRecords.setOnClickListener {
             startActivity(Intent(this, ViewAllPatientsActivity::class.java))
        }

        binding.btnAddPatient.setOnClickListener {
            startActivity(Intent(this, AddPatientActivity::class.java))
        }
    }
}