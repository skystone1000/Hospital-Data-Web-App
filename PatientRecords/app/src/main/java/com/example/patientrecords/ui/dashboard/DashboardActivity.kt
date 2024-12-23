package com.example.patientrecords.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.databinding.ActivityDashboardBinding
import com.example.patientrecords.databinding.ItemPatientBinding
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.patienthistory.PatientHistoryActivity
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_PATIENT_ID
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_REG_NO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : BaseActivity(R.layout.activity_dashboard){

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var itemBinding: ItemPatientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding and ViewModel
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        viewModel = DashboardViewModel((application as PatientRecordsApp).repository , (application as PatientRecordsApp).firebaseRepository)

        // Updating Lifecycle Owners
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Dashboard")

        // Code Logic
        observeData()
        setupClock()

        viewModel.loadSummaryData()
        viewModel.loadDashboardData()

        binding.btnSyncFirebase.setOnClickListener {
            lifecycleScope.launch {
                viewModel.syncPatients()
                Toast.makeText(this@DashboardActivity, "Sync completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClock() {
        val clockView = binding.tvClock
        val timeHandler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val time = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                clockView.text = time
                timeHandler.postDelayed(this, 1000)
            }
        }
        timeHandler.post(runnable)
    }

    private fun observeData() {
        // New Patients Data
        lifecycleScope.launchWhenStarted {
            viewModel.patientsLastWeek.collect { list ->
                binding.patientsContainer.removeAllViews()
                list.forEach { patient ->
                    itemBinding = ItemPatientBinding.inflate(
                        LayoutInflater.from(this@DashboardActivity),
                        binding.patientsContainer,
                        false
                    )
                    itemBinding.tvName.text = "${patient.firstName} ${patient.lastName}"
                    itemBinding.tvSex.text = patient.sex // example
                    itemBinding.tvPhone.text = patient.phone
                    itemBinding.tvOccupation.text = patient.occupation
                    itemBinding.tvRegNo.text = patient.regno

                    // ✅ Add click listener for each card
                    itemBinding.root.setOnClickListener {
                        val intent = Intent(this@DashboardActivity, PatientHistoryActivity::class.java).apply {
                            putExtra(EXTRA_PATIENT_ID, patient.id)
                            putExtra(EXTRA_REG_NO, patient.regno)
                        }
                        startActivity(intent)
                    }

                    binding.patientsContainer.addView(itemBinding.root)
                }
            }
        }

        // Patients with followup Data
        lifecycleScope.launchWhenStarted {
            viewModel.patientsWithFollowUpsLastWeek.collect { list ->
                binding.followUpsContainer.removeAllViews()
                list.forEach { patient ->
                    itemBinding = ItemPatientBinding.inflate(
                        LayoutInflater.from(this@DashboardActivity),
                        binding.followUpsContainer,
                        false
                    )
                    itemBinding.tvName.text = "${patient.firstName} ${patient.lastName}"
                    itemBinding.tvSex.text = patient.sex // example
                    itemBinding.tvPhone.text = patient.phone
                    itemBinding.tvOccupation.text = patient.occupation
                    itemBinding.tvRegNo.text = patient.regno

                    // ✅ Add click listener for each card
                    itemBinding.root.setOnClickListener {
                        val intent = Intent(this@DashboardActivity, PatientHistoryActivity::class.java).apply {
                            putExtra(EXTRA_PATIENT_ID, patient.id)
                            putExtra(EXTRA_REG_NO, patient.regno)
                        }
                        startActivity(intent)
                    }

                    binding.followUpsContainer.addView(itemBinding.root)
                }
            }
        }

        // Summary Data
        viewModel.patients1DayCount.observe(this) {
            binding.summaryCards.tvNewPatientsToday.text = "New patients:" + it.toString()
        }
        viewModel.patients7DayCount.observe(this) {
            binding.summaryCards.tvNewPatientsWeek.text = "New patients:" + it.toString()
        }
        viewModel.patients31DayCount.observe(this) {
            binding.summaryCards.tvNewPatientsMonth.text = "New patients:" + it.toString()
        }
        viewModel.patients365DayCount.observe(this) {
            binding.summaryCards.tvNewPatientsYear.text = "New patients:" + it.toString()
        }

        viewModel.followUps1DayCount.observe(this) {
            binding.summaryCards.tvFollowUpsToday.text = "Follow Ups:" + it.toString()
        }
        viewModel.followUps7DayCount.observe(this) {
            binding.summaryCards.tvFollowUpsWeek.text = "Follow Ups:" + it.toString()
        }
        viewModel.followUps31DayCount.observe(this) {
            binding.summaryCards.tvFollowUpsMonth.text = "Follow Ups:" + it.toString()
        }
        viewModel.followUps365DayCount.observe(this) {
            binding.summaryCards.tvFollowUpsYear.text = "Follow Ups:" + it.toString()
        }
    }

}
