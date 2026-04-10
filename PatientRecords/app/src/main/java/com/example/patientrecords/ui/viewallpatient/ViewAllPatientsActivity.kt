package com.example.patientrecords.ui.viewallpatient

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.databinding.ActivityViewAllPatientsBinding
import com.example.patientrecords.ui.base.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewAllPatientsActivity : BaseActivity() {

    private lateinit var binding: ActivityViewAllPatientsBinding
    private lateinit var viewModel: ViewAllPatientsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding and ViewModel
        binding = ActivityViewAllPatientsBinding.inflate(layoutInflater)
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

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("View All Patients")

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredPatients.collectLatest {
                    adapter.submitList(it)
                }
            }
        }

    }
}
