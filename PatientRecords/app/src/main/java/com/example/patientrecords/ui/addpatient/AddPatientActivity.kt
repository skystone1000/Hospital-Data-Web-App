package com.example.patientrecords.ui.addpatient

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.databinding.ActivityAddPatientBinding
import com.example.patientrecords.ui.base.BaseActivity
import kotlin.random.Random

class AddPatientActivity : BaseActivity(R.layout.activity_add_patient) {

    private lateinit var binding: ActivityAddPatientBinding
    private lateinit var viewModel: AddPatientViewModel

    var patientId = -1
    var isViewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbarTitle("Add a Patient")

        val factory = AddPatientViewModelFactory((application as PatientRecordsApp).repository)
        viewModel = ViewModelProvider(this, factory)[AddPatientViewModel::class.java]

        patientId = intent.getIntExtra("patient_id", -1)
        isViewMode = intent.getBooleanExtra("is_view_mode", false)

        if (patientId != -1) {
            viewModel.getPatientById(patientId)
        }

        viewModel.patientLiveData.observe(this) { patient ->
            patient?.let {
                setPatientFromDb(it)
                if (isViewMode) {
                    setViewOnlyMode()
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            val patient = collectPatientFromInput()
            viewModel.insertPatient(patient)
            Toast.makeText(this, "Patient Added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun setPatientFromDb(patient: Patient) {
        binding.etFirstName.setText(patient.firstName)
        binding.etMiddleName.setText(patient.middleName)
        binding.etLastName.setText(patient.lastName)
        binding.etAge.setText(patient.age.toString()) // Because Its an Int field
        binding.etSex.setText(patient.sex)
        binding.occupation.setText(patient.occupation)
        binding.address.setText(patient.address)
        binding.etPhone.setText(patient.phone)
        binding.etRegNo.setText(patient.regno)
        patient.height?.let { binding.etHeight.setText(it.toString()) }
        patient.weight?.let { binding.etWeight.setText(it.toString()) }
        binding.etCc1.setText(patient.cc1)
        binding.etCc2.setText(patient.cc2)
        binding.etCc3.setText(patient.cc3)
        binding.etAppetite.setText(patient.appetite)
        binding.etDesire.setText(patient.desire)
        binding.etAversions.setText(patient.aversions)
        binding.etThirst.setText(patient.thirst)
        binding.etPerspiration.setText(patient.perspiration)
        binding.etSleep.setText(patient.sleep)
        binding.etStool.setText(patient.stool)
        binding.etUrine.setText(patient.urine)
        binding.etMenses.setText(patient.menses)
        binding.etThermal.setText(patient.thermal)
        binding.etMind.setText(patient.mind)
        binding.etHobbies.setText(patient.hobbies)
        binding.etParticulars.setText(patient.particulars)
        binding.etOnExamination.setText(patient.on_examination)
        binding.etPathInv.setText(patient.path_inv)
        binding.etPreviousRx.setText(patient.previous_rx)
        binding.etPastHistory.setText(patient.past_history)
        binding.etFamilyHistory.setText(patient.family_history)
        binding.etTreatment.setText(patient.treatment)
        binding.etPaid.setText(patient.paid)
        binding.etBalance.setText(patient.balance)
    }

    private fun setViewOnlyMode() {
        binding.etFirstName.isEnabled = false
        binding.etMiddleName.isEnabled = false
        binding.etLastName.isEnabled = false
        binding.etAge.isEnabled = false
        binding.etSex.isEnabled = false
        binding.occupation.isEnabled = false
        binding.address.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.etRegNo.isEnabled = false
        binding.etHeight.isEnabled = false
        binding.etWeight.isEnabled = false
        binding.etCc1.isEnabled = false
        binding.etCc2.isEnabled = false
        binding.etCc3.isEnabled = false
        binding.etAppetite.isEnabled = false
        binding.etDesire.isEnabled = false
        binding.etAversions.isEnabled = false
        binding.etThirst.isEnabled = false
        binding.etPerspiration.isEnabled = false
        binding.etSleep.isEnabled = false
        binding.etStool.isEnabled = false
        binding.etUrine.isEnabled = false
        binding.etMenses.isEnabled = false
        binding.etThermal.isEnabled = false
        binding.etMind.isEnabled = false
        binding.etHobbies.isEnabled = false
        binding.etParticulars.isEnabled = false
        binding.etOnExamination.isEnabled = false
        binding.etPathInv.isEnabled = false
        binding.etPreviousRx.isEnabled = false
        binding.etPastHistory.isEnabled = false
        binding.etFamilyHistory.isEnabled = false
        binding.etTreatment.isEnabled = false
        binding.etPaid.isEnabled = false
        binding.etBalance.isEnabled = false

        binding.btnSubmit.visibility = View.GONE
    }

    private fun collectPatientFromInput(): Patient {
        return Patient(
            id = Random.nextInt(100000),  // Replace with a proper ID logic if needed
            firstName = binding.etFirstName.text.toString(),
            middleName = binding.etMiddleName.text.toString(),
            lastName = binding.etLastName.text.toString(),
            age = binding.etAge.text.toString().toIntOrNull() ?: 0,
            sex = binding.etSex.text.toString(),
            occupation = binding.occupation.text.toString(),
            address = binding.address.text.toString(),
            phone = binding.etPhone.text.toString(),
            regno = binding.etRegNo.text.toString(),
            height = binding.etHeight.text.toString().toIntOrNull(),
            weight = binding.etWeight.text.toString().toIntOrNull(),
            cc1 = binding.etCc1.text.toString(),
            cc2 = binding.etCc2.text.toString(),
            cc3 = binding.etCc3.text.toString(),
            appetite = binding.etAppetite.text.toString(),
            desire = binding.etDesire.text.toString(),
            aversions = binding.etAversions.text.toString(),
            thirst = binding.etThirst.text.toString(),
            perspiration = binding.etPerspiration.text.toString(),
            sleep = binding.etSleep.text.toString(),
            stool = binding.etStool.text.toString(),
            urine = binding.etUrine.text.toString(),
            menses = binding.etMenses.text.toString(),
            thermal = binding.etThermal.text.toString(),
            mind = binding.etMind.text.toString(),
            hobbies = binding.etHobbies.text.toString(),
            particulars = binding.etParticulars.text.toString(),
            on_examination = binding.etOnExamination.text.toString(),
            path_inv = binding.etPathInv.text.toString(),
            previous_rx = binding.etPreviousRx.text.toString(),
            past_history = binding.etPastHistory.text.toString(),
            family_history = binding.etFamilyHistory.text.toString(),
            treatment = binding.etTreatment.text.toString(),
            paid = binding.etPaid.text.toString(),
            balance = binding.etBalance.text.toString()
        )
    }

}