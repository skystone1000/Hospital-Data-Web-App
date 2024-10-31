package com.example.patientrecords.ui.followuppatient

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.databinding.ActivityPatientFollowUpBinding
import com.example.patientrecords.ui.base.BaseActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random


class PatientFollowUpActivity : BaseActivity(R.layout.activity_patient_follow_up) {

    private lateinit var binding: ActivityPatientFollowUpBinding
    private lateinit var viewModel: PatientFollowUpViewModel
    private var patientId: Int = -1
    private var patientRegNo: String = ""
    private var totalInitialFollowUps: Int = 0
    private var patientFollowUpNumber: String = "-1"
    private var isViewMode:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientFollowUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getIntExtra("patient_id", -1)
        patientRegNo = intent.getStringExtra("patient_reg_no").toString()
        viewModel = PatientFollowUpViewModelFactory((application as PatientRecordsApp).repository, patientId).create(PatientFollowUpViewModel::class.java)
        patientFollowUpNumber = intent.getStringExtra("follow_up_number").toString()
        isViewMode = intent.getBooleanExtra("is_view_mode", false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Get Total number of current patient follow ups
         viewModel.patientFollowUps.observe(this){
             totalInitialFollowUps = it.count()
             if (patientFollowUpNumber != "-1") {
                 // Find Current Follow up
                 val currentFollowUp = findCurrentFollowup(it)
                 // Add Data to views
                 if (currentFollowUp != null) {
                     setPatientFollowUpFromDb(currentFollowUp)
                 }
                 // Disable input fields
                 setViewOnlyMode()
             }
        }

        binding.btnSubmit.setOnClickListener {
            val patientFollowUp = collectPatientFollowUpFromInput()
            viewModel.insertFollowUp(patientFollowUp)
            Toast.makeText(this, "Patient Follow Up Added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun findCurrentFollowup(patientFollowUps: List<PatientFollowUp>) : PatientFollowUp? {
        for(patientFollowUp in patientFollowUps){
            if(patientFollowUp.follow_up_num == patientFollowUpNumber.toString()){
                return patientFollowUp
            }
        }
        return null
    }

    private fun setPatientFollowUpFromDb(patientFollowUp: PatientFollowUp) {
        binding.etWeight.setText(patientFollowUp.weight.toString())  // Bcz Int
        binding.etTreatmentOutput.setText(patientFollowUp.treatment_output)
        binding.etOtherComplains.setText(patientFollowUp.other_complains)
        binding.etTreatment.setText(patientFollowUp.treatment)
        binding.etMedicineDuration.setText(patientFollowUp.medicine_duration)
        binding.etPaidAmount.setText(patientFollowUp.paid)
        binding.etBalanceAmount.setText(patientFollowUp.balance.toString())  // Bcz Int
    }

    private fun setViewOnlyMode() {
        binding.etWeight.isEnabled = false
        binding.etTreatmentOutput.isEnabled = false
        binding.etOtherComplains.isEnabled = false
        binding.etTreatment.isEnabled = false
        binding.etMedicineDuration.isEnabled = false
        binding.etPaidAmount.isEnabled = false
        binding.etBalanceAmount.isEnabled = false

        binding.btnSubmit.visibility = View.GONE
    }

    private fun collectPatientFollowUpFromInput(): PatientFollowUp {
        var newPatientFollowUp = PatientFollowUp(
            followUpId = Random.nextInt(100000),  // Replace with a proper ID logic if needed,
            id = patientId,
            date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
            regno = patientRegNo,
            follow_up_num = (totalInitialFollowUps + 1).toString(),
            weight = binding.etWeight.text.toString().toInt(),
            treatment_output = binding.etTreatmentOutput.text.toString(),
            other_complains = binding.etOtherComplains.text.toString(),
            treatment = binding.etTreatment.text.toString(),
            medicine_duration = binding.etMedicineDuration.text.toString(),
            paid = binding.etPaidAmount.text.toString(),
            balance = binding.etBalanceAmount.text.toString().toInt()
        )

        return newPatientFollowUp
    }
}
