package com.example.patientrecords.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.databinding.ActivitySettingsBinding
import com.example.patientrecords.ui.backup.BackUpViewModel
import com.example.patientrecords.ui.base.BaseActivity
import com.example.patientrecords.ui.importsql.ImportProgressActivity
import com.example.patientrecords.utils.ThemePreferences
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themePrefs: ThemePreferences
    private lateinit var backupViewModel: BackUpViewModel
    private lateinit var pickSqlFile: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register file picker before anything else
        pickSqlFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            startActivity(
                Intent(this, ImportProgressActivity::class.java).apply {
                    putExtra("FILE_URI", uri.toString())
                    putExtra("FILE_NAME", uri.lastPathSegment ?: "backup.sql")
                }
            )
        }

        // Binding and ViewModels
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        themePrefs = ThemePreferences(this)
        backupViewModel = BackUpViewModel(
            (application as PatientRecordsApp).repository,
            (application as PatientRecordsApp).firebaseRepository
        )

        // Updating Lifecycle Owners
        binding.lifecycleOwner = this

        // Toolbars And NavigationDrawer
        setChildContentView(binding.root)
        initToolbarWithDrawer()
        setToolbarTitle("Settings")

        // Set initial theme toggle state from saved preference
        when (themePrefs.themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.btnThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.btnThemeDark.isChecked = true
            else -> binding.btnThemeSystem.isChecked = true
        }

        // Theme toggle — applies immediately and persists across app restarts
        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                binding.btnThemeLight.id -> AppCompatDelegate.MODE_NIGHT_NO
                binding.btnThemeDark.id -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            themePrefs.themeMode = mode
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Import from SQL file
        binding.btnImportSql.setOnClickListener {
            pickSqlFile.launch("*/*")
        }

        // Sync to cloud — disabled during operation to prevent duplicate triggers
        binding.btnSyncToCloud.setOnClickListener {
            lifecycleScope.launch {
                binding.btnSyncToCloud.isEnabled = false
                try {
                    backupViewModel.syncData()
                    Toast.makeText(this@SettingsActivity, "Sync completed!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnSyncToCloud.isEnabled = true
                }
            }
        }
    }
}
