package com.example.patientrecords.ui.viewallpatient

import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.databinding.ActivityViewAllPatientsBinding
import com.example.patientrecords.ui.base.BaseActivity
import kotlinx.coroutines.flow.collectLatest

class ViewAllPatientsActivity : BaseActivity(R.layout.activity_view_all_patients) {

    private lateinit var binding: ActivityViewAllPatientsBinding
    private lateinit var viewModel: ViewAllPatientsViewModel
    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding and ViewModel
        binding = ActivityViewAllPatientsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val factory = ViewAllPatientsViewModelFactory((application as PatientRecordsApp).repository)
        viewModel = ViewModelProvider(this, factory)[ViewAllPatientsViewModel::class.java]
        /**
        // Initialization Before Application Class
        val dao = PatientDatabase.getInstance(application).patientDao()
        val repository = PatientRepository(dao)
          **/

        // Updating Lifecycle Owners
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = PatientAdapter()
        binding.rvPatients.adapter = adapter

        /**
        // Used if we manually add listener in place of Data binding in xml
        binding.etSearch.addTextChangedListener {
            viewModel.updateSearchQuery(it.toString())
        }

        // Used Before for showing all patients in place of filtered patients
        viewModel.allPatients.observe(this) { patients ->
            adapter.submitList(patients)
        }
         **/

        lifecycleScope.launchWhenStarted {
            viewModel.filteredPatients.collectLatest {
                adapter.submitList(it)
            }
        }

    }
}
