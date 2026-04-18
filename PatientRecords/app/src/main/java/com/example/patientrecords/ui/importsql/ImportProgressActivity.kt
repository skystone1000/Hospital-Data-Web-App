package com.example.patientrecords.ui.importsql

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.patientrecords.PatientRecordsApp
import com.example.patientrecords.R
import com.example.patientrecords.data.sqlimport.ImportResult
import com.example.patientrecords.databinding.ActivityImportProgressBinding
import com.example.patientrecords.ui.viewallpatient.ViewAllPatientsActivity
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportProgressBinding
    private lateinit var viewModel: ImportViewModel
    private val dotAnimators = mutableListOf<ObjectAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupToolbar()
        setupDotColors()

        viewModel = ViewModelProvider(
            this,
            ImportViewModelFactory((application as PatientRecordsApp).database)
        )[ImportViewModel::class.java]

        observeState()
        readFileAndStartImport()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarHeight   = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            binding.toolbar.updatePadding(top = statusBarHeight)
            binding.contentContainer.updatePadding(bottom = navBarHeight)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDotColors() {
        val primary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, 0)
        listOf(binding.dot1, binding.dot2, binding.dot3).forEach { dot ->
            dot.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(primary)
            }
        }
    }

    private fun readFileAndStartImport() {
        val uriString  = intent.getStringExtra("FILE_URI") ?: return
        val fileName   = intent.getStringExtra("FILE_NAME") ?: "backup.sql"
        val uri        = Uri.parse(uriString)

        lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                }.getOrNull()
            }

            if (content == null) {
                Toast.makeText(this@ImportProgressActivity, "Could not open file.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            startPipelineAnimation()
            viewModel.startImport(content, fileName)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ImportUiState.Idle -> Unit

                        is ImportUiState.ReadingFile -> {
                            setChipActive(binding.chipRead)
                            binding.tvPhaseLabel.text = "Reading ${state.fileName}…"
                            binding.progressBar.isIndeterminate = true
                            binding.tvProgressCount.text = ""
                        }

                        is ImportUiState.Parsing -> {
                            setChipDone(binding.chipRead)
                            setChipActive(binding.chipParse)
                            binding.tvPhaseLabel.text = "Parsing SQL structure…"
                            binding.progressBar.isIndeterminate = true
                            binding.tvProgressCount.text = ""
                        }

                        is ImportUiState.InsertingPatients -> {
                            setChipDone(binding.chipParse)
                            setChipActive(binding.chipPatients)
                            binding.tvPhaseLabel.text = "Importing patients…"
                            binding.progressBar.isIndeterminate = false
                            binding.progressBar.max      = state.total
                            binding.progressBar.progress = state.done
                            binding.tvProgressCount.text = "${state.done} / ${state.total}"
                            binding.tvPatientsCount.text = "${state.done} imported"
                        }

                        is ImportUiState.InsertingFollowUps -> {
                            setChipDone(binding.chipPatients)
                            setChipActive(binding.chipFollowUps)
                            binding.tvPhaseLabel.text = "Importing follow-ups…"
                            binding.progressBar.isIndeterminate = false
                            binding.progressBar.max      = state.total
                            binding.progressBar.progress = state.done
                            binding.tvProgressCount.text = "${state.done} / ${state.total}"
                            binding.tvFollowUpsCount.text = "${state.done} imported"
                        }

                        is ImportUiState.Success        -> showResultView(state.result, hasErrors = false)
                        is ImportUiState.PartialSuccess -> showResultView(state.result, hasErrors = true)
                        is ImportUiState.Failure        -> showErrorView(state.reason)
                    }
                }
            }
        }
    }

    // region chip states
    private val colorSurfaceVariant
        get() = MaterialColors.getColorStateList(this, com.google.android.material.R.attr.colorSurfaceVariant, ColorStateList.valueOf(0))!!
    private val colorPrimaryContainer
        get() = MaterialColors.getColorStateList(this, com.google.android.material.R.attr.colorPrimaryContainer, ColorStateList.valueOf(0))!!
    private val colorSecondaryContainer
        get() = MaterialColors.getColorStateList(this, com.google.android.material.R.attr.colorSecondaryContainer, ColorStateList.valueOf(0))!!

    private fun setChipActive(chip: Chip) {
        chip.chipBackgroundColor = colorPrimaryContainer
    }
    private fun setChipDone(chip: Chip) {
        chip.chipBackgroundColor = colorSecondaryContainer
        chip.chipIcon = getDrawable(R.drawable.baseline_check_circle_24)
    }
    // endregion

    // region pipeline animation
    private fun startPipelineAnimation() {
        binding.pipelineView.post {
            val totalWidth = binding.pipelineView.width.toFloat()
            animateDot(binding.dot1, totalWidth, 0)
            animateDot(binding.dot2, totalWidth, 400)
            animateDot(binding.dot3, totalWidth, 800)
        }
    }

    private fun animateDot(dot: View, pipelineWidth: Float, delayMs: Long) {
        val anim = ObjectAnimator.ofFloat(dot, "translationX", -pipelineWidth * 0.4f, pipelineWidth * 0.4f).apply {
            duration     = 1200
            startDelay   = delayMs
            repeatCount  = ObjectAnimator.INFINITE
            repeatMode   = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }
        anim.start()
        dotAnimators += anim
    }

    private fun stopPipelineAnimation() {
        dotAnimators.forEach { it.cancel() }
        dotAnimators.clear()
    }
    // endregion

    // region result / error views
    private fun showResultView(result: ImportResult, hasErrors: Boolean) {
        setChipDone(binding.chipFollowUps)
        stopPipelineAnimation()

        binding.progressLayer.animate().alpha(0f).withEndAction {
            binding.progressLayer.visibility = View.GONE
            binding.resultLayer.visibility   = View.VISIBLE
            binding.resultLayer.animate().alpha(1f)
        }

        val omissionNote = if (result.followUpsOmitted > 0)
            "\n${result.followUpsOmitted} follow-up record(s) skipped (patients not in this backup)."
        else ""

        if (hasErrors) {
            binding.ivResultIcon.setImageResource(R.drawable.baseline_warning_24)
            binding.ivResultIcon.imageTintList = ColorStateList.valueOf(
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, 0)
            )
            binding.tvResultTitle.text    = "Import Complete with Warnings"
            binding.tvResultSubtitle.text = "${result.rowErrors.size} row(s) could not be imported.$omissionNote"
        } else {
            binding.ivResultIcon.setImageResource(R.drawable.baseline_check_circle_24)
            binding.ivResultIcon.imageTintList = ColorStateList.valueOf(
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, 0)
            )
            binding.tvResultTitle.text    = "Import Complete!"
            binding.tvResultSubtitle.text = if (omissionNote.isNotEmpty())
                "Records imported successfully.$omissionNote"
            else
                "All records imported successfully."
        }

        binding.tvPatientsResultCount.text  = "${result.patientsInserted}"
        binding.tvFollowUpsResultCount.text = "${result.followUpsInserted}"
        binding.tvDuration.text = "%.1f s".format(result.durationMs / 1000.0)

        val totalSkipped = result.patientsSkipped + result.followUpsSkipped
        binding.cardSkipped.isVisible = totalSkipped > 0
        binding.tvSkippedCount.text   = "$totalSkipped"

        if (result.rowErrors.isNotEmpty()) {
            val maxShow = 15
            val shown   = result.rowErrors.take(maxShow)
            val lines   = shown.joinToString("\n") { "• [${it.table}] row ${it.rowIndex}: ${it.reason}" }
            val suffix  = if (result.rowErrors.size > maxShow) "\n… and ${result.rowErrors.size - maxShow} more" else ""
            binding.tvErrorList.text = lines + suffix
            binding.tvErrorList.isVisible = true
        }

        binding.btnViewPatients.setOnClickListener {
            startActivity(Intent(this, ViewAllPatientsActivity::class.java))
            finish()
        }
        binding.btnDone.setOnClickListener { finish() }
    }

    private fun showErrorView(reason: String) {
        stopPipelineAnimation()

        binding.progressLayer.animate().alpha(0f).withEndAction {
            binding.progressLayer.visibility = View.GONE
            binding.errorLayer.visibility    = View.VISIBLE
            binding.errorLayer.animate().alpha(1f)
        }

        binding.ivErrorIcon.imageTintList = ColorStateList.valueOf(
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, 0)
        )
        binding.tvErrorDetail.text = reason

        binding.btnTryAgain.setOnClickListener { finish() }
        binding.btnCancelError.setOnClickListener { finish() }
    }
    // endregion

    override fun onDestroy() {
        super.onDestroy()
        stopPipelineAnimation()
    }
}
