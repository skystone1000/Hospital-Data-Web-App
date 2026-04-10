package com.example.patientrecords.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.databinding.ActivityDashboardBinding
import com.example.patientrecords.databinding.ItemPatientBinding
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.patienthistory.PatientHistoryActivity
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_PATIENT_ID
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : BaseActivity(){

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
        // New Patients Data — repeatOnLifecycle cancels the collector when the Activity is stopped
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.patientsLastWeek.collect { list ->
                    binding.patientsContainer.removeAllViews()
                    list.forEach { patient ->
                        itemBinding = ItemPatientBinding.inflate(
                            LayoutInflater.from(this@DashboardActivity),
                            binding.patientsContainer,
                            false
                        )
                        itemBinding.tvName.text = "${patient.firstName} ${patient.lastName}"
                        itemBinding.tvSex.text = patient.sex
                        itemBinding.tvPhone.text = patient.phone
                        itemBinding.tvOccupation.text = patient.occupation
                        itemBinding.tvRegNo.text = patient.regno
                        itemBinding.root.setOnClickListener {
                            startActivity(
                                Intent(this@DashboardActivity, PatientHistoryActivity::class.java)
                                    .putExtra(EXTRA_PATIENT_ID, patient.id)
                            )
                        }
                        binding.patientsContainer.addView(itemBinding.root)
                    }
                }
            }
        }

        // Patients with follow-up Data
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.patientsWithFollowUpsLastWeek.collect { list ->
                    binding.followUpsContainer.removeAllViews()
                    list.forEach { patient ->
                        itemBinding = ItemPatientBinding.inflate(
                            LayoutInflater.from(this@DashboardActivity),
                            binding.followUpsContainer,
                            false
                        )
                        itemBinding.tvName.text = "${patient.firstName} ${patient.lastName}"
                        itemBinding.tvSex.text = patient.sex
                        itemBinding.tvPhone.text = patient.phone
                        itemBinding.tvOccupation.text = patient.occupation
                        itemBinding.tvRegNo.text = patient.regno
                        itemBinding.root.setOnClickListener {
                            startActivity(
                                Intent(this@DashboardActivity, PatientHistoryActivity::class.java)
                                    .putExtra(EXTRA_PATIENT_ID, patient.id)
                            )
                        }
                        binding.followUpsContainer.addView(itemBinding.root)
                    }
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
