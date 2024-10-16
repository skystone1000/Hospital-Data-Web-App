package com.example.patientrecords.ui.patienthistory

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.databinding.ActivityPatientHistoryBinding
import com.example.patientrecords.databinding.ItemFollowupEntryBinding
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.followuppatient.PatientFollowUpActivity


class PatientHistoryActivity : BaseActivity(R.layout.activity_patient_history) {

    private lateinit var binding: ActivityPatientHistoryBinding
    private lateinit var viewModel: PatientHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val patientId = intent.getIntExtra("patient_id", -1)
        val patientRegNo = intent.getIntExtra("patient_regno", -1)

        viewModel = ViewModelProvider(
            this,
            PatientHistoryViewModelFactory(patientId, (application as PatientRecordsApp).repository)
        )[PatientHistoryViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.btnAddFollowUp.setOnClickListener {
            // Navigating to Follow Up Activity
            val intent = Intent(this, PatientFollowUpActivity::class.java).apply {
                putExtra("patient_id", patientId)
                putExtra("patient_regno", patientRegNo)
            }
            startActivity(intent)
        }

        viewModel.Patient.observe(this){ patient->
            binding.cvPatientDetails.tvName.text = "${patient.firstName} ${patient.lastName}"
            binding.cvPatientDetails.tvSex.text = patient.sex ?: "N/A"
            binding.cvPatientDetails.tvOccupation.text = patient.occupation ?: "N/A"
            binding.cvPatientDetails.tvPhone.text = patient.phone ?: "N/A"
            binding.cvPatientDetails.tvRegNo.text = patient.regno ?: "N/A"
        }

        lifecycleScope.launchWhenStarted {
            viewModel.followUps.collect { list ->
                displayFollowUps(list)
            }
        }
    }

    private fun displayFollowUps(followUps: List<PatientFollowUp>) {
        val container = binding.followUpListContainer
        container.removeAllViews()

        followUps.forEach { followUp ->
            val itemBinding = ItemFollowupEntryBinding.inflate(layoutInflater)
            itemBinding.tvFollowUpDate.text = "Date: ${followUp.date}"
            itemBinding.tvFollowUpNum.text = "Follow Up Number: ${followUp.follow_up_num}"
            itemBinding.btnViewDetails.setOnClickListener {
                // handle view follow-up details
            }
            container.addView(itemBinding.root)
        }
    }
}
