package com.example.patientrecords.ui.followuppatient

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.databinding.ActivityPatientFollowUpBinding
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_FOLLOW_UP_NUMBER
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_PATIENT_ID
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_VIEW_MODE
import com.example.patientrecords.utils.Extensions.Companion.toDisplayDateTime
import kotlin.random.Random


class PatientFollowUpActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientFollowUpBinding
    private lateinit var viewModel: PatientFollowUpViewModel
    private var patientId: Int = -1
    private var patientRegNo: String = ""
    private var totalInitialFollowUps: Int = 0
    private var patientFollowUpNumber: String = "-1"
    private var followUpId: Int = -1
    private var isViewMode:Boolean = false
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Getting Extras
        patientId = intent.getIntExtra(EXTRA_PATIENT_ID, -1)
        patientFollowUpNumber = intent.getStringExtra(EXTRA_FOLLOW_UP_NUMBER).toString()
        isViewMode = intent.getBooleanExtra(EXTRA_VIEW_MODE, false)

        // Binding and ViewModel
        binding = ActivityPatientFollowUpBinding.inflate(layoutInflater)
        viewModel = PatientFollowUpViewModelFactory((application as PatientRecordsApp).repository, patientId).create(PatientFollowUpViewModel::class.java)

        // Updating Lifecycle Owners
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Patient Follow Up")

        // Patient Data
        viewModel.patient.observe(this){
            patientRegNo = it.regno.toString()
        }

        // Get Total number of current patient follow ups
         viewModel.patientFollowUps.observe(this){
             totalInitialFollowUps = it.count()
             if (patientFollowUpNumber != "-1" && isViewMode) {
                 // Find Current Follow up
                 val currentFollowUp = findCurrentFollowup(it)
                 // Add Data to views
                 if (currentFollowUp != null) {
                     setPatientFollowUpFromDb(currentFollowUp)
                     followUpId = currentFollowUp.followUpId
                 }

                 // Set Followup Date and number
                 binding.tvFollowUpDate.visibility = View.VISIBLE
                 binding.tvFollowUpDate.text = "Follow up date: ".plus(currentFollowUp?.date?.toDisplayDateTime())
                 binding.tvFollowUpNum.visibility = View.VISIBLE
                 binding.tvFollowUpNum.text = "Follow up number: ".plus(currentFollowUp?.follow_up_num)

                 // Disable input fields
                 setViewOnlyMode()
                 binding.btnEdit.visibility = View.VISIBLE
             }else{
                 enableAllFields()
             }
        }

        binding.btnEdit.setOnClickListener {
            isEditMode = true
            enableAllFields()
            binding.btnUpdate.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
        }

        binding.btnUpdate.setOnClickListener {
            val updatedPatient = collectPatientFollowUpFromInput()
            viewModel.updateFollowUp(updatedPatient)
            Toast.makeText(this, "Patient details updated", Toast.LENGTH_SHORT).show()
            finish()
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
            if(patientFollowUp.follow_up_num == patientFollowUpNumber){
                return patientFollowUp
            }
        }
        return null
    }

    private fun enableAllFields() {
        binding.etWeight.isEnabled = true
        binding.etTreatmentOutput.isEnabled = true
        binding.etOtherComplains.isEnabled = true
        binding.etTreatment.isEnabled = true
        binding.etMedicineDuration.isEnabled = true
        binding.etPaidAmount.isEnabled = true
        binding.etBalanceAmount.isEnabled = true

        if(isEditMode){
            binding.btnSubmit.visibility = View.GONE
            binding.btnUpdate.visibility = View.VISIBLE
        }else{
            binding.btnSubmit.visibility = View.VISIBLE
            binding.btnUpdate.visibility = View.GONE
        }

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
        binding.btnUpdate.visibility = View.GONE
    }

    private fun collectPatientFollowUpFromInput(): PatientFollowUp {
        // New Follow up Id and number
        var currFollowUpId = Random.nextInt(100000)
        var currFollowUpNo = (totalInitialFollowUps + 1).toString()
        if(isEditMode){
            // Existing Follow up Id and number
            currFollowUpId = followUpId
            currFollowUpNo = patientFollowUpNumber
        }

        var newPatientFollowUp = PatientFollowUp(
            followUpId = currFollowUpId,  // Replace with a proper ID logic if needed,
            id = patientId,
            date = System.currentTimeMillis(),
            regno = patientRegNo,
            follow_up_num = currFollowUpNo,
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
