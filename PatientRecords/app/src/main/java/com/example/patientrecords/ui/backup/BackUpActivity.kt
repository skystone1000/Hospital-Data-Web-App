package com.example.patientrecords.ui.backup

import android.os.Bundle
import android.widget.Toast
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.databinding.ActivityBackUpBinding
import com.example.patientrecords.ui.base.BaseActivity
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class BackUpActivity : BaseActivity() {

    private lateinit var binding: ActivityBackUpBinding
    private lateinit var viewModel: BackUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding and ViewModel
        binding = ActivityBackUpBinding.inflate(layoutInflater)
        viewModel = BackUpViewModel(
            (application as PatientRecordsApp).repository,
            (application as PatientRecordsApp).firebaseRepository
        )

        // Updating Lifecycle Owners
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Patient Backup")

        // Sync to cloud — button disabled during sync to prevent duplicate triggers.
        // Toast fires only after both patient and follow-up syncs complete (syncData is suspend).
        // Errors from Firebase are surfaced as a Toast instead of being silently swallowed.
        binding.btnSyncToCloud.setOnClickListener {
            lifecycleScope.launch {
                binding.btnSyncToCloud.isEnabled = false
                try {
                    viewModel.syncData()
                    Toast.makeText(this@BackUpActivity, "Sync completed!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@BackUpActivity, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnSyncToCloud.isEnabled = true
                }
            }
        }
    }
}
