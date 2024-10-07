package com.example.patientrecords.ui.base

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.example.patientrecords.R
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity() {

    private var toolbar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        initToolbar()
        applyTopPaddingToRoot()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.topAppBar)
        toolbar?.let {
            setSupportActionBar(it)
            it.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }

    private fun applyTopPaddingToRoot() {
        // Set Notification bar padding
        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top
            view.updatePadding(top = statusBarHeight)
            // view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
    }

    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
