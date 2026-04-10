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

class PatientFollowUpActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientFollowUpBinding
    private lateinit var viewModel: PatientFollowUpViewModel

    private var patientId: Int = -1
    private var patientRegNo: String = ""
    private var maxFollowUpNum: Int = 0
    private var patientFollowUpNumber: Int = -1   // -1 = add-new mode
    private var originalFollowUpDate: Long = 0L
    private var followUpId: Int = -1
    private var isViewMode: Boolean = false
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Getting Extras
        patientId = intent.getIntExtra(EXTRA_PATIENT_ID, -1)
        patientFollowUpNumber = intent.getIntExtra(EXTRA_FOLLOW_UP_NUMBER, -1)
        isViewMode = intent.getBooleanExtra(EXTRA_VIEW_MODE, false)

        // Binding and ViewModel
        binding = ActivityPatientFollowUpBinding.inflate(layoutInflater)
        viewModel = PatientFollowUpViewModelFactory(
            (application as PatientRecordsApp).repository, patientId
        ).create(PatientFollowUpViewModel::class.java)

        // Updating Lifecycle Owners
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Patient Follow Up")

        // Patient Data — cache regno for new follow-up records
        viewModel.patient.observe(this) {
            patientRegNo = it.regno.toString()
        }

        // Populate fields and apply view-only mode when viewing an existing follow-up.
        // maxFollowUpNum is set from the list so new records continue from the correct number.
        // originalFollowUpDate is cached here so it is not overwritten on update.
        viewModel.patientFollowUps.observe(this) { list ->
            maxFollowUpNum = list.maxOfOrNull { it.follow_up_num } ?: 0

            if (patientFollowUpNumber != -1 && isViewMode) {
                val currentFollowUp = findCurrentFollowup(list)
                if (currentFollowUp != null) {
                    originalFollowUpDate = currentFollowUp.date
                    followUpId = currentFollowUp.followUpId
                    setPatientFollowUpFromDb(currentFollowUp)
                }

                binding.tvFollowUpDate.visibility = View.VISIBLE
                binding.tvFollowUpDate.text = "Follow up date: ${currentFollowUp?.date?.toDisplayDateTime()}"
                binding.tvFollowUpNum.visibility = View.VISIBLE
                binding.tvFollowUpNum.text = "Follow up number: ${currentFollowUp?.follow_up_num}"

                setViewOnlyMode()
                binding.btnEdit.visibility = View.VISIBLE
            } else {
                enableAllFields()
            }
        }

        // Switch from View → Edit mode
        binding.btnEdit.setOnClickListener {
            isEditMode = true
            enableAllFields()
            binding.btnUpdate.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
        }

        // Update existing follow-up — validates numeric fields before writing; preserves originalFollowUpDate
        binding.btnUpdate.setOnClickListener {
            val (weight, balance) = validateNumericFields() ?: return@setOnClickListener
            viewModel.updateFollowUp(collectPatientFollowUpFromInput(weight, balance))
            Toast.makeText(this, "Follow up updated", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Insert new follow-up — followUpId=0 so Room auto-assigns; validates before writing
        binding.btnSubmit.setOnClickListener {
            val (weight, balance) = validateNumericFields() ?: return@setOnClickListener
            viewModel.insertFollowUp(collectPatientFollowUpFromInput(weight, balance))
            Toast.makeText(this, "Patient Follow Up Added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Validates weight and balance before any DB write; sets field errors and returns null to abort
    private fun validateNumericFields(): Pair<Int, Int>? {
        val weightText = binding.etWeight.text.toString()
        val balanceText = binding.etBalanceAmount.text.toString()

        val weight = weightText.toIntOrNull()
        if (weight == null) {
            binding.etWeight.error = "Enter a valid number"
            return null
        }

        val balance = balanceText.toIntOrNull()
        if (balance == null) {
            binding.etBalanceAmount.error = "Enter a valid number"
            return null
        }

        return Pair(weight, balance)
    }

    private fun findCurrentFollowup(list: List<PatientFollowUp>): PatientFollowUp? {
        return list.find { it.follow_up_num == patientFollowUpNumber }
    }

    private fun enableAllFields() {
        binding.etWeight.isEnabled = true
        binding.etTreatmentOutput.isEnabled = true
        binding.etOtherComplains.isEnabled = true
        binding.etTreatment.isEnabled = true
        binding.etMedicineDuration.isEnabled = true
        binding.etPaidAmount.isEnabled = true
        binding.etBalanceAmount.isEnabled = true

        binding.btnSubmit.visibility = if (isEditMode) View.GONE else View.VISIBLE
        binding.btnUpdate.visibility = if (isEditMode) View.VISIBLE else View.GONE
    }

    private fun setPatientFollowUpFromDb(followUp: PatientFollowUp) {
        binding.etWeight.setText(followUp.weight.toString())
        binding.etTreatmentOutput.setText(followUp.treatment_output)
        binding.etOtherComplains.setText(followUp.other_complains)
        binding.etTreatment.setText(followUp.treatment)
        binding.etMedicineDuration.setText(followUp.medicine_duration)
        binding.etPaidAmount.setText(followUp.paid)
        binding.etBalanceAmount.setText(followUp.balance.toString())
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

    private fun collectPatientFollowUpFromInput(weight: Int, balance: Int): PatientFollowUp {
        val currFollowUpId = if (isEditMode) followUpId else 0   // 0 → Room auto-assigns
        val currFollowUpNum = if (isEditMode) patientFollowUpNumber else maxFollowUpNum + 1
        // Preserve original date on edit; use now for new follow-ups
        val date = if (isEditMode) originalFollowUpDate else System.currentTimeMillis()

        return PatientFollowUp(
            followUpId = currFollowUpId,
            id = patientId,
            date = date,
            regno = patientRegNo,
            follow_up_num = currFollowUpNum,
            weight = weight,
            treatment_output = binding.etTreatmentOutput.text.toString(),
            other_complains = binding.etOtherComplains.text.toString(),
            treatment = binding.etTreatment.text.toString(),
            medicine_duration = binding.etMedicineDuration.text.toString(),
            paid = binding.etPaidAmount.text.toString(),
            balance = balance
        )
    }
}
