package com.example.patientrecords.ui.patienthistory

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.databinding.ActivityPatientHistoryBinding
import com.example.patientrecords.databinding.ItemFollowupEntryBinding
import com.example.patientrecords.ui.addpatient.AddPatientActivity
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.followuppatient.PatientFollowUpActivity
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_FOLLOW_UP_NUMBER
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_PATIENT_ID
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_REG_NO
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_VIEW_MODE


class PatientHistoryActivity : BaseActivity(R.layout.activity_patient_history) {

    private lateinit var binding: ActivityPatientHistoryBinding
    private lateinit var viewModel: PatientHistoryViewModel

    private var patientId = -1
    private var patientRegNo = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getIntExtra(EXTRA_PATIENT_ID, -1)
        patientRegNo = intent.getIntExtra(EXTRA_REG_NO, -1)

        viewModel = ViewModelProvider(
            this,
            PatientHistoryViewModelFactory(patientId, (application as PatientRecordsApp).repository)
        )[PatientHistoryViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Add New Patient Follow Up
        binding.btnAddFollowUp.setOnClickListener {
            val intent = Intent(this, PatientFollowUpActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_ID, patientId)
                putExtra(EXTRA_REG_NO, patientRegNo)
            }
            startActivity(intent)
        }

        // View Initial Details
        binding.btnInitialDetails.setOnClickListener {
            val intent = Intent(this, AddPatientActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_ID, patientId)
                putExtra(EXTRA_VIEW_MODE, true) // optional flag to mark view-only mode
            }
            startActivity(intent)
        }

        // Update Patient Details Card
        viewModel.patient.observe(this){ patient->
            binding.cvPatientDetails.tvName.text = "${patient.firstName} ${patient.lastName}"
            binding.cvPatientDetails.tvSex.text = patient.sex ?: "N/A"
            binding.cvPatientDetails.tvOccupation.text = patient.occupation ?: "N/A"
            binding.cvPatientDetails.tvPhone.text = patient.phone ?: "N/A"
            binding.cvPatientDetails.tvRegNo.text = patient.regno ?: "N/A"
        }

        // Add All follow ups
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

            // Set data
            itemBinding.tvFollowUpDate.text = "Date: ${followUp.date}"
            itemBinding.tvFollowUpNum.text = "Follow Up Number: ${followUp.follow_up_num}"

            // View Follow up Details
            itemBinding.btnViewDetails.setOnClickListener {
                val intent = Intent(this, PatientFollowUpActivity::class.java).apply {
                    putExtra(EXTRA_PATIENT_ID, patientId)
                    putExtra(EXTRA_REG_NO, patientRegNo)
                    putExtra(EXTRA_FOLLOW_UP_NUMBER, followUp.follow_up_num)
                    putExtra(EXTRA_VIEW_MODE, true) // optional flag to mark view-only mode
                }
                startActivity(intent)
            }

            // Apply margins programmatically
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginVertical = 8.dpToPx()
                val marginHorizontal = 4.dpToPx()
                setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical) // top and bottom margins
            }

            itemBinding.root.layoutParams = layoutParams

            // Add view to container
            container.addView(itemBinding.root)
        }
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }



}
