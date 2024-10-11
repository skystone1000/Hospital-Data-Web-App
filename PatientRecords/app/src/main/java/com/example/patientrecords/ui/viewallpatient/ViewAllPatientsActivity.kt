package com.example.patientrecords.ui.viewallpatient

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.PatientDatabase
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.databinding.ActivityViewAllPatientsBinding
import com.example.patientrecords.ui.base.BaseActivity

class ViewAllPatientsActivity : BaseActivity(R.layout.activity_view_all_patients) {

    private lateinit var binding: ActivityViewAllPatientsBinding
    private lateinit var viewModel: ViewAllPatientsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllPatientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
        // Initialization Before Application Class
        val dao = PatientDatabase.getInstance(application).patientDao()
        val repository = PatientRepository(dao)
          **/
        val factory = ViewAllPatientsViewModelFactory((application as PatientRecordsApp).repository)
        viewModel = ViewModelProvider(this, factory)[ViewAllPatientsViewModel::class.java]

        viewModel.allPatients.observe(this) { patients ->
            binding.rvPatients.adapter = PatientAdapter(patients)
        }

    }
}
