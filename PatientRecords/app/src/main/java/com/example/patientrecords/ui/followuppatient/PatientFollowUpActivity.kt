package com.example.patientrecords.ui.followuppatient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.PatientFollowUp
import com.example.patientrecords.databinding.ActivityPatientFollowUpBinding
import com.example.patientrecords.ui.base.BaseActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random


class PatientFollowUpActivity : BaseActivity(R.layout.activity_patient_follow_up) {

    private lateinit var binding: ActivityPatientFollowUpBinding
    private lateinit var viewModel: PatientFollowUpViewModel
    private var patientId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientFollowUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getIntExtra("patient_id", -1)
        viewModel = PatientFollowUpViewModelFactory((application as PatientRecordsApp).repository, patientId).create(PatientFollowUpViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.patient.observe(this){ patient ->
            // binding.

        }

        binding.btnSubmit.setOnClickListener {
            val patientFollowUp = collectPatientFollowUpFromInput()
            viewModel.insertFollowUp(patientFollowUp)
            Toast.makeText(this, "Patient Follow Up Added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun collectPatientFollowUpFromInput(): PatientFollowUp{
        var newPatientFollowUp = PatientFollowUp(
            followUpId = Random.nextInt(100000),  // Replace with a proper ID logic if needed,
            id = patientId,
            date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
            regno = "123",
            follow_up_num = "1",
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
