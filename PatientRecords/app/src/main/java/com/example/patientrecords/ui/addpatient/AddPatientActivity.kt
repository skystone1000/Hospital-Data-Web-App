package com.example.patientrecords.ui.addpatient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.Patient
import com.example.patientrecords.data.PatientDatabase
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.databinding.ActivityAddPatientBinding
import com.example.patientrecords.ui.base.BaseActivity
import kotlin.random.Random

class AddPatientActivity : BaseActivity(R.layout.activity_add_patient) {

    private lateinit var binding: ActivityAddPatientBinding
    private lateinit var viewModel: AddPatientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbarTitle("Add a Patient")

        val factory = AddPatientViewModelFactory((application as PatientRecordsApp).repository)
        viewModel = ViewModelProvider(this, factory)[AddPatientViewModel::class.java]

        binding.btnSubmit.setOnClickListener {
            val patient = collectPatientFromInput()
            viewModel.insertPatient(patient)
            Toast.makeText(this, "Patient Added", Toast.LENGTH_SHORT).show()
            finish()
        }
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