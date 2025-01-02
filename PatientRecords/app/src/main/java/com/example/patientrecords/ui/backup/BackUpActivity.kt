package com.example.patientrecords.ui.backup

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.databinding.ActivityBackUpBinding
import com.example.patientrecords.ui.base.BaseActivity
import kotlinx.coroutines.launch

class BackUpActivity : BaseActivity() {

    private lateinit var binding: ActivityBackUpBinding
    private lateinit var viewModel: BackUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding and ViewModel
        binding = ActivityBackUpBinding.inflate(layoutInflater)
        viewModel = BackUpViewModel((application as PatientRecordsApp).repository , (application as PatientRecordsApp).firebaseRepository)

        // Updating Lifecycle Owners
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Patient Backup")


        // Sync to cloud
        binding.btnSyncToCloud.setOnClickListener {
            lifecycleScope.launch {
                viewModel.syncData()
                Toast.makeText(this@BackUpActivity, "Sync completed!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}